(ns pi-clojure.domain.model)

(defn create-user [id handle user-type]
  #:user{:id id
         :handle handle
         :type user-type})

(defn create-room [id room-type title owner-id]
  (cond-> #:room{:id id
                 :type room-type
                 :title title}
    owner-id (assoc :room/owner-id owner-id)))

(defn create-participation [user-id room-id]
  #:participation{:user-id user-id
                  :room-id room-id
                  :active? true})

(defn create-message [id room-id author-id sequence body-markdown]
  #:message{:id id
            :room-id room-id
            :author-id author-id
            :sequence sequence
            :body-markdown body-markdown})

(defn create-message-created-event [id message]
  #:event{:id id
          :type :message/created
          :room-id (:message/room-id message)
          :actor-id (:message/author-id message)
          :message message})
