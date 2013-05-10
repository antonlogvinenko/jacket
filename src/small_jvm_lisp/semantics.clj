(ns small-jvm-lisp.semantics)

(defn is-sexpr? [expr]
  (vector? expr))

(defn analyze-sexpr [sexpr])

(defn raise-semantics-error [analysis]
  (->> analysis
       (str "Semantics analysis failed: ")
       RuntimeException.
       throw))

(defn semantics [program]
  (if-let [analysis (->> program
                         (filter is-sexpr?)
                         (reduce analyze-sexpr))]
    (raise-semantics-error analysis)
    program))
    
  