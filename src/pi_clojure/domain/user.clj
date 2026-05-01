(ns pi-clojure.domain.user)

(defn create-human [handle]
  #:user{:handle handle
         :type :user.type/human})
