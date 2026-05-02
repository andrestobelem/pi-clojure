(ns pi-clojure.cli
  (:refer-clojure :exclude [run!])
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [pi-clojure.domain.markdown :as markdown]
            [pi-clojure.domain.user :as chat])
  (:import [java.nio.channels FileChannel OverlappingFileLockException]
           [java.nio.file Files StandardCopyOption StandardOpenOption]))

(def default-state-file ".pi-chat.edn")

(defn state-file-from-env []
  (or (System/getenv "PI_CHAT_STATE_FILE")
      default-state-file))

(defn load-store [state-file]
  (let [file (io/file state-file)]
    (if (.exists file)
      (atom (edn/read-string (slurp file)))
      (chat/create-store))))

(defn save-store! [state-file store]
  (let [state-file (io/file state-file)
        state-path (.toPath state-file)
        parent (or (.getParentFile state-file)
                   (io/file "."))
        temp-file (java.io.File/createTempFile "pi-chat-state" ".edn.tmp" parent)
        temp-path (.toPath temp-file)]
    (spit temp-file (pr-str @store))
    (try
      (Files/move temp-path
                  state-path
                  (into-array StandardCopyOption
                              [StandardCopyOption/ATOMIC_MOVE
                               StandardCopyOption/REPLACE_EXISTING]))
      (catch java.nio.file.AtomicMoveNotSupportedException _
        (Files/move temp-path
                    state-path
                    (into-array StandardCopyOption
                                [StandardCopyOption/REPLACE_EXISTING]))))))

(def state-lock-timeout-ms 100)

(defn lock-file-path [state-file]
  (str state-file ".lock"))

(defn lock-unavailable! [state-file]
  (throw (ex-info (str "No se pudo obtener el lock del estado: " state-file)
                  {:error/type :state/lock-unavailable
                   :error/path [:state/lock]
                   :state/file state-file
                   :state/lock-file (lock-file-path state-file)})))

(defn try-lock [channel]
  (try
    (.tryLock channel)
    (catch OverlappingFileLockException _
      nil)))

(defn acquire-lock! [channel state-file]
  (let [deadline (+ (System/currentTimeMillis) state-lock-timeout-ms)]
    (loop []
      (if-let [lock (try-lock channel)]
        lock
        (if (< (System/currentTimeMillis) deadline)
          (do
            (Thread/sleep 10)
            (recur))
          (lock-unavailable! state-file))))))

(defn with-state-lock! [state-file f]
  (with-open [channel (FileChannel/open (.toPath (io/file (lock-file-path state-file)))
                                        (into-array StandardOpenOption
                                                    [StandardOpenOption/CREATE
                                                     StandardOpenOption/WRITE]))]
    (let [lock (acquire-lock! channel state-file)]
      (try
        (f)
        (finally
          (.release lock))))))

(defn room-title [room-name]
  (str/capitalize room-name))

(defn shared-room-id [room-name]
  (str "room:shared:" (chat/room-slug-for-title room-name)))

(defn require-user [store handle]
  (or (chat/find-by-handle store handle)
      (throw (ex-info (str "El usuario \"" handle "\" no existe")
                      {:error/type :user/not-found
                       :error/path [:user/handle]
                       :handle handle}))))

(defn require-room [store room-name]
  (let [room-id (shared-room-id room-name)]
    (or (chat/find-room store room-id)
        (throw (ex-info (str "La sala \"" room-name "\" no existe")
                        {:error/type :room/not-found
                         :error/path [:room/name]
                         :room room-name})))))

(defn handle-by-id [store user-id]
  (:user/handle (chat/find-by-id store user-id)))

(defn format-conversation [store room messages]
  (str "# " (:room/title room) "\n\n"
       "## Mensajes\n\n"
       (str/join "\n"
                 (map (fn [message]
                        (str (:message/sequence message)
                             ". "
                             (handle-by-id store (:message/author-id message))
                             ": "
                             (:message/body-markdown message)))
                      messages))
       "\n"))

