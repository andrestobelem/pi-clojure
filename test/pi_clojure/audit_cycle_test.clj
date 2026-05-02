(ns pi-clojure.audit-cycle-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [pi-clojure.cli :as cli]))

(defn temp-edn-file []
  (doto (java.io.File/createTempFile "pi-clojure-audit-cycle" ".edn")
    (.delete)))

(defn run-main-status! [& args]
  (let [out (java.io.StringWriter.)
        err (java.io.StringWriter.)]
    (binding [*out* out
              *err* err]
      {:exit-code (cli/main-status! "unused-state.edn" args)
       :out (str out)
       :err (str err)})))

(deftest audit-cycle-fixture-reports-project-and-worktree-inconsistencies
  (testing "given simulated Project items and local worktrees, when auditing the cycle, then it reports stable warnings without writing chat state"
    (let [fixture-file (temp-edn-file)
          fixture {:issues [{:number 50 :title "Acceso seguro" :status "In Progress" :foco "Ahora"}
                            {:number 51 :title "Checklist operativo" :status "In Progress" :foco "Ahora"}
                            {:number 53 :title "Auditoría ejecutable" :status "In Progress" :foco "Ahora"}
                            {:number 52 :title "Locking de estado" :status "Todo" :foco "Siguiente"}]
                   :worktrees [{:path "../pi-clojure-50" :branch "story/50-acceso-seguro" :git-status " M test/pi_clojure/cli_test.clj\n"}
                               {:path "../pi-clojure-51" :branch "story/999-rama-equivocada" :git-status "UU src/pi_clojure/cli.clj\n"}]}
          _ (spit fixture-file (pr-str fixture))
          {:keys [exit-code out err]} (run-main-status! "audit-cycle" "--fixture" (.getPath fixture-file))]
      (is (= 1 exit-code))
      (is (= "" err))
      (is (str/includes? out "#50 | In Progress | Ahora | story/50-acceso-seguro | ../pi-clojure-50 | cambios pendientes"))
      (is (str/includes? out "#51 | In Progress | Ahora | story/999-rama-equivocada | ../pi-clojure-51 | conflictos UU"))
      (is (str/includes? out "#53 | In Progress | Ahora | sin branch | sin worktree | limpio"))
      (is (str/includes? out "Advertencia: hay 3 items en In Progress (máximo 2)"))
      (is (str/includes? out "Advertencia: hay 3 items con Foco: Ahora (máximo 2)"))
      (is (str/includes? out "Advertencia: #53 está In Progress sin branch/worktree local detectable"))
      (is (str/includes? out "Advertencia: #50 tiene cambios pendientes en ../pi-clojure-50"))
      (is (str/includes? out "Advertencia: #51 tiene conflictos UU en ../pi-clojure-51"))
      (is (str/includes? out "Advertencia: worktree ../pi-clojure-51 usa branch story/999-rama-equivocada, esperada para issue #51"))
      (is (false? (.exists (io/file "unused-state.edn"))))
      (io/delete-file fixture-file true))))
