(ns pi-clojure.cli
  (:refer-clojure :exclude [run!])
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [pi-clojure.domain.user :as chat]))

(def default-state-file ".pi-chat.edn")

(defn load-store [state-file]
  (let [file (io/file state-file)]
    (if (.exists file)
      (atom (edn/read-string (slurp file)))
      (chat/create-store))))

(defn save-store! [state-file store]
  (spit state-file (pr-str @store)))

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

(defn run-command! [store command args]
  (case command
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
          room (require-room store room-name)]
      (chat/send-message! store (:user/id user) (:room/id room) body-markdown client-txn-id)
      (println (str "Mensaje enviado a " room-name " por " handle)))

    "show"
    (let [[room-name handle] args
          user (require-user store handle)
          room (require-room store room-name)]
      (print (format-conversation store room (chat/read-room store (:user/id user) (:room/id room)))))

    "participants"
    (let [[room-name] args
          room (require-room store room-name)]
      (prn (chat/list-active-participants store (:room/id room))))

    "export"
    (let [[room-name handle] args
          _user (require-user store handle)
          room (require-room store room-name)]
      (print (chat/export-room-markdown store (:room/id room))))

    "leave"
    (let [[room-name handle] args
          user (require-user store handle)
          room (require-room store room-name)]
      (chat/leave-room! store (:user/id user) (:room/id room))
      (println (str handle " salió de " room-name)))

    (throw (ex-info "comando desconocido" {:command command}))))

(defn run! [state-file command & args]
  (let [store (load-store state-file)]
    (run-command! store command args)
    (save-store! state-file store)))

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
  (System/exit (main-status! default-state-file args)))
