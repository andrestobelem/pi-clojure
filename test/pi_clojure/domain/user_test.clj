(ns pi-clojure.domain.user-test
  (:require [clojure.test :refer [deftest is testing]]
            [pi-clojure.domain.user :as user]))

(deftest create-human-user-with-handle
  (testing "creates a human user with the provided handle"
    (is (= #:user{:handle "andres"
                  :type :user.type/human}
           (user/create-human "andres")))))
