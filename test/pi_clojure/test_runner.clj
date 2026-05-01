(ns pi-clojure.test-runner
  (:require [clojure.test :as test]
            [pi-clojure.cli-test]
            [pi-clojure.domain.markdown-test]
            [pi-clojure.domain.model-test]
            [pi-clojure.domain.user-test]))

(def test-namespaces
  '[pi-clojure.cli-test
    pi-clojure.domain.markdown-test
    pi-clojure.domain.model-test
    pi-clojure.domain.user-test])

(defn -main [& _args]
  (let [{:keys [fail error]} (apply test/run-tests test-namespaces)]
    (when (pos? (+ fail error))
      (System/exit 1))))
