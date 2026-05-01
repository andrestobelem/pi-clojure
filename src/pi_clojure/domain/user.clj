(ns pi-clojure.domain.user
  (:require [clojure.string :as str]))

(def min-handle-length 3)
(def max-handle-length 39)

(defn normalize-handle [handle]
  (some-> handle str/trim str/lower-case))

(defn create-human [handle]
  #:user{:handle handle
         :type :user.type/human})

(defn create-store []
  (atom #:users{:by-handle {}}))

(defn find-by-handle [store handle]
  (get-in @store [:users/by-handle handle]))

(defn list-users [store]
  (->> (get-in @store [:users/by-handle])
       (sort-by key)
       (mapv val)))

(defn validate-handle! [handle]
  (when (str/blank? handle)
    (throw (ex-info "handle is required" {:handle handle})))
  (when (< (count handle) min-handle-length)
    (throw (ex-info "handle is too short" {:handle handle})))
  (when (> (count handle) max-handle-length)
    (throw (ex-info "handle is too long" {:handle handle})))
  (when-not (re-matches #"[a-z0-9_-]+" handle)
    (throw (ex-info "handle has unsupported characters" {:handle handle})))
  (when (re-find #"(^[-_]|[-_]$)" handle)
    (throw (ex-info "handle cannot start or end with a separator"
                    {:handle handle}))))

(defn create-human! [store handle]
  (let [normalized-handle (normalize-handle handle)
        human-user (create-human normalized-handle)]
    (validate-handle! normalized-handle)
    (when (find-by-handle store normalized-handle)
      (throw (ex-info "handle already exists" {:handle normalized-handle})))
    (swap! store assoc-in [:users/by-handle normalized-handle] human-user)
    human-user))
