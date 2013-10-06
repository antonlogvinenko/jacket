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

(defn expand-macro-with [[f & args :as expr] macro-fn]
  (._invoke macro-fn (.toArray args)))

(defn expand-macro-all [definitions obj]
  (if (-> obj vector? not) obj
      (let [f (.value (first obj))
            def (get definitions f)]
        (if (nil? def) obj
            (expand-macro-with obj def)))))

(defn expand-with-macro-definitions [ast definitions]
  (postwalk (partial expand-macro-all definitions) ast))

(defn expand-macro [class-name ast macro-names]
  (let [loaded-class (Class/forName class-name)
        fields (.getDeclaredFields loaded-class)
        pairs (->> fields
                   (map (fn [field] [(symbol (.getName field)) (.get field nil)]))
                   (into {}))]
    [(expand-with-macro-definitions ast pairs) macro-names]))
