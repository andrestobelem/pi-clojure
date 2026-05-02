(ns pi-clojure.domain.markdown-test
  (:require [clojure.test :refer [deftest is testing]]
            [pi-clojure.domain.markdown :as markdown]
            [pi-clojure.domain.user :as user]))

(deftest validate-message-markdown
  (testing "given supported Markdown, when validating a message, then it is accepted"
    (is (= {:valid? true
            :markdown "Hola **mundo** con [link](https://example.com) y `código`"}
           (markdown/validate-message-markdown
            "Hola **mundo** con [link](https://example.com) y `código`"))))

  (testing "given an empty message, when validating, then it returns a useful client error"
    (is (= {:valid? false
            :errors [#:error{:type :markdown/blank
                              :message "El mensaje no puede estar vacío"
                              :path [:message/body]}]}
           (markdown/validate-message-markdown "  \n\t  "))))

  (testing "given a message longer than the maximum, when validating, then it returns a useful client error"
    (let [too-long (apply str (repeat (inc markdown/max-message-markdown-length) "a"))]
      (is (= {:valid? false
              :errors [#:error{:type :markdown/too-long
                                :message (str "El mensaje no puede superar "
                                              markdown/max-message-markdown-length
                                              " caracteres")
                                :path [:message/body]
                                :limit markdown/max-message-markdown-length
                                :actual (count too-long)}]}
             (markdown/validate-message-markdown too-long)))))

  (testing "given raw HTML or unsafe links, when validating, then it returns structured errors"
    (is (= {:valid? false
            :errors [#:error{:type :markdown/raw-html
                              :message "El mensaje contiene HTML crudo no permitido"
                              :path [:message/body]}
                     #:error{:type :markdown/unsafe-link
                              :message "Usá links http:// o https://; otros protocolos no están permitidos"
                              :path [:message/body]}]}
           (markdown/validate-message-markdown
            "Hola <script>alert(1)</script> [click](javascript:alert(1))"))))

  (testing "given an HTML comment, when validating, then it is rejected as raw HTML"
    (is (= {:valid? false
            :errors [#:error{:type :markdown/raw-html
                              :message "El mensaje contiene HTML crudo no permitido"
                              :path [:message/body]}]}
           (markdown/validate-message-markdown "Hola <!-- secreto -->"))))

  (testing "given an http link, when validating, then it is accepted"
    (is (= {:valid? true
            :markdown "Ver [sitio](http://example.com)"}
           (markdown/validate-message-markdown "Ver [sitio](http://example.com)"))))

  (testing "given unsafe or empty link protocols, when validating, then it returns an actionable link error"
    (doseq [body ["Ver [sitio](javascript:alert(1))"
                  "Ver [sitio](data:text/html;base64,PGgxPkZvbzwvaDE+)"
                  "Ver [sitio](/interno)"
                  "Ver [sitio]()"]]
      (is (= {:valid? false
              :errors [#:error{:type :markdown/unsafe-link
                                :message "Usá links http:// o https://; otros protocolos no están permitidos"
                                :path [:message/body]}]}
             (markdown/validate-message-markdown body)))))

  (testing "given a Markdown image, when validating, then it returns an actionable image error"
    (is (= {:valid? false
            :errors [#:error{:type :markdown/image-not-allowed
                              :message "Las imágenes Markdown todavía no están permitidas; compartí un link http:// o https:// en su lugar"
                              :path [:message/body]}]}
           (markdown/validate-message-markdown "![alt](https://example.com/a.png)")))))

(deftest lint-message-markdown
  (testing "given a fenced code block without language, when linting, then it returns a non-blocking warning"
    (is (= [{:warning/type :markdown/code-block-without-language
             :warning/severity :warning.severity/info
             :warning/path [:message/body]}]
           (markdown/lint-message-markdown "```
(+ 1 1)
```"))))

  (testing "given a very long message within the hard limit, when linting and validating, then it warns without rejecting it"
    (let [long-message (apply str (repeat markdown/readability-message-markdown-length "a"))]
      (is (= [{:warning/type :markdown/very-long
               :warning/severity :warning.severity/info
               :warning/path [:message/body]
               :warning/limit markdown/readability-message-markdown-length
               :warning/actual (count long-message)}]
             (markdown/lint-message-markdown long-message)))
      (is (= {:valid? true
              :markdown long-message}
             (markdown/validate-message-markdown long-message))))))

(deftest invalid-markdown-exceptions-are-clear
  (testing "given invalid Markdown, when requiring valid Markdown, then the exception message is understandable and data is structured"
    (try
      (markdown/validate-message-markdown! "<b>Hola</b>")
      (is false "validate-message-markdown! should reject raw HTML")
      (catch clojure.lang.ExceptionInfo ex
        (is (= "El mensaje contiene HTML crudo no permitido"
               (ex-message ex)))
        (is (= {:valid? false
                :errors [#:error{:type :markdown/raw-html
                                  :message "El mensaje contiene HTML crudo no permitido"
                                  :path [:message/body]}]}
               (ex-data ex)))))))

(deftest send-message-validates-markdown-before-persisting
  (testing "given invalid Markdown, when sending a message, then it is rejected and not persisted"
    (let [store (user/create-store)
          created-user (user/create-user! store "andres" :user.type/human)
          shared-room (user/create-shared-room! store "General")]
      (user/join-room! store (:user/id created-user) (:room/id shared-room))
      (try
        (user/send-message! store (:user/id created-user) (:room/id shared-room) "<b>Hola</b>")
        (is false "send-message! should reject raw HTML")
        (catch clojure.lang.ExceptionInfo ex
          (is (= {:valid? false
                  :errors [#:error{:type :markdown/raw-html
                                    :message "El mensaje contiene HTML crudo no permitido"
                                    :path [:message/body]}]}
                 (ex-data ex)))))
      (is (= []
             (user/read-room store (:user/id created-user) (:room/id shared-room)))))))
