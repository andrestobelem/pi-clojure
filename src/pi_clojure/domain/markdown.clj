(ns pi-clojure.domain.markdown
  (:require [clojure.string :as str]))

(def max-message-markdown-length 4000)

(def raw-html-pattern #"(?is)<!--.*?-->|<\s*/?\s*[a-z][^>]*>")
(def markdown-image-pattern #"!\[[^\]]*\]\(")
(def markdown-link-destination-pattern #"(?i)(?<!!)\[[^\]]*\]\(\s*([^\s)]*)")

(defn valid-result [body-markdown]
  {:valid? true
   :markdown body-markdown})

(defn error-result [errors]
  {:valid? false
   :errors (vec errors)})

(defn blank-error []
  #:error{:type :markdown/blank
          :message "El mensaje no puede estar vacío"
          :path [:message/body]})

(defn too-long-error [body-markdown]
  #:error{:type :markdown/too-long
          :message (str "El mensaje no puede superar "
                        max-message-markdown-length
                        " caracteres")
          :path [:message/body]
          :limit max-message-markdown-length
          :actual (count body-markdown)})

(defn raw-html-error []
  #:error{:type :markdown/raw-html
          :message "El mensaje contiene HTML crudo no permitido"
          :path [:message/body]})

(defn unsafe-link-error []
  #:error{:type :markdown/unsafe-link
          :message "Usá links http:// o https://; otros protocolos no están permitidos"
          :path [:message/body]})

(defn image-not-allowed-error []
  #:error{:type :markdown/image-not-allowed
          :message "Las imágenes Markdown todavía no están permitidas; compartí un link http:// o https:// en su lugar"
          :path [:message/body]})

(defn blank-message? [body-markdown]
  (or (not (string? body-markdown))
      (str/blank? body-markdown)))

(defn too-long-message? [body-markdown]
  (> (count body-markdown) max-message-markdown-length))

(defn contains-raw-html? [body-markdown]
  (boolean (re-find raw-html-pattern body-markdown)))

(defn contains-markdown-image? [body-markdown]
  (boolean (re-find markdown-image-pattern body-markdown)))

(defn safe-link-destination? [destination]
  (boolean (re-find #"(?i)^https?://" destination)))

(defn link-destinations [body-markdown]
  (map second (re-seq markdown-link-destination-pattern body-markdown)))

(defn contains-unsafe-link? [body-markdown]
  (boolean (some (complement safe-link-destination?)
                 (link-destinations body-markdown))))

(defn validation-errors [body-markdown]
  (cond-> []
    (blank-message? body-markdown)
    (conj (blank-error))

    (and (string? body-markdown)
         (too-long-message? body-markdown))
    (conj (too-long-error body-markdown))

    (and (string? body-markdown)
         (contains-raw-html? body-markdown))
    (conj (raw-html-error))

    (and (string? body-markdown)
         (contains-markdown-image? body-markdown))
    (conj (image-not-allowed-error))

    (and (string? body-markdown)
         (contains-unsafe-link? body-markdown))
    (conj (unsafe-link-error))))

(defn validate-message-markdown [body-markdown]
  (let [errors (validation-errors body-markdown)]
    (if (empty? errors)
      (valid-result body-markdown)
      (error-result errors))))

(defn validation-error-message [result]
  (or (some-> result :errors first :error/message)
      "El mensaje Markdown no es válido"))

(defn validate-message-markdown! [body-markdown]
  (let [result (validate-message-markdown body-markdown)]
    (when-not (:valid? result)
      (throw (ex-info (validation-error-message result) result)))
    body-markdown))
