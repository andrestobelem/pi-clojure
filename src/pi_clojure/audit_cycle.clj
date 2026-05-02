(ns pi-clojure.audit-cycle
  (:require [clojure.edn :as edn]
            [clojure.java.shell :as shell]
            [clojure.string :as str]))

(def max-active-items 2)

(defn story-number [branch]
  (when-let [[_ number] (re-find #"^story/(\d+)-" (or branch ""))]
    (parse-long number)))

(defn path-mentions-issue? [path issue-number]
  (boolean (re-find (re-pattern (str "(^|[^0-9])" issue-number "([^0-9]|$)"))
                    (or path ""))))

(defn conflict? [git-status]
  (some #(str/starts-with? % "UU ") (str/split-lines (or git-status ""))))

(defn dirty? [git-status]
  (not (str/blank? git-status)))

(defn git-state-label [git-status]
  (cond
    (conflict? git-status) "conflictos UU"
    (dirty? git-status) "cambios pendientes"
    :else "limpio"))

(defn worktree-for-issue [issue worktrees]
  (let [issue-number (:number issue)]
    (or (some #(when (= issue-number (story-number (:branch %))) %) worktrees)
        (some #(when (path-mentions-issue? (:path %) issue-number) %) worktrees))))

(defn issue-row [issue worktree]
  (str "#" (:number issue) " | "
       (or (:status issue) "") " | "
       (or (:foco issue) "") " | "
       (or (:branch worktree) "sin branch") " | "
       (or (:path worktree) "sin worktree") " | "
       (git-state-label (:git-status worktree))))

(defn warning-lines [issues issue-worktrees]
  (let [in-progress (filter #(= "In Progress" (:status %)) issues)
        ahora (filter #(= "Ahora" (:foco %)) issues)]
    (concat
     (when (> (count in-progress) max-active-items)
       [(str "Advertencia: hay " (count in-progress) " items en In Progress (máximo 2)")])
     (when (> (count ahora) max-active-items)
       [(str "Advertencia: hay " (count ahora) " items con Foco: Ahora (máximo 2)")])
     (mapcat
      (fn [issue]
        (let [worktree (get issue-worktrees (:number issue))]
          (cond-> []
            (and (= "In Progress" (:status issue)) (nil? worktree))
            (conj (str "Advertencia: #" (:number issue) " está In Progress sin branch/worktree local detectable"))

            (and worktree (dirty? (:git-status worktree)))
            (conj (str "Advertencia: #" (:number issue) " tiene cambios pendientes en " (:path worktree)))

            (and worktree (conflict? (:git-status worktree)))
            (conj (str "Advertencia: #" (:number issue) " tiene conflictos UU en " (:path worktree)))

            (and worktree
                 (story-number (:branch worktree))
                 (not= (:number issue) (story-number (:branch worktree))))
            (conj (str "Advertencia: worktree " (:path worktree)
                       " usa branch " (:branch worktree)
                       ", esperada para issue #" (:number issue))))))
      issues))))

(defn audit-report [snapshot]
  (let [issues (sort-by :number (:issues snapshot))
        worktrees (:worktrees snapshot)
        issue-worktrees (into {} (map (fn [issue]
                                        [(:number issue) (worktree-for-issue issue worktrees)])
                                      issues))
        rows (map #(issue-row % (get issue-worktrees (:number %))) issues)
        warnings (vec (warning-lines issues issue-worktrees))]
    {:text (str (str/join "\n" rows)
                "\n\n"
                (if (seq warnings)
                  (str/join "\n" warnings)
                  "Sin advertencias")
                "\n")
     :warnings warnings}))

(defn tsv-lines [s]
  (remove str/blank? (str/split-lines s)))

(defn parse-issue-line [line]
  (let [[number title status foco] (str/split line #"\t" -1)]
    {:number (parse-long number)
     :title title
     :status status
     :foco foco}))

(defn open-issue-numbers []
  (let [{:keys [exit out err]} (shell/sh "gh" "issue" "list"
                                         "--state" "open"
                                         "--json" "number"
                                         "--jq" ".[] | .number")]
    (when-not (zero? exit)
      (throw (ex-info (str "no se pudo leer issues abiertos: " err) {:exit exit})))
    (set (map parse-long (tsv-lines out)))))

(defn project-issues []
  (let [open-numbers (open-issue-numbers)
        {:keys [exit out err]} (shell/sh "gh" "project" "item-list" "2"
                                         "--owner" "andrestobelem"
                                         "--format" "json"
                                         "--limit" "100"
                                         "--jq" ".items[] | select(.content.type == \"Issue\") | [.content.number, .content.title, (.status // \"\"), (.foco // \"\")] | @tsv")]
    (when-not (zero? exit)
      (throw (ex-info (str "no se pudo leer GitHub Project: " err) {:exit exit})))
    (->> (tsv-lines out)
         (map parse-issue-line)
         (filter #(contains? open-numbers (:number %)))
         vec)))

(defn parse-worktree-block [block]
  (let [fields (into {}
                     (keep (fn [line]
                             (let [[k v] (str/split line #" " 2)]
                               (when v [(keyword k) v]))))
                     (str/split-lines block))]
    {:path (:worktree fields)
     :branch (some-> (:branch fields) (str/replace #"^refs/heads/" ""))}))

(defn worktree-status [worktree]
  (let [{:keys [out]} (shell/sh "git" "-C" (:path worktree) "status" "--short")]
    (assoc worktree :git-status out)))

(defn local-worktrees []
  (let [{:keys [exit out err]} (shell/sh "git" "worktree" "list" "--porcelain")]
    (when-not (zero? exit)
      (throw (ex-info (str "no se pudo leer worktrees locales: " err) {:exit exit})))
    (->> (str/split out #"\n\n")
         (remove str/blank?)
         (map parse-worktree-block)
         (filter #(some-> (:branch %) (str/starts-with? "story/")))
         (mapv worktree-status))))

(defn load-snapshot [args]
  (case (first args)
    "--fixture" (edn/read-string (slurp (second args)))
    nil {:issues (project-issues)
         :worktrees (local-worktrees)}
    (throw (ex-info "opción de audit-cycle desconocida" {:option (first args)}))))

(defn main-status! [args]
  (let [{:keys [text warnings]} (audit-report (load-snapshot args))]
    (print text)
    (if (seq warnings) 1 0)))
