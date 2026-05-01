(ns pi-clojure.domain.user-test
  (:require [clojure.test :refer [deftest is testing]]
            [pi-clojure.domain.user :as user]))

(deftest create-human-user-with-handle
  (testing "creates a human user with the provided handle"
    (is (= #:user{:handle "andres"
                  :type :user.type/human}
           (user/create-human "andres")))))

(deftest create-human-user-in-store
  (testing "persists a human user with a unique handle"
    (let [store (user/create-store)
          created-user (user/create-human! store "andres")]
      (is (= #:user{:handle "andres"
                    :type :user.type/human}
             created-user))
      (is (= created-user
             (user/find-by-handle store "andres")))))

  (testing "rejects duplicated handles"
    (let [store (user/create-store)]
      (user/create-human! store "andres")
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"handle already exists"
                            (user/create-human! store "andres")))))

  (testing "rejects blank handles"
    (let [store (user/create-store)]
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"handle is required"
                            (user/create-human! store ""))))))
