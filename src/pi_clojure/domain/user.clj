(ns pi-clojure.domain.user)

(defn create-human [handle]
  #:user{:handle handle
         :type :user.type/human})

(defn create-store []
  (atom {}))

(defn find-by-handle [store handle]
  (get @store handle))

(defn create-human! [store handle]
  (let [human-user (create-human handle)]
    (when (find-by-handle store handle)
      (throw (ex-info "handle already exists" {:handle handle})))
    (swap! store assoc handle human-user)
    human-user))
