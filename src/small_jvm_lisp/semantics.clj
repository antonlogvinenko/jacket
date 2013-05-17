(ns small-jvm-lisp.semantics
  (:use [small-jvm-lisp.lexer]
        [small-jvm-lisp.errors]
        [small-jvm-lisp.syntax]
        [small-jvm-lisp.fsm]
        ))

(defn is-sexpr? [expr]
  (vector? expr))

(def ok [nil nil nil nil])
(defn super-update [s i v] (update-in s [i] #(conj % v)))
(defn +global [s g] (super-update s 0 g))
(defn +local [s l] (super-update s 1 l))
(defn +error [s er] (super-update s 2 er))
(defn +exprs [s ex] (super-update s 3 ex))

(defn symbol-undefined? [[global local _] sym]
  (let [legal-syms (concat (map keywordize KEYWORDS) (flatten local) global)]
    (not-any? #(= sym %) legal-syms)))

(defn check-define [[_ local _ _] sexpr]
  (let [length (count sexpr)
        name-token (second sexpr)]
    (cond
      (not= length 3)

      (-> ok
          (+error (str "Wrong arguments amount to define (" length ")")))
      
      (-> name-token (is? symbol?) not)
      (-> ok
          (+error (str "Not a symbol (" (.value name-token) ")")))

      :else (-> ok
                (+local (second sexpr))
                (+global (second sexpr))
                (+exprs (last sexpr))))))

(defn check-lambda [_ sexpr]
  (let [length (count sexpr)
        args (second sexpr)
        body (last sexpr)]
    (cond
      (not= length 3)
      (-> ok
          (+error (str "Wrong arguments amount to lambda (" length ")")))
      
      (->> sexpr second (every? #(is? % symbol?)) not)
      (-> ok
          (+error (str "Wrong arguments at lambda")))

      :else (-> ok
                (+local (second sexpr))
                (+exprs body)))))

(defn check-pair [[g l _] pair]
  (let [f (first pair)
        body (second pair)]
    (cond
      (-> pair count (not= 2))
      (-> ok
          (+error "Wrong arguments for let"))
      
      (-> f (is? symbol?) not)
      (-> ok
          (+error "Must be token"))
      
      :else (-> ok
                (+local f)))))

(defn check-let [state sexpr]
  (let [length (count sexpr)
        args (second sexpr)
        args-length (count args)
        body (last sexpr)]
    (cond
      (not= 3 length) [nil nil ["Error :)"] nil]
      (< args-length 1) [nil nil [] body]
      :else (let [analysis (->> args
                                (map (partial check-pair state))
                                (apply map (comp vec concat))
                                vec)]
              (conj (pop analysis) (conj (peek analysis) body))))))

;;new concepts:
;;1. pass anything possibly checkable, even constants
;;2. how to return only what required? global state? using maps?

(defn check-quote [_ sexpr]
  (let [length (count sexpr)]
    (if (= length 2)
      [nil nil nil nil]
      [nil nil ["Wrong arguments count to quote"] nil])))

(defn check-dynamic-list [state sexpr]
  (let [f (first sexpr)
        other (rest sexpr)
        pred (fn [t] (is? t #(or (symbol? %) (keyword? %))))
        undefined-symbols (->> other
                               (filter pred)
                               (filter (partial symbol-undefined? state))
                               vec)
        sexprs (filter is-sexpr? other)]
    (cond
      (symbol-undefined? state f)
      [nil nil ["Illegal first token for s-expression"] sexprs]
      (seq undefined-symbols)
      [nil nil [(->> undefined-symbols (map #(.value %)) vec (str "Undefined symbols: "))] sexprs]
      :else
      [nil nil nil sexprs])))
  
(defn analyze-sexpr [state sexpr]
  (let [f (first sexpr)]
    (cond
      (nil? f) [nil nil ["expected a function"] nil]
      (= f :define) (check-define state sexpr)
      (= f :lambda) (check-lambda state sexpr)
      (= f :quote) (check-quote state sexpr)
      (= f :let) (check-let state sexpr)
      :else (check-dynamic-list state sexpr))))

(defn analyze-atom [state expr]
  ok)

(defn analyze-expr [state expr]
  (let [analyze (if (is-sexpr? expr)
                  analyze-sexpr
                  analyze-atom)]
    (analyze state expr)))                    
  
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
        :else (let [[g2 l2 e2 s2] (analyze-expr [g l e s] current-s)]
                (recur (concat g g2)
                       (conj-not-empty l l2)
                       (concat e e2)
                       (-> s pop (conj-not-empty (pop current-level) s2))))))))

(defn analyze-lonely-atom [[g l e] expr]
  (->> expr
       (str "What is that? ")
       (conj e)
       (conj [g l])))

(defn analyze-file-expr [state expr]
  (let [analyze (if (is-sexpr? expr)
                  analyze-sexpr-tree
                  analyze-lonely-atom)]
    (analyze state expr)))

(defn raise-semantics-error [analysis]
  (->> analysis
       (str "Semantics analysis failed: " analysis)
       raise))

(defn semantics [program]
  (let [errors (->> program
                    (reduce analyze-file-expr [[] [] []])
                    last)]
    (if (empty? errors)
      program
      (raise-semantics-error (reduce str (map str errors))))))