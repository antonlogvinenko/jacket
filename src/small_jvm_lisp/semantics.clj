(ns small-jvm-lisp.semantics
  (:use [small-jvm-lisp.lexer]
        [small-jvm-lisp.errors]
        ))

(defn is-sexpr? [expr]
  (vector? expr))

(defn check-define [symtable sexpr]
  {})
(defn check-lambda [symtable sexpr] {:symtable []})

(defn analyze-sexpr [symtable sexpr]
  (let [f (first sexpr)
        legal-fs (concat (map keywordize KEYWORDS) symtable)]
    (cond
        (not-any? (partial = f) legal-fs) {:errors ["error1"]}
        (= :def f) (check-define symtable sexpr)
        (= :lambda f) (check-lambda symtable sexpr)
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
            new-sexpr-stack (->> current-sexpr
                                 (filter is-sexpr?)
                                 reverse
                                 (concat (pop sexpr-stack))
                                 vec)]
        (recur new-analysis new-sexpr-stack)))))

(defn raise-semantics-error [analysis]
  (raise-error "Semantics analysis failed: "))

(defn semantics [program]
  (if-let [analysis (->> program
                         (filter is-sexpr?)
                         (reduce analyze-sexpr-tree {})
                         :errors)]
    (raise-semantics-error analysis)
    program))
    
  