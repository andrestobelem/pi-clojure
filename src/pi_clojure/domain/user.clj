(ns pi-clojure.domain.user
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [pi-clojure.domain.markdown :as markdown]))

(def min-handle-length 3)
(def max-handle-length 39)
(def allowed-handle-pattern #"[a-z0-9_-]+")
(def edge-separator-pattern #"(^[-_]|[-_]$)")
(def consecutive-separator-pattern #"[-_]{2}")
(def reserved-handles #{"admin"
                        "api"
                        "root"
                        "support"
                        "www"})

(defn required-handle? [handle]
  (and (string? handle)
       (not (str/blank? handle))))

(defn without-surrounding-whitespace? [handle]
  (= handle (str/trim handle)))

(defn lowercase-handle? [handle]
  (= handle (str/lower-case handle)))

(defn handle-length-valid? [handle]
  (<= min-handle-length (count handle) max-handle-length))

(defn supported-handle-characters? [handle]
  (boolean (re-matches allowed-handle-pattern handle)))

(defn without-edge-separators? [handle]
  (not (re-find edge-separator-pattern handle)))

(defn without-consecutive-separators? [handle]
  (not (re-find consecutive-separator-pattern handle)))

(defn available-handle-name? [handle]
  (not (contains? reserved-handles handle)))

(defn valid-handle? [handle]
  (and (required-handle? handle)
       (without-surrounding-whitespace? handle)
       (lowercase-handle? handle)
       (handle-length-valid? handle)
       (supported-handle-characters? handle)
       (without-edge-separators? handle)
       (without-consecutive-separators? handle)
       (available-handle-name? handle)))

(def user-types #{:user.type/human
                  :user.type/agent})

(s/def :user/handle valid-handle?)
(s/def :user/type user-types)
(s/def :user/user (s/keys :req [:user/handle :user/type]))

(defn create-user [handle user-type]
  #:user{:handle handle
         :type user-type})

(defn create-store []
  (atom #:users{:by-handle {}
        :users/by-id {}
        :rooms/by-id {}
        :rooms/personal-by-owner-id {}
        :participations/active #{}
        :messages/by-room-id {}
        :messages/by-client-txn-id {}
        :events/by-room-id {}}))

(defn find-by-handle [store handle]
  (get-in @store [:users/by-handle handle]))

(defn find-by-id [store user-id]
  (get-in @store [:users/by-id user-id]))

(defn list-users [store]
  (->> (get-in @store [:users/by-handle])
       (sort-by key)
       (mapv val)))

(defn find-room [store room-id]
  (get-in @store [:rooms/by-id room-id]))

(defn find-personal-room-by-owner [store owner-id]
  (when-let [room-id (get-in @store [:rooms/personal-by-owner-id owner-id])]
    (find-room store room-id)))

(defn list-rooms [store]
  (->> (get-in @store [:rooms/by-id])
       (sort-by key)
       (mapv val)))

(defn active-participant? [store user-id room-id]
  (contains? (get-in @store [:participations/active]) [user-id room-id]))

(defn room-list-item [store user-id room]
  #:room{:id (:room/id room)
         :type (:room/type room)
         :title (:room/title room)
         :active? (active-participant? store user-id (:room/id room))})

(defn active-participation-count [store user-id room-id]
  (if (active-participant? store user-id room-id) 1 0))

(defn existing-user-id? [store user-id]
  (boolean (find-by-id store user-id)))

(defn accessible-room? [store user-id room-id]
  (let [room (find-room store room-id)]
    (and (existing-user-id? store user-id)
         (or (= :room.type/shared (:room/type room))
             (= user-id (:room/owner-id room))))))

