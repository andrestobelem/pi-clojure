(ns pi-clojure.domain.markdown
  (:require [clojure.string :as str]))

(def max-message-markdown-length 4000)
(def readability-message-markdown-length 1200)

(def raw-html-pattern #"(?is)<!--.*?-->|<\s*/?\s*[a-z][^>]*>")
(def markdown-image-pattern #"!\[[^\]]*\]\(")
(def markdown-link-destination-pattern #"(?i)(?<!!)\[[^\]]*\]\(\s*([^\s)]*)")

(def required-backlog-message-headings
  ["### Fricción observada"
   "### Historia candidata"
   "### Criterios de aceptación"
   "### Primer test rojo sugerido"])

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

(defn missing-backlog-heading-error [heading]
  #:error{:type :backlog-message/missing-heading
          :message (str "Falta encabezado requerido: " heading)
          :path [:message/body]
          :heading heading})

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

(defn code-fence-line? [line]
  (str/starts-with? (str/trim line) "```"))

(defn code-fence-without-language-line? [line]
  (= "```" (str/trim line)))

(defn contains-code-block-without-language? [body-markdown]
  (loop [[line & remaining] (str/split-lines body-markdown)
         inside-code-block? false]
    (cond
      (nil? line) false
      (and (not inside-code-block?)
           (code-fence-without-language-line? line)) true
      (code-fence-line? line) (recur remaining (not inside-code-block?))
      :else (recur remaining inside-code-block?))))

(defn very-long-readable-message? [body-markdown]
  (<= readability-message-markdown-length
      (count body-markdown)
      max-message-markdown-length))

(defn code-block-without-language-warning []
  #:warning{:type :markdown/code-block-without-language
            :severity :warning.severity/info
            :path [:message/body]})

(defn very-long-warning [body-markdown]
  #:warning{:type :markdown/very-long
            :severity :warning.severity/info
            :path [:message/body]
            :limit readability-message-markdown-length
            :actual (count body-markdown)})

(defn lint-message-markdown [body-markdown]
  (cond-> []
    (and (string? body-markdown)
         (contains-code-block-without-language? body-markdown))
    (conj (code-block-without-language-warning))

    (and (string? body-markdown)
         (very-long-readable-message? body-markdown))
    (conj (very-long-warning body-markdown))))

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

(defn missing-backlog-headings [body-markdown]
  (remove #(str/includes? body-markdown %) required-backlog-message-headings))

(defn validate-backlog-message [body-markdown]
  (let [{base-errors :errors} (validate-message-markdown body-markdown)
        heading-errors (when (string? body-markdown)
                         (map missing-backlog-heading-error
                              (missing-backlog-headings body-markdown)))
        errors (concat base-errors heading-errors)]
    (if (empty? errors)
      (valid-result body-markdown)
      (error-result errors))))

(defn validate-backlog-message! [body-markdown]
  (let [result (validate-backlog-message body-markdown)]
    (when-not (:valid? result)
      (throw (ex-info (validation-error-message result) result)))
    body-markdown))
