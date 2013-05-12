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

(defn search-symbol [global local sym]
  (let [legal-syms (concat (map keywordize KEYWORDS) (flatten local) global)]
    (not-any? #(= sym %) legal-syms)))

(defn check-define [symtable sexpr]
  (let [length (count sexpr)
        name-token (second sexpr)]
    (cond
      (not= length 3)
      {:errors [(str "Wrong arguments amount to def (" length ")")]}
      
      (-> name-token (is? symbol?) not)
      {:errors [(str "Not a symbol (" (.value name-token) ")")]}

      :else {:symtable []} )))

(defn check-lambda [symtable sexpr]
  (let [length (count sexpr)
        args (second sexpr)]
    (cond
      (not= length 3)
      {:errors [(str "Wrong arguments amount to lambda (" length ")")]}
      
      (->> sexpr second (every? #(is? % symbol?)) not)
      {:errors [(str "Wrong arguments at lambda")]}

      :else {:symtable (second sexpr)}
      )))

(defn analyze-sexpr [sym-g sym-l sexpr]
  (let [f (first sexpr)]
    (cond
      (search-symbol sym-g sym-l f)
      [sym-g {:errors ["Illegal first token for s-expression"]}]
      (= f :def) [(conj sym-g (second sexpr)) (check-define sym-l sexpr)]
      (= f :lambda) [sym-g (check-lambda sym-l sexpr)]
      :else [sym-g {:errors [] :symtable []}])))

(defn analyze-sexpr-tree [analysis sexpr]
  (loop [global-table []
         {symtable :symtable errors :errors :as analysis} analysis
         sexpr-level-stack [[sexpr]]]
    (let [current-sexpr-level (last sexpr-level-stack)]
      (cond
        (empty? sexpr-level-stack) analysis

        (empty? current-sexpr-level) (recur global-table
                                            {:errors errors :symtable (pop symtable)}
                                            (pop sexpr-level-stack))
        
        :else (let [current-sexpr (last current-sexpr-level)
                    
                    [global-table {new-errors :errors new-symtable :symtable}]
                    (analyze-sexpr global-table symtable current-sexpr)
                    
                    new-analysis {:errors (vec (concat errors new-errors))
                                  :symtable (conj symtable new-symtable)}
                    
                    is-quoted (-> current-sexpr first (= :quote))
                    is-lambda (-> current-sexpr first (= :lambda))
                    cleaned-sexpr-stack (pop current-sexpr-level)
                    other-sexprs (->> current-sexpr
                                      (filter is-sexpr?)
                                      reverse)
                    new-sexpr-stack (conj cleaned-sexpr-stack
                                          (vec
                                           (if is-lambda
                                             (drop-last other-sexprs)
                                             other-sexprs)))]
                (recur global-table new-analysis new-sexpr-stack))))))

(defn raise-semantics-error [analysis]
  (->> analysis
       (str "Semantics analysis failed: " analysis)
       raise))

(defn semantics [program]
  (let [errors (->> program
                    (filter is-sexpr?)
                    (reduce analyze-sexpr-tree {:errors [] :symtable []})
                    :errors)]
    (if (empty? errors)
      program
      (raise-semantics-error (reduce str (map str errors))))))