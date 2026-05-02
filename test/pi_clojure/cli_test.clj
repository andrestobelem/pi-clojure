(ns pi-clojure.cli-test
  (:refer-clojure :exclude [run!])
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [pi-clojure.cli :as cli]))

(defn temp-state-file []
  (doto (java.io.File/createTempFile "pi-clojure-chat" ".edn")
    (.delete)))

(defn temp-output-file []
  (doto (java.io.File/createTempFile "pi-clojure-export" ".md")
    (.delete)))

(defn run! [state-file & args]
  (with-out-str
    (apply cli/run! (str state-file) args)))

(defn run-main-status! [state-file & args]
  (let [out (java.io.StringWriter.)
        err (java.io.StringWriter.)]
    (binding [*out* out
              *err* err]
      {:exit-code (cli/main-status! (str state-file) args)
       :out (str out)
       :err (str err)})))

(defn create-general-room-with-message! [state-file]
  (run! state-file "create-user" "andres")
  (run! state-file "create-room" "general")
  (run! state-file "join" "general" "andres")
  (run! state-file "send" "general" "andres" "Hola **mundo**" "client-txn-1"))

(def valid-backlog-message
  "### Fricción observada\n\nAlgo molesta.\n\n### Historia candidata\n\nComo usuaria, quiero validar.\n\n### Criterios de aceptación\n\n- informa éxito\n\n### Primer test rojo sugerido\n\nFalla primero.")

(deftest minimal-chat-cli-flow
  (testing "given command invocations sharing a state file, when using the MVP flow, then chat state is persisted and rendered"
    (let [state-file (temp-state-file)]
      (is (= "Usuario creado: andres\n"
             (run! state-file "create-user" "andres")))
      (is (= "Sala creada: general\n"
             (run! state-file "create-room" "general")))
      (is (= "andres entró a general\n"
             (run! state-file "join" "general" "andres")))
      (is (= (str "[{:user-id \"user:andres\", "
                  ":handle \"andres\", "
                  ":user-type :user.type/human}]\n")
             (run! state-file "participants" "general")))
      (is (= (str "Mensaje creado y enviado a general por andres\n"
                  "client-txn-id: client-txn-1\n")
             (run! state-file "send" "general" "andres" "Hola **mundo**" "client-txn-1")))
      (is (= (str "Mensaje reutilizado por idempotencia en general por andres\n"
                  "client-txn-id: client-txn-1\n")
             (run! state-file "send" "general" "andres" "Hola **mundo**" "client-txn-1")))
      (is (= "# General\n\n## Mensajes\n\n1. andres: Hola **mundo**\n"
             (run! state-file "show" "general" "andres")))
      (is (= (str "# General\n\n"
                  "Tipo: shared\n\n"
                  "## Mensajes\n\n"
                  "### Mensaje 1\n\n"
                  "Autor: andres\n\n"
                  "Hola **mundo**\n")
             (run! state-file "export" "general" "andres")))
      (is (= "andres salió de general\n"
             (run! state-file "leave" "general" "andres")))
      (io/delete-file state-file true))))

(deftest rooms-command-lists-shared-rooms-with-basic-activity
  (testing "given shared rooms with messages and participants, when listing rooms, then output is stable and ordered"
    (let [state-file (temp-state-file)]
      (doseq [handle ["alice" "bob" "carol"]]
        (run! state-file "create-user" handle))
      (run! state-file "create-room" "beta")
      (run! state-file "create-room" "alfa")
      (run! state-file "join" "beta" "alice")
      (run! state-file "join" "alfa" "alice")
      (run! state-file "join" "alfa" "bob")
      (run! state-file "join" "beta" "carol")
      (run! state-file "send" "alfa" "alice" "Hola alfa" "txn-alfa-1")
      (run! state-file "send" "alfa" "bob" "Respuesta alfa" "txn-alfa-2")
      (run! state-file "send" "beta" "carol" "Hola beta" "txn-beta-1")
      (is (= (str "Salas:\n"
                  "- alfa | mensajes: 2 | participantes: 2\n"
                  "- beta | mensajes: 1 | participantes: 2\n")
             (run! state-file "rooms")))
      (io/delete-file state-file true))))

(deftest rooms-command-empty-state-is-clear-and-successful
  (testing "given no rooms, when listing rooms from the binary entrypoint, then it prints a clear message and exits successfully"
    (let [state-file (temp-state-file)
          {:keys [exit-code out err]} (run-main-status! state-file "rooms")]
      (is (= 0 exit-code))
      (is (= "No hay salas disponibles.\n" out))
      (is (= "" err))
      (is (false? (.exists state-file))))))

