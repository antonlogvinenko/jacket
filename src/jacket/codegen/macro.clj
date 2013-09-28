(ns jacket.codegen.macro
  (:use [jacket.lexer.lexer]
        [jacket.lexer.fsm]
        [clojure.walk])
  (:import [jacket.lexer.fsm Token]))

(defn expand-macro-definition [[key & others :as block]]
  (if (not= key :defmacro)
    block
    (let [name (first others)
          args (second others)
          body (nth others 2)]
      [(Token. :define (.line key) (.column key))
       name
       [(Token. :lambda (.line name) (.column name)) args body]])))

(defn macro-definitions [[definitions macro]]
  [(map expand-macro-definition definitions) macro])

(defn replace-symbols [symbols obj]
  (cond
   (vector? obj) obj
   (is? obj symbol?) (get symbols obj obj)
   :else obj))

(defn expand-macro-with [[f & args :as expr] [pars & body :as definition]]
  (if (not= (count args) (count expr))
    (-> "Not enough arguments for macro expansion" RuntimeException. throw)
    (let [symbols (zipmap pars args)]
      (walk (partial replace-symbols symbols) identity body))))

(defn expand-macro-all [definitions obj]
  (if (-> obj vector? not) obj
      (let [f (first obj)
            def (get definitions f)]
        (if (nil? def) obj
            (expand-macro-with obj def)))))




(defn expand-with-macro-definitions [ast definitions]
  (walk (partial expand-macro-all definitions) identity ast))

(defn fetch-definitions [ast]
  (let [clusters (group-by #(-> % first (= :defmacro)) ast)
        definitions (->> true
                         clusters
;;                         (map (partial drop 1))
  ;;                       (map #(vector (first %) (rest %)))
    ;;                     (into {})
                         )
        body (clusters false)]
    [definitions body]))

(defn expand-macro [ast]
  (let [[definitions ast] (fetch-definitions ast)]
    (expand-with-macro-definitions ast definitions)))
