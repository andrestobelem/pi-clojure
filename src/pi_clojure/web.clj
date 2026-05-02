(ns pi-clojure.web
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [pi-clojure.domain.user :as chat])
  (:import [com.sun.net.httpserver HttpExchange HttpHandler HttpServer]
           [java.net InetSocketAddress]))

(def default-port 8080)
(def default-state-file ".pi-chat.edn")

(defn state-file-from-env []
  (or (System/getenv "PI_CHAT_STATE_FILE")
      default-state-file))

(defn html-escape [value]
  (-> (str value)
      (str/replace "&" "&amp;")
      (str/replace "<" "&lt;")
      (str/replace ">" "&gt;")
      (str/replace "\"" "&quot;")
      (str/replace "'" "&#39;")))

(defn load-store [state-file]
  (let [file (io/file state-file)]
    (if (.exists file)
      (atom (edn/read-string (slurp file)))
      (chat/create-store))))

(defn shared-room? [room]
  (= :room.type/shared (:room/type room)))

(defn author-handle [store message]
  (or (:user/handle (chat/find-by-id store (:message/author-id message)))
      "usuario-desconocido"))

(defn render-message [store message]
  (str "<li class=\"message\">"
       "<span class=\"message__author\">" (html-escape (author-handle store message)) "</span>"
       "<pre class=\"message__body\">" (html-escape (:message/body-markdown message)) "</pre>"
       "</li>"))

(defn render-room [store room]
  (let [messages (sort-by :message/sequence
                          (chat/messages-in-room store (:room/id room)))]
    (str "<section class=\"room\">"
         "<h2>" (html-escape (:room/title room)) "</h2>"
         (if (seq messages)
           (str "<ol class=\"messages\">"
                (str/join (map #(render-message store %) messages))
                "</ol>")
           "<p class=\"empty\">No hay mensajes todavía.</p>")
         "</section>")))

(defn render-rooms [store]
  (let [rooms (->> (chat/list-rooms store)
                   (filter shared-room?)
                   (sort-by :room/title))]
    (if (seq rooms)
      (str/join "\n" (map #(render-room store %) rooms))
      "<p class=\"empty\">No hay salas disponibles.</p>")))

(defn render-home-page [store]
  (str "<!doctype html>\n"
       "<html lang=\"es\">\n"
       "<head>\n"
       "<meta charset=\"utf-8\">\n"
       "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n"
       "<title>pi-clojure</title>\n"
       "<style>body{font-family:system-ui,sans-serif;max-width:56rem;margin:2rem auto;padding:0 1rem;}pre{white-space:pre-wrap;background:#f6f8fa;padding:1rem;border-radius:.5rem}.message__author{font-weight:700}.room{border-top:1px solid #ddd;margin-top:2rem}</style>\n"
       "</head>\n"
       "<body>\n"
       "<header><h1>pi-clojure</h1><p>Interfaz web local de lectura.</p></header>\n"
       "<main>\n"
       (render-rooms store)
       "\n</main>\n"
       "</body>\n"
       "</html>\n"))

(defn send-response! [^HttpExchange exchange status body]
  (let [bytes (.getBytes body "UTF-8")]
    (.add (.getResponseHeaders exchange) "Content-Type" "text/html; charset=utf-8")
    (.sendResponseHeaders exchange status (alength bytes))
    (with-open [out (.getResponseBody exchange)]
      (.write out bytes))))

(defn home-handler [state-file]
  (reify HttpHandler
    (handle [_ exchange]
      (if (= "/" (.getPath (.getRequestURI ^HttpExchange exchange)))
        (send-response! exchange 200 (render-home-page (load-store state-file)))
        (send-response! exchange 404 "<!doctype html><h1>404</h1>")))))

(defn start-server! [{:keys [port state-file]
                      :or {port default-port}}]
  (let [server (HttpServer/create (InetSocketAddress. port) 0)]
    (.createContext server "/" (home-handler state-file))
    (.setExecutor server nil)
    (.start server)
    server))

(defn -main [& args]
  (let [port (if-let [port-arg (first args)]
               (parse-long port-arg)
               default-port)
        state-file (state-file-from-env)]
    (start-server! {:port port :state-file state-file})
    (println (str "Interfaz web pi-clojure en http://localhost:" port))
    (println (str "Estado: " state-file))))
