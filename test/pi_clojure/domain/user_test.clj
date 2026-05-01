(ns pi-clojure.domain.user-test
  (:require [clojure.spec.alpha :as s]
            [clojure.test :refer [deftest is testing]]
            [pi-clojure.domain.user :as user]))

(deftest create-user-with-handle-and-type
  (testing "given a handle and human type, when creating a user, then it returns a human user"
    (is (= #:user{:handle "andres"
                  :type :user.type/human}
           (user/create-user "andres" :user.type/human))))

  (testing "given a handle and agent type, when creating a user, then it returns an agent user"
    (is (= #:user{:handle "agent"
                  :type :user.type/agent}
           (user/create-user "agent" :user.type/agent)))))

(deftest user-specs
  (testing "given canonical and non-canonical handles, when validating handle spec, then only canonical handles are valid"
    (is (s/valid? :user/handle "andres_42-test"))
    (is (not (s/valid? :user/handle "Andres")))
    (is (not (s/valid? :user/handle " andres")))
    (is (not (s/valid? :user/handle "andres."))))

  (testing "given a human user, when validating user spec, then it is valid"
    (is (s/valid? :user/user
                  #:user{:handle "andres"
                         :type :user.type/human})))

  (testing "given an agent user, when validating user spec, then it is valid"
    (is (s/valid? :user/user
                  #:user{:handle "agent"
                         :type :user.type/agent}))))

(deftest create-user-in-store
  (testing "given an empty store, when creating a user, then it persists the user by handle"
    (let [store (user/create-store)
          created-user (user/create-user! store "andres" :user.type/human)]
      (is (= #:user{:id "user:andres"
                    :handle "andres"
                    :type :user.type/human}
             created-user))
      (is (= created-user
             (user/find-by-handle store "andres")))))

  (testing "given an empty store, when creating an agent user, then it preserves the agent type"
    (let [store (user/create-store)
          created-user (user/create-user! store "agent" :user.type/agent)]
      (is (= #:user{:id "user:agent"
                    :handle "agent"
                    :type :user.type/agent}
             created-user))))

  (testing "given an existing handle, when creating another user with the same handle, then it rejects the duplicate"
    (let [store (user/create-store)]
      (user/create-user! store "andres" :user.type/human)
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"handle already exists"
                            (user/create-user! store "andres" :user.type/agent)))))

  (testing "given a handle with surrounding whitespace, when creating a user, then it rejects the handle"
    (let [store (user/create-store)]
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"handle cannot have surrounding whitespace"
                            (user/create-user! store " andres " :user.type/human)))))

  (testing "given a handle with uppercase letters, when creating a user, then it rejects the handle"
    (let [store (user/create-store)]
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"handle must be lowercase"
                            (user/create-user! store "Andres" :user.type/human)))))

  (testing "given a blank handle, when creating a user, then it rejects the handle"
    (let [store (user/create-store)]
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"handle is required"
                            (user/create-user! store "" :user.type/human)))))

  (testing "given a handle with allowed characters, when creating a user, then it accepts the handle"
    (let [store (user/create-store)]
      (is (= #:user{:id "user:andres_42-test"
                    :handle "andres_42-test"
                    :type :user.type/human}
             (user/create-user! store "andres_42-test" :user.type/human)))))

  (testing "given a handle with unsupported characters, when creating a user, then it rejects the handle"
    (let [store (user/create-store)]
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"handle has unsupported characters"
                            (user/create-user! store "andres.test" :user.type/human)))))

  (testing "given a handle shorter than 3 characters, when creating a user, then it rejects the handle"
    (let [store (user/create-store)]
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"handle is too short"
                            (user/create-user! store "ab" :user.type/human)))))

  (testing "given a handle longer than 39 characters, when creating a user, then it rejects the handle"
    (let [store (user/create-store)]
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"handle is too long"
                            (user/create-user! store "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" :user.type/human)))))

  (testing "given a handle that starts or ends with a separator, when creating a user, then it rejects the handle"
    (let [store (user/create-store)]
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"handle cannot start or end with a separator"
                            (user/create-user! store "-andres" :user.type/human)))
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"handle cannot start or end with a separator"
                            (user/create-user! store "andres_" :user.type/human)))))

  (testing "given a handle with consecutive separators, when creating a user, then it rejects the handle"
    (let [store (user/create-store)]
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"handle cannot contain consecutive separators"
                            (user/create-user! store "andres--test" :user.type/human)))
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"handle cannot contain consecutive separators"
                            (user/create-user! store "andres__test" :user.type/human)))
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"handle cannot contain consecutive separators"
                            (user/create-user! store "andres-_test" :user.type/human)))
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"handle cannot contain consecutive separators"
                            (user/create-user! store "andres_-test" :user.type/human)))))

  (testing "given a reserved handle, when creating a user, then it rejects the handle"
    (let [store (user/create-store)]
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"handle is reserved"
                            (user/create-user! store "admin" :user.type/human)))
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"handle is reserved"
                            (user/create-user! store "support" :user.type/human))))))

(deftest create-personal-room-for-human-user
  (testing "given an empty store, when creating a human user, then it creates a private user room owned by the user"
    (let [store (user/create-store)
          created-user (user/create-user! store "andres" :user.type/human)]
      (is (= #:room{:id "room:user:andres"
                    :type :room.type/user
                    :title "andres"
                    :owner-id (:user/id created-user)
                    :visibility :room.visibility/private}
             (user/find-personal-room-by-owner store (:user/id created-user))))))

  (testing "given a created human user, when checking participation, then the owner is an active participant in the personal room"
    (let [store (user/create-store)
          created-user (user/create-user! store "andres" :user.type/human)
          personal-room (user/find-personal-room-by-owner store (:user/id created-user))]
      (is (user/active-participant? store (:user/id created-user) (:room/id personal-room)))))

  (testing "given an existing personal room, when ensuring it again, then it does not duplicate the room"
    (let [store (user/create-store)
          created-user (user/create-user! store "andres" :user.type/human)
          personal-room (user/find-personal-room-by-owner store (:user/id created-user))]
      (is (= personal-room
             (user/ensure-personal-room! store created-user)))
      (is (= [personal-room]
             (user/list-rooms store))))))

(deftest create-shared-room
  (testing "given a title, when creating a shared room, then it persists a shared room"
    (let [store (user/create-store)
          room (user/create-shared-room! store "general" "General")]
      (is (= #:room{:id "general"
                    :type :room.type/shared
                    :title "General"
                    :visibility :room.visibility/shared}
             room))
      (is (= room
             (user/find-room-by-id store "general")))))

  (testing "given existing users, when creating a shared room, then it is accessible to them"
    (let [store (user/create-store)
          andres (user/create-user! store "andres" :user.type/human)
          zoe (user/create-user! store "zoe" :user.type/human)
          room (user/create-shared-room! store "general" "General")]
      (is (some #{room}
                (user/list-accessible-rooms store (:user/id andres))))
      (is (some #{room}
                (user/list-accessible-rooms store (:user/id zoe)))))))

(deftest list-users-in-store
  (testing "given users created out of order, when listing users, then it returns them ordered by handle"
    (let [store (user/create-store)
          zoe (user/create-user! store "zoe" :user.type/human)
          andres (user/create-user! store "andres" :user.type/human)]
      (is (= [andres zoe]
             (user/list-users store))))))
