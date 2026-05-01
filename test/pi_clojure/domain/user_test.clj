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
             (user/find-by-handle store "andres")))
      (is (= created-user
             (user/find-by-id store (:user/id created-user))))))

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
          shared-room (user/create-shared-room! store "General")]
      (is (= #:room{:id "room:shared:general"
                    :type :room.type/shared
                    :title "General"}
             shared-room))
      (is (= shared-room
             (user/find-room store (:room/id shared-room))))))

  (testing "given an existing user and a shared room, when checking access, then the room is accessible to that user"
    (let [store (user/create-store)
          created-user (user/create-user! store "andres" :user.type/human)
          shared-room (user/create-shared-room! store "General")]
      (is (user/accessible-room? store (:user/id created-user) (:room/id shared-room)))))

  (testing "given a shared room, when listing rooms, then it is available for later participation flows"
    (let [store (user/create-store)
          shared-room (user/create-shared-room! store "General")]
      (is (= [shared-room]
             (user/list-rooms store))))))

(deftest list-accessible-rooms
  (testing "given shared rooms and user rooms, when listing rooms for a user, then it includes accessible rooms with active participation"
    (let [store (user/create-store)
          andres (user/create-user! store "andres" :user.type/human)
          zoe (user/create-user! store "zoe" :user.type/human)
          general-room (user/create-shared-room! store "General")
          random-room (user/create-shared-room! store "Random")]
      (user/join-room! store (:user/id andres) (:room/id general-room))
      (is (= [#:room{:id (:room/id general-room)
                     :type :room.type/shared
                     :title "General"
                     :active? true}
              #:room{:id (:room/id random-room)
                     :type :room.type/shared
                     :title "Random"
                     :active? false}
              #:room{:id "room:user:andres"
                     :type :room.type/user
                     :title "andres"
                     :active? true}]
             (user/list-accessible-rooms store (:user/id andres))))
      (is (not-any? #(= (str "room:" (:user/id zoe)) (:room/id %))
                    (user/list-accessible-rooms store (:user/id andres)))))))

