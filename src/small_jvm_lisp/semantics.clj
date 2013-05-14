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

(defn search-symbol [[global local _] sym]
  (let [legal-syms (concat (map keywordize KEYWORDS) (flatten local) global)]
    (not-any? #(= sym %) legal-syms)))

(defn check-define [[_ local _ _] sexpr]
  (let [length (count sexpr)
        name-token (second sexpr)
        body (last sexpr)]
    (cond
      (not= length 3)
      [nil nil [(str "Wrong arguments amount to def (" length ")")] nil]
      
      (-> name-token (is? symbol?) not)
      [nil nil [(str "Not a symbol (" (.value name-token) ")")] nil]

      :else [[(second sexpr)] [(second sexpr)] nil (if (is-sexpr? body) [body] nil)])))

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
  (let [f (first sexpr)]
    (cond
      (= f :def) (check-define state sexpr)
      (= f :lambda) (check-lambda state sexpr)
      (= f :quote) (check-quote state sexpr)
      (search-symbol state f)
      [nil nil ["Illegal first token for s-expression"] nil]
      :else [nil nil nil nil])))

(defn conj-not-empty [coll & xs]
  (loop [coll coll, [x & xx :as xs] xs]
    (cond
      (empty? xs) coll
      (nil? x) (recur coll xx)
      :else (recur (conj coll x) xx))))
        
(defn analyze-sexpr-tree [[global local errors] sexpr]
  (loop [g global, l local, e errors, s [[sexpr]]]
    (let [current-level (last s)
          current-s (last current-level)]
      (cond
        (empty? s) [g l e]
        (empty? current-level) (recur g (if (empty? l) l (pop l)) e (pop s))
        :else (let [[g2 l2 e2 s2] (analyze-sexpr [g l e s] current-s)]
                (recur (concat g g2)
                       (conj-not-empty l l2)
                       (concat e e2)
                       (-> s pop (conj-not-empty (pop current-level) s2))))))))

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