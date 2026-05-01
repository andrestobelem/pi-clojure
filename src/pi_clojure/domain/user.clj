(ns pi-clojure.domain.user
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str]))

(def min-handle-length 3)
(def max-handle-length 39)
(def allowed-handle-pattern #"[a-z0-9_-]+")
(def edge-separator-pattern #"(^[-_]|[-_]$)")

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

(defn valid-handle? [handle]
  (and (required-handle? handle)
       (without-surrounding-whitespace? handle)
       (lowercase-handle? handle)
       (handle-length-valid? handle)
       (supported-handle-characters? handle)
       (without-edge-separators? handle)))

(def user-types #{:user.type/human
                  :user.type/agent})

(s/def :user/handle valid-handle?)
(s/def :user/type user-types)
(s/def :user/user (s/keys :req [:user/handle :user/type]))

(defn create-user [handle user-type]
  #:user{:handle handle
         :type user-type})

(defn create-store []
  (atom #:users{:by-handle {}}))

(defn find-by-handle [store handle]
  (get-in @store [:users/by-handle handle]))

(defn list-users [store]
  (->> (get-in @store [:users/by-handle])
       (sort-by key)
       (mapv val)))

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
                    {:handle handle}))))

(defn create-user! [store handle user-type]
  (validate-handle! handle)
  (let [created-user (create-user handle user-type)]
    (when (find-by-handle store handle)
      (throw (ex-info "handle already exists" {:handle handle})))
    (swap! store assoc-in [:users/by-handle handle] created-user)
    created-user))