(deftest participate-in-room
  (testing "given an accessible shared room, when a user joins, then the user becomes an active participant"
    (let [store (user/create-store)
          created-user (user/create-user! store "andres" :user.type/human)
          shared-room (user/create-shared-room! store "General")]
      (user/join-room! store (:user/id created-user) (:room/id shared-room))
      (is (user/active-participant? store (:user/id created-user) (:room/id shared-room)))))

  (testing "given an active participant, when joining again, then participation is not duplicated"
    (let [store (user/create-store)
          created-user (user/create-user! store "andres" :user.type/human)
          shared-room (user/create-shared-room! store "General")]
      (user/join-room! store (:user/id created-user) (:room/id shared-room))
      (user/join-room! store (:user/id created-user) (:room/id shared-room))
      (is (= 1
             (user/active-participation-count store (:user/id created-user) (:room/id shared-room))))))

  (testing "given a participant with messages, when reading the room, then messages are ordered by sequence"
    (let [store (user/create-store)
          created-user (user/create-user! store "andres" :user.type/human)
          shared-room (user/create-shared-room! store "General")]
      (user/join-room! store (:user/id created-user) (:room/id shared-room))
      (user/add-message! store (:room/id shared-room) (:user/id created-user) 3 "Tres")
      (user/add-message! store (:room/id shared-room) (:user/id created-user) 1 "Uno")
      (user/add-message! store (:room/id shared-room) (:user/id created-user) 2 "Dos")
      (is (= [1 2 3]
             (mapv :message/sequence
                   (user/read-room store (:user/id created-user) (:room/id shared-room)))))))

  (testing "given sent messages in two rooms, when reading each room, then sequences are monotonic per room and Markdown is preserved"
    (let [store (user/create-store)
          created-user (user/create-user! store "andres" :user.type/human)
          general-room (user/create-shared-room! store "General")
          random-room (user/create-shared-room! store "Random")]
      (user/join-room! store (:user/id created-user) (:room/id general-room))
      (user/join-room! store (:user/id created-user) (:room/id random-room))
      (user/send-message! store (:user/id created-user) (:room/id general-room) "Uno **general**")
      (user/send-message! store (:user/id created-user) (:room/id random-room) "Uno _random_")
      (user/send-message! store (:user/id created-user) (:room/id general-room) "Dos `general`")
      (is (= [[1 "Uno **general**"] [2 "Dos `general`"]]
             (mapv (juxt :message/sequence :message/body-markdown)
                   (user/read-room store (:user/id created-user) (:room/id general-room)))))
      (is (= [[1 "Uno _random_"]]
             (mapv (juxt :message/sequence :message/body-markdown)
                   (user/read-room store (:user/id created-user) (:room/id random-room)))))))

  (testing "given an active participant, when sending a message, then the room keeps the original Markdown"
    (let [store (user/create-store)
          created-user (user/create-user! store "andres" :user.type/human)
          shared-room (user/create-shared-room! store "General")]
      (user/join-room! store (:user/id created-user) (:room/id shared-room))
      (is (= #:message{:id "message:room:shared:general:1"
                       :room-id (:room/id shared-room)
                       :author-id (:user/id created-user)
                       :sequence 1
                       :body-markdown "Hola **mundo**"}
             (user/send-message! store (:user/id created-user) (:room/id shared-room) "Hola **mundo**")))))

  (testing "given an active participant, when sending a message, then a message-created event is recorded"
    (let [store (user/create-store)
          created-user (user/create-user! store "andres" :user.type/human)
          shared-room (user/create-shared-room! store "General")]
      (user/join-room! store (:user/id created-user) (:room/id shared-room))
      (let [message (user/send-message! store (:user/id created-user) (:room/id shared-room) "Hola **mundo**")]
        (is (= [#:event{:id "event:message:room:shared:general:1:created"
                        :type :message/created
                        :room-id (:room/id shared-room)
                        :actor-id (:user/id created-user)
                        :message-id (:message/id message)}]
               (user/list-message-events store (:room/id shared-room)))))))

  (testing "given a repeated client transaction, when sending again, then it returns the same message without duplicate events"
    (let [store (user/create-store)
          created-user (user/create-user! store "andres" :user.type/human)
          shared-room (user/create-shared-room! store "General")]
      (user/join-room! store (:user/id created-user) (:room/id shared-room))
      (let [first-send (user/send-message! store
                                           (:user/id created-user)
                                           (:room/id shared-room)
                                           "Hola **mundo**"
                                           "client-txn-1")
            retry-send (user/send-message! store
                                           (:user/id created-user)
                                           (:room/id shared-room)
                                           "Hola **mundo**"
                                           "client-txn-1")]
        (is (= first-send retry-send))
        (is (= [first-send]
               (user/read-room store (:user/id created-user) (:room/id shared-room))))
        (is (= 1
               (count (user/list-message-events store (:room/id shared-room))))))))

  (testing "given a user who is not an active participant, when reading or sending, then access is denied"
    (let [store (user/create-store)
          created-user (user/create-user! store "andres" :user.type/human)
          shared-room (user/create-shared-room! store "General")]
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"active participation required"
                            (user/read-room store (:user/id created-user) (:room/id shared-room))))
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"active participation required"
                            (user/send-message! store (:user/id created-user) (:room/id shared-room) "Hola")))))

  (testing "given an active participant, when leaving and rejoining, then access follows active participation"
    (let [store (user/create-store)
          created-user (user/create-user! store "andres" :user.type/human)
          shared-room (user/create-shared-room! store "General")]
      (user/join-room! store (:user/id created-user) (:room/id shared-room))
      (user/leave-room! store (:user/id created-user) (:room/id shared-room))
      (is (not (user/active-participant? store (:user/id created-user) (:room/id shared-room))))
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"active participation required"
                            (user/read-room store (:user/id created-user) (:room/id shared-room))))
      (user/join-room! store (:user/id created-user) (:room/id shared-room))
      (is (= []
             (user/read-room store (:user/id created-user) (:room/id shared-room)))))))

(deftest export-room-as-markdown
  (testing "given out-of-order messages, when anyone exports a room, then it includes the title and ordered original Markdown"
    (let [store (user/create-store)
          created-user (user/create-user! store "andres" :user.type/human)
          shared-room (user/create-shared-room! store "General")]
      (user/add-message! store (:room/id shared-room) (:user/id created-user) 2 "Segundo con `código`")
      (user/add-message! store (:room/id shared-room) (:user/id created-user) 1 "Primero **fuerte**")
      (is (= "# General\n\n## Mensajes\n\n### Mensaje 1\n\nPrimero **fuerte**\n\n### Mensaje 2\n\nSegundo con `código`\n"
             (user/export-room-markdown store (:room/id shared-room))))))

  (testing "given a user who is not an active participant, when exporting a room, then export is allowed"
    (let [store (user/create-store)
          created-user (user/create-user! store "andres" :user.type/human)
          shared-room (user/create-shared-room! store "General")]
      (user/add-message! store (:room/id shared-room) (:user/id created-user) 1 "Público")
      (is (= "# General\n\n## Mensajes\n\n### Mensaje 1\n\nPúblico\n"
             (user/export-room-markdown store (:room/id shared-room)))))))

(deftest list-users-in-store
  (testing "given users created out of order, when listing users, then it returns them ordered by handle"
    (let [store (user/create-store)
          zoe (user/create-user! store "zoe" :user.type/human)
          andres (user/create-user! store "andres" :user.type/human)]
      (is (= [andres zoe]
             (user/list-users store))))))
