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

  (testing "given any store, when rendering the web home, then it shows a create-user form"
    (let [html (web/render-home-page (user/create-store))]
      (is (str/includes? html "<form method=\"post\" action=\"/users\""))
      (is (str/includes? html "Crear usuario"))
      (is (str/includes? html "name=\"handle\""))))

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

(deftest create-user-from-web
  (testing "given an empty store, when posting a valid handle, then it creates a human user and renders a safe confirmation"
    (let [store (user/create-store)
          {:keys [status html]} (web/create-user! store {"handle" "andres"})
          created-user (user/find-by-handle store "andres")
          personal-room (user/find-personal-room-by-owner store (:user/id created-user))]
      (is (= 200 status))
      (is (= :user.type/human (:user/type created-user)))
      (is (= "andres" (:user/handle created-user)))
      (is (= (:user/id created-user) (:room/owner-id personal-room)))
      (is (str/includes? html "Usuario creado"))
      (is (str/includes? html "andres"))
      (is (not (str/includes? html ":user/id")))
      (is (not (str/includes? html "user:andres")))))

  (testing "given an invalid handle, when posting it from the web, then it shows an actionable error and does not persist"
    (let [store (user/create-store)
          before @store
          {:keys [status html]} (web/create-user! store {"handle" "Andres"})]
      (is (= 400 status))
      (is (= before @store))
      (is (str/includes? html "No se pudo crear el usuario"))
      (is (str/includes? html "Usá 3 a 39 caracteres"))))

  (testing "given a duplicate handle, when posting it from the web, then it shows an actionable error and does not create partial data"
    (let [store (user/create-store)
          existing-user (user/create-user! store "andres" :user.type/human)
          existing-room (user/find-personal-room-by-owner store (:user/id existing-user))
          before @store
          {:keys [status html]} (web/create-user! store {"handle" "andres"})]
      (is (= 400 status))
      (is (= before @store))
      (is (= existing-user (user/find-by-handle store "andres")))
      (is (= existing-room (user/find-personal-room-by-owner store (:user/id existing-user))))
      (is (str/includes? html "Ya existe un usuario con ese handle")))))

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
