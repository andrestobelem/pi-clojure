(ns pi-clojure.domain.user
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str]))

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
        :rooms/by-id {}
        :rooms/personal-by-owner-id {}
        :participations/active #{}}))

(defn find-by-handle [store handle]
  (get-in @store [:users/by-handle handle]))

(defn list-users [store]
  (->> (get-in @store [:users/by-handle])
       (sort-by key)
       (mapv val)))

(defn find-user-by-id [store user-id]
  (some #(when (= user-id (:user/id %)) %)
        (list-users store)))

(defn find-room-by-id [store room-id]
  (get-in @store [:rooms/by-id room-id]))

(defn find-personal-room-by-owner [store owner-id]
  (when-let [room-id (get-in @store [:rooms/personal-by-owner-id owner-id])]
    (find-room-by-id store room-id)))

(defn list-rooms [store]
  (->> (get-in @store [:rooms/by-id])
       (sort-by key)
       (mapv val)))

(defn active-participant? [store user-id room-id]
  (contains? (get-in @store [:participations/active]) [user-id room-id]))

(defn list-active-participations [store]
  (->> (get-in @store [:participations/active])
       sort
       vec))

(defn shared-room? [room]
  (= :room.type/shared (:room/type room)))

(defn owned-by? [user-id room]
  (= user-id (:room/owner-id room)))

(defn accessible-room? [user-id room]
  (or (shared-room? room)
      (owned-by? user-id room)))

(defn list-accessible-rooms [store user-id]
  (->> (list-rooms store)
       (filter #(accessible-room? user-id %))
       vec))

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

(defn personal-room-id-for-user [created-user]
  (str "room:" (:user/id created-user)))

(defn create-shared-room! [store room-id title]
  (let [room #:room{:id room-id
                    :type :room.type/shared
                    :title title
                    :visibility :room.visibility/shared}]
    (swap! store assoc-in [:rooms/by-id room-id] room)
    room))

(defn join-room! [store user-id room-id]
  (let [room (find-room-by-id store room-id)]
    (when-not (find-user-by-id store user-id)
      (throw (ex-info "user does not exist" {:user-id user-id})))
    (when-not room
      (throw (ex-info "room does not exist" {:room-id room-id})))
    (when-not (accessible-room? user-id room)
      (throw (ex-info "room is not accessible" {:user-id user-id
                                                 :room-id room-id})))
    (swap! store update-in [:participations/active] conj [user-id room-id])
    (find-room-by-id store room-id)))

(defn leave-room! [store user-id room-id]
  (swap! store update-in [:participations/active] disj [user-id room-id])
  nil)

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
    (swap! store assoc-in [:users/by-handle handle] created-user)
    (when (= :user.type/human user-type)
      (ensure-personal-room! store created-user))
    created-user))
