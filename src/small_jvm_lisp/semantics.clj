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

(defn search-symbol [{local :sym-l global :g-sym} sym]
  (let [legal-syms (concat (map keywordize KEYWORDS) (flatten local) global)]
    (not-any? #(= sym %) legal-syms)))

(defn check-define [_ sexpr]
  (let [length (count sexpr)
        name-token (second sexpr)
        body (last sexpr)]
    (cond
      (not= length 3)
      [nil nil [(str "Wrong arguments amount to def (" length ")")] nil]
      
      (-> name-token (is? symbol?) not)
      [nil nil [(str "Not a symbol (" (.value name-token) ")")] nil]

      :else [[(second sexpr)] nil nil (if (is-sexpr? body) [body] nil)])))

(defn check-lambda [_ sexpr]
  (let [length (count sexpr)
        args (second sexpr)
        body (last sexpr)]
    (cond
      (not= length 3)
      [nil nil [(str "Wrong arguments amount to lambda (" length ")")] nil]
      
      (->> sexpr second (every? #(is? % symbol?)) not)
      [nil nil [(str "Wrong arguments at lambda")] nil]

      :else [nil (second sexpr) nil (if (is-sexpr? body) [body] nil)]
      )))

(defn check-quote [_ sexpr]
  (let [length (count sexpr)]
    (if (= length 2)
      [nil nil nil nil]
      [nil nil ["Wrong arguments count to quote"] nil])))

(defn analyze-sexpr [state sexpr]
;;  (println "sexpr: " sexpr)
  (let [f (first sexpr)
        other-sexprs (->> sexpr (filter is-sexpr?) reverse)
        other-sexprs (cond
                       (= f :quote) nil
                       (= f :lambda) (-> other-sexprs drop-last vec)
                       :else (vec other-sexprs))]
    (cond
      (= f :def) (check-define state sexpr)
      (= f :lambda) (check-lambda state sexpr)
      (= f :quote) (check-quote state sexpr)
      (search-symbol state f)
      [nil nil ["Illegal first token for s-expression"] nil]
      :else [nil nil nil nil])))

(defn analyze-sexpr-tree [[global local errors] sexpr]
  (loop [g global, l local, e errors, s [[sexpr]]]
    (let [current-level (last s)]
      (cond
        (empty? s) [g l e]
        (empty? current-level) (recur
                                g (if (empty? l) l (pop l)) e (pop s))
        :else (let [current-s (last current-level)
                    [g2 l2 e2 s2] (analyze-sexpr [g l e s] current-s)
                    new-current-level (pop current-level)
                    l (if (nil? l2) l (conj l l2))
                    s (pop s)
                    s (if (empty? new-current-level) s (conj s (new-current-level)))
                    s (if (empty? s2) s (conj s s2))]
                (recur (concat g g2) l (concat e e2) s))))))

(defn raise-semantics-error [analysis]
  (->> analysis
       (str "Semantics analysis failed: " analysis)
       raise))

(defn semantics [program]
  (let [errors (->> program
                    (filter is-sexpr?)
                    (reduce analyze-sexpr-tree [[] [] []])
                    last)]
    (if (empty? errors)
      program
      (raise-semantics-error (reduce str (map str errors))))))