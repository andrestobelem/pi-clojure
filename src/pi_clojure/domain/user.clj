(ns pi-clojure.domain.user
  (:require [clojure.string :as str]))

(defn create-human [handle]
  #:user{:handle handle
         :type :user.type/human})

(defn create-store []
  (atom #:users{:by-handle {}}))

(defn find-by-handle [store handle]
  (get-in @store [:users/by-handle handle]))

(defn create-human! [store handle]
  (when (str/blank? handle)
    (throw (ex-info "handle is required" {:handle handle})))
  (let [human-user (create-human handle)]
    (when (find-by-handle store handle)
      (throw (ex-info "handle already exists" {:handle handle})))
    (swap! store assoc-in [:users/by-handle handle] human-user)
    human-user))
