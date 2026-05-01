(ns pi-clojure.cli-test
  (:refer-clojure :exclude [run!])
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [pi-clojure.cli :as cli]))

(defn temp-state-file []
  (doto (java.io.File/createTempFile "pi-clojure-chat" ".edn")
    (.delete)))

(defn run! [state-file & args]
  (with-out-str
    (apply cli/run! (str state-file) args)))

(deftest minimal-chat-cli-flow
  (testing "given command invocations sharing a state file, when using the MVP flow, then chat state is persisted and rendered"
    (let [state-file (temp-state-file)]
      (is (= "Usuario creado: andres\n"
             (run! state-file "create-user" "andres")))
      (is (= "Sala creada: general\n"
             (run! state-file "create-room" "general")))
      (is (= "andres entró a general\n"
             (run! state-file "join" "general" "andres")))
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

(deftest cli-errors
  (testing "given an unknown command, when running the CLI, then it returns usage feedback"
    (let [state-file (temp-state-file)]
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"comando desconocido"
                            (cli/run! (str state-file) "unknown")))
      (io/delete-file state-file true))))
