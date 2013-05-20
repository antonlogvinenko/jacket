(ns small-jvm-lisp.semantics
  (:use [small-jvm-lisp.lexer]
        [small-jvm-lisp.errors]
        [small-jvm-lisp.parser]
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

(defn check-define [sexpr]
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

(defn check-lambda [sexpr]
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

(defn check-pair [pair]
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

(defn merge-states [states]
  (->> states
       (map check-pair)
       (apply map (comp vec concat))
       vec))

(defn check-let [sexpr]
  (let [length (count sexpr)
        args (second sexpr)
        args-length (count args)
        body (drop 2 sexpr)]
    (cond
      (-> body count zero?)
      (-> ok
          (+error "Error :)"))
      
      (< args-length 1)
      (-> ok
          (+exprs body))
      
      :else (let [analysis (merge-states args)]
              (conj (pop analysis) (into (peek analysis) body))))))

(defn check-quote [sexpr]
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
      (if-let [impl (get dispatch (.value f))]
        (impl sexpr)
        (check-dynamic-list state sexpr)))))
      
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
  
(defn analyze-sexpr-tree [[global local errors] sexpr]
  (loop [g global, l [[]], e errors, s [[sexpr]]]
    (let [current-level (last s)
          current-s (last current-level)]
      (cond
        (empty? s) [g [] e]
        (empty? current-level) (recur g (pop l) e (pop s))
        :else (let [[g2 l2 e2 s2] (check-expr [g l e s] current-s)]
                (recur (concat g g2)
                       (conj l l2)
                       (concat e e2)
                       (-> s
                           pop
                           (conj (pop current-level))
                           (conj s2))))))))

(defn analyze-lonely-atom [_ expr]
  (->> expr
       (str "What is that ")
       (+error ok)
       (drop-last 1)))

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
