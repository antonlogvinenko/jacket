(ns small-jvm-lisp.semantics
  (:use [small-jvm-lisp.lexer]))

(defn is-sexpr? [expr]
  (vector? expr))

(defn check-define [sexpr] {:symtable [] :errors []})
(defn check-lambda [sexpr] {:symtable [] :errors []})

(defn analyze-sexpr [symtable sexpr]
  (let [f (first sexpr)
        legal-fs (concat KEYWORDS symtable)]
    (cond
        (not-any? (partial = f) legal-fs) {:errors ["error1"]}
        (= :def f) (check-define symtable sexpr)
        (= :lambda f) (check-lambda symtable sexpr)
        :else {})))

(defn analyze-sexpr-tree [sexpr]
  (loop [{symtable :symtable :as analysis} {}
         sexpr-stack [sexpr]]
    (if (empty? sexpr-stack)
      (analysis :errors)
      (let [new-analysis (->> sexpr-stack
                              last
                              (analyze-sexpr symtable)
                              (merge-with concat analysis))
            new-sexpr-stack (->> sexpr
                                 (filter is-sexpr?)
                                 reverse
                                 (concat (pop sexpr-stack)))]
        (recur new-analysis new-sexpr-stack)))))

;;check def, lambda
;;global defines?
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
                         (reduce analyze-sexpr-tree))]
    (raise-semantics-error analysis)
    program))
    
  