(deftest send-command-reports-idempotency-outcome
  (testing "given the same client txn id is retried, when sending, then the CLI reports creation vs reuse and keeps one message"
    (let [state-file (temp-state-file)]
      (run! state-file "create-user" "andres")
      (run! state-file "create-room" "general")
      (run! state-file "join" "general" "andres")
      (is (= (str "Mensaje creado y enviado a general por andres\n"
                  "client-txn-id: client-txn-1\n")
             (run! state-file "send" "general" "andres" "Hola **mundo**" "client-txn-1")))
      (is (= (str "Mensaje reutilizado por idempotencia en general por andres\n"
                  "client-txn-id: client-txn-1\n")
             (run! state-file "send" "general" "andres" "Hola **mundo**" "client-txn-1")))
      (is (= "# General\n\n## Mensajes\n\n1. andres: Hola **mundo**\n"
             (run! state-file "show" "general" "andres")))
      (io/delete-file state-file true))))

(deftest send-command-with-warnings
  (testing "given Markdown with only lint warnings, when sending, then it persists and prints success with warnings"
    (let [state-file (temp-state-file)]
      (run! state-file "create-user" "andres")
      (run! state-file "create-room" "general")
      (run! state-file "join" "general" "andres")
      (is (= (str "Mensaje creado y enviado a general por andres\n"
                  "client-txn-id: client-txn-1\n"
                  "Advertencia: bloque de código sin lenguaje en message.body\n")
             (run! state-file "send" "general" "andres" "```
(+ 1 1)
```" "client-txn-1")))
      (is (= "# General\n\n## Mensajes\n\n1. andres: ```
(+ 1 1)
```\n"
             (run! state-file "show" "general" "andres")))
      (io/delete-file state-file true))))

(deftest show-room-with-message-metadata
  (testing "given messages with known client txn ids, when showing a room with metadata, then audit fields are visible in order"
    (let [state-file (temp-state-file)]
      (run! state-file "create-user" "andres")
      (run! state-file "create-room" "general")
      (run! state-file "join" "general" "andres")
      (run! state-file "send" "general" "andres" "Primer mensaje" "client-txn-1")
      (run! state-file "send" "general" "andres" "Segundo mensaje" "client-txn-2")
      (let [shown (run! state-file "show" "general" "andres" "--with-meta")]
        (is (str/includes? shown "# General"))
        (is (str/includes? shown "1. andres: Primer mensaje"))
        (is (str/includes? shown "2. andres: Segundo mensaje"))
        (is (< (str/index-of shown "client-txn-1")
               (str/index-of shown "client-txn-2")))
        (is (str/includes? shown "Autor: andres"))
        (is (str/includes? shown "client-txn-id: client-txn-1"))
        (is (str/includes? shown "client-txn-id: client-txn-2"))
        (is (re-find #"timestamp: .+" shown)))
      (is (= "# General\n\n## Mensajes\n\n1. andres: Primer mensaje\n2. andres: Segundo mensaje\n"
             (run! state-file "show" "general" "andres")))
      (io/delete-file state-file true))))

(deftest export-room-with-message-metadata
  (testing "given messages with known client txn ids, when exporting with metadata, then audit fields are visible without losing Markdown"
    (let [state-file (temp-state-file)]
      (run! state-file "create-user" "andres")
      (run! state-file "create-room" "general")
      (run! state-file "join" "general" "andres")
      (run! state-file "send" "general" "andres" "### Decisión\n\nUsar **Markdown**\n\n- mantener formato" "client-txn-1")
      (run! state-file "send" "general" "andres" "Segundo con `código`" "client-txn-2")
      (let [exported (run! state-file "export" "general" "andres" "--with-meta")]
        (is (str/includes? exported "# General"))
        (is (str/includes? exported "Tipo: shared"))
        (is (str/includes? exported "### Mensaje 1"))
        (is (str/includes? exported "Autor: andres"))
        (is (str/includes? exported "Orden: 1"))
        (is (str/includes? exported "client-txn-id: client-txn-1"))
        (is (str/includes? exported "client-txn-id: client-txn-2"))
        (is (re-find #"timestamp: .+" exported))
        (is (str/includes? exported "### Decisión\n\nUsar **Markdown**\n\n- mantener formato"))
        (is (< (str/index-of exported "client-txn-1")
               (str/index-of exported "client-txn-2"))))
      (is (not (str/includes? (run! state-file "export" "general" "andres")
                              "client-txn-id")))
      (io/delete-file state-file true))))

(deftest export-room-to-markdown-file
  (testing "given an output path, when exporting a room, then it writes the same Markdown as stdout and confirms the export"
    (let [state-file (temp-state-file)
          output-file (temp-output-file)]
      (create-general-room-with-message! state-file)
      (let [exported-markdown (run! state-file "export" "general" "andres")]
        (is (= (str "Exportación escrita en " (.getPath output-file)
                    " para la sala general con 1 mensajes\n")
               (run! state-file "export" "general" "andres" "--output" (.getPath output-file))))
        (is (= exported-markdown (slurp output-file))))
      (io/delete-file state-file true)
      (io/delete-file output-file true))))

(deftest export-room-does-not-overwrite-existing-file-without-force
  (testing "given an existing output file, when exporting without force, then it fails without overwriting it"
    (let [state-file (temp-state-file)
          output-file (temp-output-file)
          original-content "contenido existente"]
      (spit output-file original-content)
      (create-general-room-with-message! state-file)
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"El archivo de exportación ya existe"
                            (run! state-file "export" "general" "andres" "--output" (.getPath output-file))))
      (is (= original-content (slurp output-file)))
      (is (= (str "Exportación escrita en " (.getPath output-file)
                  " para la sala general con 1 mensajes\n")
             (run! state-file "export" "general" "andres" "--output" (.getPath output-file) "--force")))
      (is (not= original-content (slurp output-file)))
      (io/delete-file state-file true)
      (io/delete-file output-file true))))

(deftest send-help-shows-safe-markdown-example
  (testing "when asking for send help, then it shows a shell-safe Markdown example with backticks and a client txn id"
    (let [state-file (temp-state-file)
          help-output (run! state-file "help" "send")]
      (is (str/includes? help-output "Uso: clojure -M:chat send"))
      (is (str/includes? help-output "$'Mensaje con `codigo` inline'"))
      (is (str/includes? help-output "client-txn-1"))
      (is (str/includes? help-output "Evita comillas dobles"))
      (is (false? (.exists state-file))))))

(deftest validate-backlog-message-cli
  (testing "given a backlog message missing a required heading, when validating structure, then it fails without creating state"
    (let [state-file (temp-state-file)
          {:keys [exit-code out err]} (run-main-status! state-file
                                                        "validate-backlog-message"
                                                        "### Fricción observada\n\nAlgo molesta.\n\n### Criterios de aceptación\n\n- uno\n\n### Primer test rojo sugerido\n\nFalla primero.")]
      (is (= 1 exit-code))
      (is (= "" out))
      (is (str/includes? err "Error: Falta encabezado requerido: ### Historia candidata"))
      (is (str/includes? err "Código: backlog-message/missing-heading"))
      (is (str/includes? err "Campo: message.body"))
      (is (false? (.exists state-file)))))

  (testing "given a valid backlog message, when validating structure, then it reports it can be published without creating state"
    (let [state-file (temp-state-file)]
      (is (= "Mensaje backlog válido: puede publicarse\n"
             (run! state-file "validate-backlog-message" valid-backlog-message)))
      (is (false? (.exists state-file)))))

  (testing "given a valid backlog message in a file, when validating structure, then it reads the file without creating state"
    (let [state-file (temp-state-file)
          message-file (temp-output-file)]
      (spit message-file valid-backlog-message)
      (is (= "Mensaje backlog válido: puede publicarse\n"
             (run! state-file "validate-backlog-message" "--file" (.getPath message-file))))
      (is (false? (.exists state-file)))
      (io/delete-file message-file true))))

(deftest validate-markdown-cli
  (testing "given valid Markdown, when validating before sending, then it prints success without creating state"
    (let [state-file (temp-state-file)]
      (is (= "Markdown válido\n"
             (run! state-file "validate-markdown" "Hola **mundo**")))
      (is (false? (.exists state-file)))))

  (testing "given an existing state file, when validating Markdown, then it does not modify state"
    (let [state-file (temp-state-file)
          original-state "{:existing true}"]
      (spit state-file original-state)
      (is (= "Markdown válido\n"
             (run! state-file "validate-markdown" "Hola **mundo**")))
      (is (= original-state (slurp state-file)))))

  (testing "given invalid Markdown, when validating before sending, then it prints a structured error without creating state"
    (let [state-file (temp-state-file)
          {:keys [exit-code out err]} (run-main-status! state-file
                                                        "validate-markdown"
                                                        "<b>Hola</b>")]
      (is (= 1 exit-code))
      (is (= "" out))
      (is (str/includes? err "Error: El mensaje contiene HTML crudo no permitido"))
      (is (str/includes? err "Código: markdown/raw-html"))
      (is (str/includes? err "Campo: message.body"))
      (is (false? (.exists state-file))))))

(deftest cli-errors
  (testing "given an unknown command, when running the CLI, then it returns usage feedback"
    (let [state-file (temp-state-file)]
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"comando desconocido"
                            (cli/run! (str state-file) "unknown")))
      (io/delete-file state-file true))))

(deftest actionable-cli-errors
  (testing "given unsafe Markdown, when sending from the binary entrypoint, then the CLI prints an actionable error and does not persist the message"
    (let [state-file (temp-state-file)]
      (run! state-file "create-user" "andres")
      (run! state-file "create-room" "general")
      (run! state-file "join" "general" "andres")
      (let [{:keys [exit-code out err]} (run-main-status! state-file
                                                          "send"
                                                          "general"
                                                          "andres"
                                                          "Hola <b>mundo</b>")]
        (is (= 1 exit-code))
        (is (= "" out))
        (is (str/includes? err "Error: El mensaje contiene HTML crudo no permitido"))
        (is (str/includes? err "Código: markdown/raw-html"))
        (is (str/includes? err "Campo: message.body")))
      (is (= "# General\n\n## Mensajes\n\n\n"
             (run! state-file "show" "general" "andres")))
      (is (= "# General\n\nTipo: shared\n\n## Mensajes\n\n\n"
             (run! state-file "export" "general" "andres")))
      (io/delete-file state-file true)))

  (testing "given an incompatible idempotent retry, when running the binary entrypoint, then the CLI prints a structured conflict and does not persist the retry"
    (let [state-file (temp-state-file)]
      (run! state-file "create-user" "andres")
      (run! state-file "create-room" "general")
      (run! state-file "join" "general" "andres")
      (run! state-file "send" "general" "andres" "Hola **mundo**" "client-txn-1")
      (let [{:keys [exit-code out err]} (run-main-status! state-file
                                                          "send"
                                                          "general"
                                                          "andres"
                                                          "Chau **mundo**"
                                                          "client-txn-1")]
        (is (= 1 exit-code))
        (is (= "" out))
        (is (str/includes? err "Error: client-txn-id ya fue usado para otro mensaje"))
        (is (str/includes? err "Código: idempotency/conflict"))
        (is (str/includes? err "Campo: message.client-txn-id")))
      (is (= "# General\n\n## Mensajes\n\n1. andres: Hola **mundo**\n"
             (run! state-file "show" "general" "andres")))
      (io/delete-file state-file true)))

  (testing "given a missing user or room, when running the binary entrypoint, then the CLI prints clear actionable errors"
    (let [state-file (temp-state-file)]
      (run! state-file "create-user" "andres")
      (run! state-file "create-room" "general")
      (is (= {:exit-code 1
              :out ""
              :err "Error: El usuario \"ana\" no existe\nCódigo: user/not-found\nCampo: user.handle\n"}
             (run-main-status! state-file "join" "general" "ana")))
      (is (= {:exit-code 1
              :out ""
              :err "Error: La sala \"random\" no existe\nCódigo: room/not-found\nCampo: room.name\n"}
             (run-main-status! state-file "join" "random" "andres")))
      (io/delete-file state-file true)))

  (testing "given a non participant, when exporting from the binary entrypoint, then the CLI prints an access error"
    (let [state-file (temp-state-file)]
      (run! state-file "create-user" "andres")
      (run! state-file "create-user" "zoe")
      (run! state-file "create-room" "general")
      (run! state-file "join" "general" "andres")
      (let [{:keys [exit-code out err]} (run-main-status! state-file
                                                          "export"
                                                          "general"
                                                          "zoe")]
        (is (= 1 exit-code))
        (is (= "" out))
        (is (str/includes? err "Error: room export access denied"))
        (is (str/includes? err "Código: room/access-denied"))
        (is (str/includes? err "Campo: room.access")))
      (io/delete-file state-file true))))
