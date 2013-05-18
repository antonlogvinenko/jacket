(ns small-jvm-lisp.semantics
  (:use [small-jvm-lisp.lexer]
        [small-jvm-lisp.errors]
        [small-jvm-lisp.syntax]
        [small-jvm-lisp.fsm]
        ))

(defn is-sexpr? [expr]
  (vector? expr))

(def ok [[] [] [] []])
(defn super-update [s i v]
  (update-in s [i]
             #(if (seq? v) (apply conj % v) (conj % v))))
(defn +global [s g] (super-update s 0 g))
(defn +local [s l] (super-update s 1 l))
(defn +error [s er] (super-update s 2 er))
(defn +exprs [s ex] (super-update s 3 ex))

(defn symbol-undefined? [[global local _] sym]
  (let [legal-syms (concat (map keywordize KEYWORDS) (flatten local) global)]
    (not-any? #(= sym %) legal-syms)))

(defn check-define [[_ local _ _] sexpr]
  (let [length (count sexpr)
        name (second sexpr)
        body (last sexpr)]
    (cond
      (not= length 3)
      (-> ok
          (+error (str "Wrong arguments amount to define (" length ")")))
      
      (-> name (is? symbol?) not)
      (-> ok
          (+error (str "Not a symbol (" (.value name) ")"))
          (+exprs body))

      :else (-> ok
                (+local name)
                (+global name)
                (+exprs body)))))

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
          (+error (str "Wrong arguments at lambda"))
          (+exprs body))

      :else (-> ok
                (+local (seq args))
                (+exprs body)))))

(defn check-pair [[g l _] pair]
  (if (-> pair vector? not)
    (-> ok (+error "Must be a list"))
    (let [f (first pair)
          body (second pair)]
      (cond
        
        (-> pair count (not= 2))
        (-> ok
            (+error "Wrong arguments for let")
            (+exprs body))
        
        (-> f (is? symbol?) not)
        (-> ok
            (+error "Must be token")
            (+exprs body))
        
        :else (-> ok
                  (+local f)
                  (+exprs body))))))
  
(defn merge-states [states state]
  (->> states
       (map (partial check-pair state))
       (apply map (comp vec concat))
       vec))

(defn check-let [state sexpr]
  (let [length (count sexpr)
        args (second sexpr)
        args-length (count args)
        body (last sexpr)]
    (cond
      (not= 3 length)
      (-> ok
          (+error "Error :)"))
      
      (< args-length 1)
      (-> ok
          (+exprs body))
      
      :else (let [analysis (merge-states args state)]
              (conj (pop analysis) (conj (peek analysis) body))))))

(defn check-quote [_ sexpr]
  (let [length (count sexpr)]
    (if (= length 2)
      ok
      (-> ok
          (+error "Wrong arguments count to quote")))))

(defn check-dynamic-list [state sexpr]
  (let [f (first sexpr)
        other (rest sexpr)
        sexprs (filter is-sexpr? sexpr)
        pred (fn [t] (is? t #(or (symbol? %) (keyword? %))))
        undefined-symbols (->> other
                               (filter (comp not coll?))
                               (filter pred)
                               (filter (partial symbol-undefined? state))
                               vec)
        new-state (if (empty? sexprs) ok (+exprs ok sexprs))]
    (cond
      (symbol-undefined? state f)
      (-> new-state
          (+error "Illegal first token for s-expression"))
      
      (seq undefined-symbols)
      (-> new-state
          (+error (->> undefined-symbols (map #(.value %)) vec (str "Undefined symbols: "))))
      
      :else new-state)))
  
(defn check-sexpr [state sexpr]
  (let [f (first sexpr)
        dispatch {:define check-define
                  :lambda check-lambda
                  :quote check-quote
                  :let check-let}]
    (if (-> sexpr first nil?)
      (-> ok
          (+error "expected a function"))
      ((get dispatch (.value f) check-dynamic-list) state sexpr))))

(defn check-atom [state expr]
  (if (and (or (is? expr symbol?) (is? expr keyword?))
           (symbol-undefined? state expr))
    (-> ok (+error (str "Undefined symbol " expr)))
    ok))

(defn check-expr [state expr]
  (let [check (if (is-sexpr? expr)
                  check-sexpr
                  check-atom)]
    (check state expr)))                    
  
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
        (empty? current-level) (recur g (if (empty? l) l (drop-last l)) e (drop-last s))
        :else (let [[g2 l2 e2 s2] (check-expr [g l e s] current-s)]
                (recur (concat g g2)
                       (conj-not-empty l l2)
                       (concat e e2)
                       (-> s pop (conj-not-empty (drop-last current-level) s2))))))))

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
                    (reduce analyze-file-expr [])
                    last)]
    (if (empty? errors)
      program
      (-> errors (map str) (reduce str) raise-semantics-error))))
