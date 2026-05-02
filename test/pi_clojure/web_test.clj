(ns pi-clojure.web-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [pi-clojure.domain.user :as user]
            [pi-clojure.web :as web]))

(deftest render-home-page
  (testing "given a store with a shared room and message, when rendering the web home, then it shows a read-only safe conversation"
    (let [store (user/create-store)
          andres (user/create-user! store "andres" :user.type/human)
          room (user/create-shared-room! store "General")]
      (user/join-room! store (:user/id andres) (:room/id room))
      (user/add-message! store (:room/id room) (:user/id andres) 1 "Hola <script>alert(1)</script>" "txn-1")
      (let [html (web/render-home-page store)]
        (is (str/includes? html "<!doctype html>"))
        (is (str/includes? html "pi-clojure"))
        (is (str/includes? html "General"))
        (is (str/includes? html "andres"))
        (is (str/includes? html "Hola &lt;script&gt;alert(1)&lt;/script&gt;"))
        (is (not (str/includes? html "<script>alert(1)</script>")))
        (is (not (str/includes? html "<form")))
        (is (not (str/includes? html "<input"))))))

  (testing "given an empty store, when rendering the web home, then it shows a clear empty state"
    (let [html (web/render-home-page (user/create-store))]
      (is (str/includes? html "No hay salas disponibles")))))
