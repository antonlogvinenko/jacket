(ns small-jvm-lisp.semantics
  (:use [small-jvm-lisp.grammar]
        [small-jvm-lisp.errors]
        [small-jvm-lisp.syntax]
        [small-jvm-lisp.fsm]
        ))

;;check tail-recursively
;;if outside of quotation f is not keyword or defined symbol then error
;;gather ALL semantics errors, do not throw on the first one!

(defn is-sexpr? [expr]
  (vector? expr))

(defn check-define [symtable sexpr]
  (let [length (count sexpr)
        name-token (second sexpr)]
    (cond
      (not= length 3)
      {:errors [(str "Wrong arguments amount to def (" length ")")]}
      
      (-> name-token (is? symbol?) not)
      {:errors [(str "Not a symbol (" (.value name-token) ")")]}

      :else
      {:symtable [name-token]})))

(defn check-lambda [symtable sexpr]
  (let [length (count sexpr)
        args (second sexpr)]
    (cond
      (not= length 3)
      {:errors [(str "Wrong arguments amount to lambda (" length ")")]}
      
      (->> sexpr second (every? #(is? % symbol?)) not)
      {:errors [(str "Wrong arguments at lambda")]}

      :else {}
      )))

(defn analyze-sexpr [symtable sexpr]
  (let [f (first sexpr)
        legal-fs (concat (map keywordize KEYWORDS) symtable)]
    (cond
        (not-any? #(= f %) legal-fs) {:errors ["Illegal first token for s-expression"]}
        (= f :def) (check-define symtable sexpr)
        (= f :lambda) (check-lambda symtable sexpr)
        :else {})))

(defn analyze-sexpr-tree [analysis sexpr]
  (loop [{symtable :symtable :as analysis} analysis
         sexpr-stack [sexpr]]
    (if (empty? sexpr-stack)
      analysis
      (let [current-sexpr (last sexpr-stack)
            new-analysis (->> current-sexpr
                              (analyze-sexpr symtable)
                              (merge-with concat analysis))
            is-quoted (-> sexpr-stack first (= :quote))
            cleaned-sexpr-stack (pop sexpr-stack)
            new-sexpr-stack (if is-quoted
                              cleaned-sexpr-stack
                              (->> current-sexpr
                                   (filter is-sexpr?)
                                   reverse
                                   (concat cleaned-sexpr-stack)
                                   vec))]
        (recur new-analysis new-sexpr-stack)))))

(defn raise-semantics-error [analysis]
  (->> analysis
       (str "Semantics analysis failed: ")
       raise))

(defn semantics [program]
  (if-let [analysis (->> program
                         (filter is-sexpr?)
                         (reduce analyze-sexpr-tree {:errors []})
                         :errors
                         (comp not empty? :errors))]
    (raise-semantics-error analysis)
    program))