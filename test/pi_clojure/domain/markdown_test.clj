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
                              :message "El mensaje contiene un link con protocolo no permitido"
                              :path [:message/body]}]}
           (markdown/validate-message-markdown
            "Hola <script>alert(1)</script> [click](javascript:alert(1))")))))

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
