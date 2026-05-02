(ns pi-clojure.web-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [pi-clojure.domain.user :as user]
            [pi-clojure.web :as web]))

(deftest render-home-page
  (testing "given a store with a shared room and message, when rendering the web home, then it shows a safe conversation and publish form"
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
        (is (str/includes? html "<form method=\"post\" action=\"/rooms\""))
        (is (str/includes? html "name=\"title\""))
        (is (str/includes? html "<form method=\"post\" action=\"/messages\""))
        (is (str/includes? html "name=\"handle\""))
        (is (str/includes? html "name=\"body-markdown\""))
        (is (str/includes? html "name=\"client-txn-id\"")))))

  (testing "given an empty store, when rendering the web home, then it shows a clear empty state"
    (let [html (web/render-home-page (user/create-store))]
      (is (str/includes? html "No hay salas disponibles")))))

(deftest create-shared-room-from-web
  (testing "given an empty store, when posting a new shared room, then it persists and renders it"
    (let [store (user/create-store)
          {:keys [status html]} (web/create-room! store {"title" "General"})]
      (is (= 200 status))
      (is (= [#:room{:id "room:shared:general"
                     :type :room.type/shared
                     :title "General"}]
             (user/list-rooms store)))
      (is (str/includes? html "Sala creada"))
      (is (str/includes? html "General"))))

  (testing "given an invalid or duplicated room title, when posting, then it shows a safe error without changing rooms"
    (let [store (user/create-store)
          _ (user/create-shared-room! store "General")]
      (let [{:keys [status html]} (web/create-room! store {"title" "General"})]
        (is (= 400 status))
        (is (str/includes? html "No se pudo crear la sala"))
        (is (= ["General"] (mapv :room/title (user/list-rooms store)))))
      (let [{:keys [status html]} (web/create-room! store {"title" "<script>alert(1)</script>"})]
        (is (= 400 status))
        (is (str/includes? html "No se pudo crear la sala"))
        (is (not (str/includes? html "<script>alert(1)</script>")))
        (is (= ["General"] (mapv :room/title (user/list-rooms store))))))))

(deftest publish-message-from-web
  (testing "given a valid post, when publishing from the web, then it persists and renders the new escaped message"
    (let [store (user/create-store)
          andres (user/create-user! store "andres" :user.type/human)
          room (user/create-shared-room! store "General")]
      (user/join-room! store (:user/id andres) (:room/id room))
      (let [{:keys [status html]} (web/publish-message! store {"room-id" (:room/id room)
                                                               "handle" "andres"
                                                               "body-markdown" "Hola **web** & equipo"
                                                               "client-txn-id" "web-txn-1"})]
        (is (= 200 status))
        (is (str/includes? html "Mensaje publicado"))
        (is (str/includes? html "Hola **web** &amp; equipo"))
        (is (= ["Hola **web** & equipo"]
               (mapv :message/body-markdown
                     (user/read-room store (:user/id andres) (:room/id room))))))))

  (testing "given an invalid post, when publishing from the web, then it shows a safe error and does not persist"
    (let [store (user/create-store)
          andres (user/create-user! store "andres" :user.type/human)
          room (user/create-shared-room! store "General")]
      (user/join-room! store (:user/id andres) (:room/id room))
      (let [{:keys [status html]} (web/publish-message! store {"room-id" (:room/id room)
                                                               "handle" "andres"
                                                               "body-markdown" "<script>alert(1)</script>"
                                                               "client-txn-id" "bad-txn"})]
        (is (= 400 status))
        (is (str/includes? html "No se pudo publicar"))
        (is (not (str/includes? html "<script>alert(1)</script>")))
        (is (= []
               (user/read-room store (:user/id andres) (:room/id room))))))))
