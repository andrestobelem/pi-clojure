(ns pi-clojure.domain.model-test
  (:require [clojure.test :refer [deftest is testing]]
            [pi-clojure.domain.model :as model]))

(deftest minimal-domain-model
  (testing "given user data, when creating a user model, then it keeps id, handle and type"
    (is (= #:user{:id "user-1"
                  :handle "andres"
                  :type :user.type/human}
           (model/create-user "user-1" "andres" :user.type/human))))

  (testing "given room data, when creating rooms, then it distinguishes user and shared rooms"
    (is (= #:room{:id "room-1"
                  :type :room.type/user
                  :title "Andres"
                  :owner-id "user-1"}
           (model/create-room "room-1" :room.type/user "Andres" "user-1")))
    (is (= #:room{:id "room-2"
                  :type :room.type/shared
                  :title "General"}
           (model/create-room "room-2" :room.type/shared "General" nil))))

  (testing "given user and room ids, when creating a participation, then it represents active membership"
    (is (= #:participation{:user-id "user-1"
                           :room-id "room-1"
                           :active? true}
           (model/create-participation "user-1" "room-1"))))

  (testing "given message data, when creating a message, then it keeps room, author, sequence and Markdown source"
    (is (= #:message{:id "message-1"
                     :room-id "room-1"
                     :author-id "user-1"
                     :sequence 1
                     :body-markdown "Hola **mundo**"}
           (model/create-message "message-1" "room-1" "user-1" 1 "Hola **mundo**"))))

  (testing "given a message, when creating a message-created event, then it keeps the created message payload"
    (let [message (model/create-message "message-1" "room-1" "user-1" 1 "Hola **mundo**")]
      (is (= #:event{:id "event-1"
                     :type :message/created
                     :room-id "room-1"
                     :actor-id "user-1"
                     :message message}
             (model/create-message-created-event "event-1" message))))))
