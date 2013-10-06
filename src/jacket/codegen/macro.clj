(ns jacket.codegen.macro
  (:use [jacket.lexer.lexer]
        [jacket.lexer.fsm]
        [clojure.walk])
  (:import [jacket.lexer.fsm Token]))

(defn- expand-macro-definition [[key & others :as block]]
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


(defn- expand-macro [[f & args :as expr] macro-fn]
  (->> args .toArray (._invoke macro-fn)))

(defn- expand-macro-with [definitions obj]
  (if (-> obj vector? not) obj
      (let [f (.value (first obj))
            def (get definitions f)]
        (if (nil? def) obj
            (expand-macro obj def)))))

(defn- expand-with-macro-definitions [ast definitions]
  (postwalk (partial expand-macro-with definitions) ast))

(defn macroexpand-jacket [class-name ast macro-names]
  (let [loaded-class (Class/forName class-name)
        fields (.getDeclaredFields loaded-class)
        pairs (->> fields
                   (map (fn [field] [(-> field .getName symbol) (.get field nil)]))
                   (into {}))]
    [(expand-with-macro-definitions ast pairs) macro-names]))
