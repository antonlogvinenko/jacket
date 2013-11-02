(ns jacket.codegen.macro
  (:use [jacket.lexer.lexer]
        [jacket.lexer.fsm]
        [clojure.walk])
  (:import [jacket.lexer.fsm Token]))

                                        ;Making (define macro (lambda ...)) out of (defmacro ...)
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


                                        ;Macro expansion during compilation
(defn- expand-macro [[f & args :as expr] macro-fn]
  (->> args
       (map #(if (instance? Token %) (.value %) %))
       vec
       .toArray
       (._invoke macro-fn)
       (postwalk (fn [x] (cond (vector? x) x
                               (instance? Token x) x
                               :else (Token. x 1 1))))))

(defn- expand-macro-with [definitions obj]
  (cond
   (-> obj vector? not) obj
   (empty? obj) obj
   :else (let [f (first obj)]
           (if (vector? f) obj
               (let [fv (.value f)
                     def (get definitions fv)]
                 (if (nil? def) obj
                     (expand-macro obj def)))))))

(defn macrodef? [names def]
  (and (vector? def)
       (-> def count (> 1))
       (-> def first (= :define))
       (let [snd (second def)]
         (->> names (some (partial = snd)) nil? not))))

(defn- expand-with-macro-definitions [ast definitions]
  (let [grouped (group-by (partial macrodef? (keys definitions)) ast)
        macro (grouped true)
        ast (grouped false)]
    (->> ast
         (postwalk (partial expand-macro-with definitions))
         (into macro))))

(defn macroexpand-jacket [class-name ast macro-names]
  (let [loaded-class (Class/forName class-name)
        fields (.getDeclaredFields loaded-class)
        pairs (->> macro-names
                   (map str)
                   (map (fn [macro-name] (.getDeclaredField loaded-class macro-name)))
                   (map (fn [field] [(-> field .getName symbol) (.get field nil)]))
                   (into {}))]
    [(expand-with-macro-definitions ast pairs) macro-names]))
