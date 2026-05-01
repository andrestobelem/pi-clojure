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

  (testing "rejects handles with surrounding whitespace"
    (let [store (user/create-store)]
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"handle cannot have surrounding whitespace"
                            (user/create-human! store " andres ")))))

  (testing "rejects handles with uppercase letters"
    (let [store (user/create-store)]
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"handle must be lowercase"
                            (user/create-human! store "Andres")))))

  (testing "rejects blank handles"
    (let [store (user/create-store)]
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"handle is required"
                            (user/create-human! store "")))))

  (testing "allows letters numbers hyphens and underscores in handles"
    (let [store (user/create-store)]
      (is (= #:user{:handle "andres_42-test"
                    :type :user.type/human}
             (user/create-human! store "andres_42-test")))))

  (testing "rejects unsupported handle characters"
    (let [store (user/create-store)]
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"handle has unsupported characters"
                            (user/create-human! store "andres.test")))))

  (testing "rejects handles shorter than 3 characters"
    (let [store (user/create-store)]
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"handle is too short"
                            (user/create-human! store "ab")))))

  (testing "rejects handles longer than 39 characters"
    (let [store (user/create-store)]
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"handle is too long"
                            (user/create-human! store "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")))))

  (testing "rejects handles that start or end with separators"
    (let [store (user/create-store)]
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"handle cannot start or end with a separator"
                            (user/create-human! store "-andres")))
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"handle cannot start or end with a separator"
                            (user/create-human! store "andres_"))))))

(deftest list-users-in-store
  (testing "lists created users ordered by handle"
    (let [store (user/create-store)
          zoe (user/create-human! store "zoe")
          andres (user/create-human! store "andres")]
      (is (= [andres zoe]
             (user/list-users store))))))