(defn list-accessible-rooms [store user-id]
  (->> (list-rooms store)
       (filter #(accessible-room? store user-id (:room/id %)))
       (mapv #(room-list-item store user-id %))))

(defn validate-handle! [handle]
  (when-not (required-handle? handle)
    (throw (ex-info "handle is required" {:handle handle})))
  (when-not (without-surrounding-whitespace? handle)
    (throw (ex-info "handle cannot have surrounding whitespace"
                    {:handle handle})))
  (when-not (lowercase-handle? handle)
    (throw (ex-info "handle must be lowercase" {:handle handle})))
  (when (< (count handle) min-handle-length)
    (throw (ex-info "handle is too short" {:handle handle})))
  (when (> (count handle) max-handle-length)
    (throw (ex-info "handle is too long" {:handle handle})))
  (when-not (supported-handle-characters? handle)
    (throw (ex-info "handle has unsupported characters" {:handle handle})))
  (when-not (without-edge-separators? handle)
    (throw (ex-info "handle cannot start or end with a separator"
                    {:handle handle})))
  (when-not (without-consecutive-separators? handle)
    (throw (ex-info "handle cannot contain consecutive separators"
                    {:handle handle})))
  (when-not (available-handle-name? handle)
    (throw (ex-info "handle is reserved" {:handle handle}))))

(defn user-id-for-handle [handle]
  (str "user:" handle))

(defn persist-user [state created-user]
  (-> state
      (assoc-in [:users/by-handle (:user/handle created-user)] created-user)
      (assoc-in [:users/by-id (:user/id created-user)] created-user)))

(defn room-slug-for-title [title]
  (-> title
      str/lower-case
      (str/replace #"\s+" "-")))

(defn create-shared-room! [store title]
  (let [room #:room{:id (str "room:shared:" (room-slug-for-title title))
                    :type :room.type/shared
                    :title title}]
    (swap! store assoc-in [:rooms/by-id (:room/id room)] room)
    room))

(defn join-room! [store user-id room-id]
  (when-not (accessible-room? store user-id room-id)
    (throw (ex-info "room is not accessible"
                    {:user-id user-id
                     :room-id room-id})))
  (swap! store update-in [:participations/active] conj [user-id room-id])
  #:participation{:user-id user-id
                  :room-id room-id
                  :active? true})

(defn leave-room! [store user-id room-id]
  (swap! store update-in [:participations/active] disj [user-id room-id])
  #:participation{:user-id user-id
                  :room-id room-id
                  :active? false})

(defn require-active-participant! [store user-id room-id]
  (when-not (active-participant? store user-id room-id)
    (throw (ex-info "active participation required"
                    {:user-id user-id
                     :room-id room-id}))))

(defn messages-in-room [store room-id]
  (get-in @store [:messages/by-room-id room-id] []))

(defn message-txn-key [author-id client-txn-id]
  [author-id client-txn-id])

(defn find-message-by-client-txn-id [store author-id client-txn-id]
  (get-in @store [:messages/by-client-txn-id (message-txn-key author-id client-txn-id)]))

(defn add-message! [store room-id author-id sequence body-markdown & [client-txn-id]]
  (let [message (cond-> #:message{:id (str "message:" room-id ":" sequence)
                                  :room-id room-id
                                  :author-id author-id
                                  :sequence sequence
                                  :body-markdown body-markdown}
                  client-txn-id
                  (assoc :message/client-txn-id client-txn-id))]
    (swap! store
           (fn [state]
             (cond-> (update-in state [:messages/by-room-id room-id] (fnil conj []) message)
               client-txn-id
               (assoc-in [:messages/by-client-txn-id
                          (message-txn-key author-id client-txn-id)]
                         message))))
    message))

(defn message-created-event [message]
  #:event{:id (str "event:" (:message/id message) ":created")
          :type :message/created
          :room-id (:message/room-id message)
          :actor-id (:message/author-id message)
          :message-id (:message/id message)})

(defn record-message-created-event! [store message]
  (let [event (message-created-event message)]
    (swap! store update-in [:events/by-room-id (:event/room-id event)] (fnil conj []) event)
    event))

(defn list-message-events [store room-id]
  (get-in @store [:events/by-room-id room-id] []))

(defn read-room [store user-id room-id]
  (require-active-participant! store user-id room-id)
  (->> (messages-in-room store room-id)
       (sort-by :message/sequence)
       vec))

(defn export-room-markdown [store room-id]
  (let [room (find-room store room-id)
        messages (->> (messages-in-room store room-id)
                      (sort-by :message/sequence))]
    (str "# " (:room/title room) "\n\n"
         "## Mensajes\n\n"
         (str/join "\n\n"
                   (map (fn [message]
                          (str "### Mensaje " (:message/sequence message) "\n\n"
                               (:message/body-markdown message)))
                        messages))
         "\n")))

(defn next-message-sequence [store room-id]
  (inc (reduce max 0 (map :message/sequence (messages-in-room store room-id)))))

(defn create-message! [store user-id room-id body-markdown client-txn-id]
  (markdown/validate-message-markdown! body-markdown)
  (let [message (add-message! store
                              room-id
                              user-id
                              (next-message-sequence store room-id)
                              body-markdown
                              client-txn-id)]
    (record-message-created-event! store message)
    message))

(defn send-message!
  ([store user-id room-id body-markdown]
   (require-active-participant! store user-id room-id)
   (create-message! store user-id room-id body-markdown nil))
  ([store user-id room-id body-markdown client-txn-id]
   (require-active-participant! store user-id room-id)
   (or (find-message-by-client-txn-id store user-id client-txn-id)
       (create-message! store user-id room-id body-markdown client-txn-id))))

(defn personal-room-id-for-user [created-user]
  (str "room:" (:user/id created-user)))

(defn ensure-personal-room! [store created-user]
  (or (find-personal-room-by-owner store (:user/id created-user))
      (let [personal-room #:room{:id (personal-room-id-for-user created-user)
                                 :type :room.type/user
                                 :title (:user/handle created-user)
                                 :owner-id (:user/id created-user)
                                 :visibility :room.visibility/private}]
        (swap! store
               (fn [state]
                 (-> state
                     (assoc-in [:rooms/by-id (:room/id personal-room)] personal-room)
                     (assoc-in [:rooms/personal-by-owner-id (:user/id created-user)]
                               (:room/id personal-room))
                     (update-in [:participations/active]
                                conj
                                [(:user/id created-user)
                                 (:room/id personal-room)]))))
        personal-room)))

(defn create-user! [store handle user-type]
  (validate-handle! handle)
  (let [created-user (assoc (create-user handle user-type)
                            :user/id
                            (user-id-for-handle handle))]
    (when (find-by-handle store handle)
      (throw (ex-info "handle already exists" {:handle handle})))
    (swap! store persist-user created-user)
    (when (= :user.type/human user-type)
      (ensure-personal-room! store created-user))
    created-user))