(defn format-conversation-with-meta [store room messages]
  (str "# " (:room/title room) "\n\n"
       "## Mensajes\n\n"
       (str/join "\n\n"
                 (map (fn [message]
                        (let [handle (handle-by-id store (:message/author-id message))]
                          (str (:message/sequence message)
                               ". " handle ": " (:message/body-markdown message) "\n"
                               "   - Autor: " handle "\n"
                               "   - timestamp: " (or (:message/created-at message) "no disponible") "\n"
                               "   - client-txn-id: " (or (:message/client-txn-id message) "no disponible"))))
                      messages))
       "\n"))

(defn show-options [args]
  (loop [remaining args
         options {:with-meta? false}]
    (case (first remaining)
      nil options
      "--with-meta" (recur (next remaining)
                            (assoc options :with-meta? true))
      (throw (ex-info "opción de show desconocida"
                      {:option (first remaining)})))))

(defn export-options [args]
  (loop [remaining args
         options {:force? false
                  :with-meta? false}]
    (case (first remaining)
      nil options
      "--output" (recur (nnext remaining)
                         (assoc options :output (second remaining)))
      "--force" (recur (next remaining)
                        (assoc options :force? true))
      "--with-meta" (recur (next remaining)
                            (assoc options :with-meta? true))
      (throw (ex-info "opción de exportación desconocida"
                      {:option (first remaining)})))))

(defn write-export-file! [path markdown force?]
  (let [file (io/file path)]
    (when (and (.exists file) (not force?))
      (throw (ex-info "El archivo de exportación ya existe"
                      {:error/type :export/file-exists
                       :error/path [:export/output]
                       :path path})))
    (spit file markdown)))

(defn room-display-name [room]
  (chat/room-slug-for-title (:room/title room)))

(defn room-activity-summary [store room]
  {:name (room-display-name room)
   :messages (count (chat/messages-in-room store (:room/id room)))
   :participants (count (chat/list-active-participants store (:room/id room)))})

(defn shared-room? [room]
  (= :room.type/shared (:room/type room)))

