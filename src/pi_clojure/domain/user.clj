(ns pi-clojure.domain.user
  (:require [clojure.string :as str]))

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

(defn create-human! [store handle]
  (when (str/blank? handle)
    (throw (ex-info "handle is required" {:handle handle})))
  (let [normalized-handle (-> handle str/trim str/lower-case)
        human-user (create-human normalized-handle)]
    (when-not (re-matches #"[a-z0-9_-]+" normalized-handle)
      (throw (ex-info "handle has unsupported characters"
                      {:handle normalized-handle})))
    (when (find-by-handle store normalized-handle)
      (throw (ex-info "handle already exists" {:handle normalized-handle})))
    (swap! store assoc-in [:users/by-handle normalized-handle] human-user)
    human-user))
