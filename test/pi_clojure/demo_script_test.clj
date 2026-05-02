(ns pi-clojure.demo-script-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]))

(defn temp-demo-dir []
  (let [dir (java.nio.file.Files/createTempDirectory "pi-clojure-demo" (make-array java.nio.file.attribute.FileAttribute 0))]
    (.toFile dir)))

(defn run-script! [script demo-dir]
  (let [builder (doto (ProcessBuilder. ["bash" script])
                  (.directory (io/file ".")))
        _ (.put (.environment builder) "DEMO_TMP_DIR" (.getPath demo-dir))
        process (.start builder)
        exit-code (.waitFor process)]
    {:exit-code exit-code
     :out (slurp (.getInputStream process))
     :err (slurp (.getErrorStream process))}))

(defn run-demo! [demo-dir]
  (run-script! "scripts/demo-export-chat.sh" demo-dir))

(defn run-dogfood-demo! [demo-dir]
  (run-script! "scripts/demo-agent-roundtable.sh" demo-dir))

(deftest demo-export-chat-script
  (testing "given an isolated temp directory, when running the demo script, then it exercises chat flow and verifies exported Markdown"
    (let [demo-dir (temp-demo-dir)
          {:keys [exit-code out err]} (run-demo! demo-dir)
          export-file (io/file demo-dir "general-export.md")]
      (is (= 0 exit-code) err)
      (is (str/includes? out "$ clojure -M:chat create-user andres"))
      (is (str/includes? out "Sala personal de andres disponible"))
      (is (str/includes? out "# General"))
      (is (str/includes? out "1. andres: Hola **equipo**"))
      (is (str/includes? out "2. zoe: - item uno"))
      (is (str/includes? out "Exportación verificada:"))
      (is (.exists export-file))
      (is (str/includes? (slurp export-file) "Hola **equipo**"))
      (is (str/includes? (slurp export-file) "- item uno"))
      (io/delete-file export-file true)
      (io/delete-file (io/file demo-dir "state.edn") true)
      (io/delete-file demo-dir true))))

(deftest dogfood-agent-roundtable-script
  (testing "given an isolated temp directory, when running the dogfood roundtable, then agents use the chat and export candidate backlog"
    (let [demo-dir (temp-demo-dir)
          {:keys [exit-code out err]} (run-dogfood-demo! demo-dir)
          state-file (io/file demo-dir "agent-roundtable.edn")
          export-file (io/file demo-dir "agent-roundtable-demo.md")
          audit-file (io/file demo-dir "agent-roundtable-audit.md")]
      (is (= 0 exit-code) err)
      (is (str/includes? out "Dogfood agent roundtable"))
      (is (str/includes? out "Personalidades: pragmatica, esceptica, narradora"))
      (is (.exists state-file))
      (is (.exists export-file))
      (is (.exists audit-file))
      (let [audit (slurp audit-file)]
        (is (str/includes? audit "# Auditoría dogfood roundtable"))
        (is (str/includes? audit "Estado aislado"))
        (is (str/includes? audit "Sala: roundtable"))
        (is (str/includes? audit "Participantes"))
        (is (str/includes? audit "Turnos seriales"))
        (is (str/includes? audit "Fricciones observadas"))
        (is (str/includes? audit "clojure -M:chat create-user pragmatica")))
      (let [markdown (slurp export-file)]
        (is (str/includes? markdown "# Roundtable"))
        (is (str/includes? markdown "Pragmática"))
        (is (str/includes? markdown "Escéptica"))
        (is (str/includes? markdown "Narradora"))
        (is (str/includes? markdown "### Story candidata"))
        (is (str/includes? markdown "Primer test rojo sugerido")))
      (doseq [file ["pragmatica.md" "esceptica.md" "narradora.md"]]
        (io/delete-file (io/file demo-dir "prompts" file) true))
      (io/delete-file (io/file demo-dir "prompts") true)
      (io/delete-file audit-file true)
      (io/delete-file export-file true)
      (io/delete-file state-file true)
      (io/delete-file demo-dir true))))