(defn room-activity-summaries [store]
  (->> (chat/list-rooms store)
       (filter shared-room?)
       (map #(room-activity-summary store %))
       (sort-by :name)))

(defn format-room-activity-summary [{:keys [name messages participants]}]
  (str "- " name " | mensajes: " messages " | participantes: " participants))

(defn format-rooms [summaries]
  (if (seq summaries)
    (str "Salas:\n"
         (str/join "\n" (map format-room-activity-summary summaries))
         "\n")
    "No hay salas disponibles.\n"))

(defn message-body-arg [args]
  (let [[first-arg second-arg] args]
    (if (= "--file" first-arg)
      (slurp second-arg)
      first-arg)))

(def write-commands
  #{"create-user" "create-room" "join" "leave" "send"})

(defn write-command? [command]
  (contains? write-commands command))

(defn warning-field-name [warning]
  (when-let [path (first (:warning/path warning))]
    (str (namespace path) "." (name path))))

(defn warning-message [warning]
  (case (:warning/type warning)
    :markdown/code-block-without-language "bloque de código sin lenguaje"
    :markdown/very-long "mensaje muy largo"
    "advertencia de legibilidad"))

(defn format-warning [warning]
  (str "Advertencia: "
       (warning-message warning)
       (when-let [field (warning-field-name warning)]
         (str " en " field))))

(defn print-warnings! [warnings]
  (doseq [warning warnings]
    (println (format-warning warning))))

(defn send-outcome [existing-message]
  (if existing-message :message/reused :message/created))

(defn format-send-confirmation [room-name handle client-txn-id outcome]
  (str (case outcome
         :message/reused (str "Mensaje reutilizado por idempotencia en " room-name " por " handle)
         :message/created (str "Mensaje creado y enviado a " room-name " por " handle))
       "\n"
       "client-txn-id: " client-txn-id))

(def send-help
  (str "Uso: clojure -M:chat send <sala> <handle> <markdown> <client-txn-id>\n\n"
       "Ejemplo seguro para bash/zsh con Markdown y backticks:\n\n"
       "  clojure -M:chat send general andres $'Mensaje con `codigo` inline' client-txn-1\n\n"
       "Evita comillas dobles cuando el Markdown contiene backticks, porque la shell\n"
       "puede intentar ejecutar sustituciones de comando.\n"))

(defn help-text [topic]
  (case topic
    "send" send-help
    (throw (ex-info "tema de ayuda desconocido" {:topic topic}))))

(defn run-command! [store command args]
  (case command
    "validate-markdown"
    (let [[body-markdown] args]
      (markdown/validate-message-markdown! body-markdown)
      (println "Markdown válido"))

    "validate-backlog-message"
    (let [body-markdown (message-body-arg args)]
      (markdown/validate-backlog-message! body-markdown)
      (println "Mensaje backlog válido: puede publicarse"))

    "help"
    (let [[topic] args]
      (print (help-text topic)))

    "rooms"
    (print (format-rooms (room-activity-summaries store)))

    "create-user"
    (let [[handle] args]
      (chat/create-user! store handle :user.type/human)
      (println (str "Usuario creado: " handle)))

    "create-room"
    (let [[room-name] args]
      (chat/create-shared-room! store (room-title room-name))
      (println (str "Sala creada: " room-name)))

    "join"
    (let [[room-name handle] args
          user (require-user store handle)
          room (require-room store room-name)]
      (chat/join-room! store (:user/id user) (:room/id room))
      (println (str handle " entró a " room-name)))

    "send"
    (let [[room-name handle body-markdown client-txn-id] args
          user (require-user store handle)
          room (require-room store room-name)
          existing-message (chat/find-message-by-client-txn-id store (:user/id user) client-txn-id)
          outcome (send-outcome existing-message)
          message (chat/send-message! store (:user/id user) (:room/id room) body-markdown client-txn-id)]
      (println (format-send-confirmation room-name handle client-txn-id outcome))
      (print-warnings! (:message/warnings message)))

    "show"
    (let [[room-name handle & option-args] args
          user (require-user store handle)
          room (require-room store room-name)
          messages (chat/read-room store (:user/id user) (:room/id room))
          {:keys [with-meta?]} (show-options option-args)]
      (print ((if with-meta? format-conversation-with-meta format-conversation)
              store
              room
              messages)))

    "participants"
    (let [[room-name] args
          room (require-room store room-name)]
      (prn (chat/list-active-participants store (:room/id room))))

    "export"
    (let [[room-name handle & option-args] args
          user (require-user store handle)
          room (require-room store room-name)
          {:keys [output force? with-meta?]} (export-options option-args)
          markdown ((if with-meta?
                      chat/export-room-markdown-with-meta
                      chat/export-room-markdown)
                    store (:user/id user) (:room/id room))]
      (if output
        (do
          (write-export-file! output markdown force?)
          (println (str "Exportación escrita en " output
                        " para la sala " room-name
                        " con " (count (chat/messages-in-room store (:room/id room)))
                        " mensajes")))
        (print markdown)))

    "leave"
    (let [[room-name handle] args
          user (require-user store handle)
          room (require-room store room-name)]
      (chat/leave-room! store (:user/id user) (:room/id room))
      (println (str handle " salió de " room-name)))

    (throw (ex-info "comando desconocido" {:command command}))))

(defn run! [state-file command & args]
  (let [execute! (fn []
                   (let [store (load-store state-file)]
                     (run-command! store command args)
                     (when (write-command? command)
                       (save-store! state-file store))))]
    (if (write-command? command)
      (with-state-lock! state-file execute!)
      (execute!))))

(defn error-code [error]
  (when-let [type (:error/type error)]
    (str (namespace type) "/" (name type))))

(defn field-name [error]
  (when-let [path (first (:error/path error))]
    (str (namespace path) "." (name path))))

(defn primary-error [ex]
  (let [data (ex-data ex)]
    (or (first (:errors data))
        data)))

(defn format-actionable-error [ex]
  (let [error (primary-error ex)]
    (str/join "\n"
              (cond-> [(str "Error: " (ex-message ex))]
                (error-code error)
                (conj (str "Código: " (error-code error)))

                (field-name error)
                (conj (str "Campo: " (field-name error)))))))

(defn print-actionable-error! [ex]
  (binding [*out* *err*]
    (println (format-actionable-error ex))))

(defn main-status! [state-file args]
  (let [[command & command-args] args]
    (try
      (apply run! state-file command command-args)
      0
      (catch clojure.lang.ExceptionInfo ex
        (print-actionable-error! ex)
        1))))

(defn -main [& args]
  (let [status (main-status! (state-file-from-env) args)]
    (flush)
    (.flush *err*)
    (System/exit status)))
