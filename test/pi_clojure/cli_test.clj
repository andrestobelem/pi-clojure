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
      (is (= "Mensaje enviado a general por andres\n"
             (run! state-file "send" "general" "andres" "Hola **mundo**" "client-txn-1")))
      (is (= "Mensaje enviado a general por andres\n"
             (run! state-file "send" "general" "andres" "Hola **mundo**" "client-txn-1")))
      (is (= "# General\n\n## Mensajes\n\n1. andres: Hola **mundo**\n"
             (run! state-file "show" "general" "andres")))
      (is (= "# General\n\n## Mensajes\n\n### Mensaje 1\n\nHola **mundo**\n"
             (run! state-file "export" "general" "andres")))
      (is (= "andres salió de general\n"
             (run! state-file "leave" "general" "andres")))
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
      (is (= "# General\n\n## Mensajes\n\n\n"
             (run! state-file "export" "general" "andres")))
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
      (io/delete-file state-file true))))
