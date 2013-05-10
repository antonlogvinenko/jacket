(ns small-jvm-lisp.semantics
  (:use [small-jvm-lisp.lexer]))

(defn is-sexpr? [expr]
  (vector? expr))

(defn check-define [symtable sexpr] {:symtable []})
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

;;check def, lambda
;;global definitions, outside all global s-expressions must work
;;test everything

;;check tail-recursively
;;if f is not keyword or defined symbol then error - what defines symbols in constructs?
;;gather ALL semantics errors, do not throw on the first one!

(defn raise-semantics-error [analysis]
  (->> analysis
       (str "Semantics analysis failed: ")
       RuntimeException.
       throw))

(defn semantics [program]
  (if-let [analysis (->> program
                         (filter is-sexpr?)
                         (reduce analyze-sexpr-tree {})
                         :errors)]
    (raise-semantics-error analysis)
    program))
    
  