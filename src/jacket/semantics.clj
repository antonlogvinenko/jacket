(ns jacket.semantics
  (:use [jacket.lexer.lexer]
        [jacket.errors]
        [jacket.parser]
        [jacket.lexer.fsm]
        ))

(defn is-sexpr? [expr]
  (vector? expr))

(def ok [[] [] [] []])
(defn super-update [s i v]
  (update-in s [i]
             #(if (seq? v)
                (apply conj % v)
                (conj % v))))
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
          (+error (str "Wrong arguments count to 'define' (" length ")")))
      
      (-> name (is? symbol?) not)
      (-> ok
          (+error (str "First 'define' argument must be symbol, not '" (.value name) "'"))
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
          (+error (str "Wrong 'lambda' arguments count: " length)))
      
      (->> sexpr second (every? #(is? % symbol?)) not)
      (-> ok
          (+error (str "Wrong lambda function arguments, must be symbols"))
          (+exprs body))

      :else (-> ok
                (+local (seq args))
                (+exprs body)))))

(defn check-pair [pair]
  (if (-> pair vector? not)
    (-> ok (+error "Pair must be a list of 2 elements"))
    (let [f (first pair)
          body (second pair)]
      (cond
        
        (-> pair count (not= 2))
        (-> ok
            (+error "Pair must be a list of 2 elements")
            (+exprs body))
        
        (-> f (is? symbol?) not)
        (-> ok
            (+error "Pair first element must be symbol")
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
          (+error "Let must have a body of one or more expressions"))
      
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
          (+error "Quote may have a single argument")))))

(defn check-read-utility [sexpr]
  (if (-> sexpr count dec zero?)
    ok
    (-> ok (+error
            (str \' (first sexpr) \' " requires no arguments")))))

(defn check-print-utility [sexpr]
  (let [length (count sexpr)]
    (if (> length 1)
      ok
      (-> ok (+error
              (str \' (first sexpr) \' " requires at least a single argument"))))))

(defn check-dynamic-list [state sexpr]
  (let [f (first sexpr)
        other (rest sexpr)
        sexprs (filter is-sexpr? sexpr)
        pred (fn [t] (is? t #(or (symbol? %) (keyword? %))))
        undefined-symbols (->> other
                               (filter (comp not coll?))
                               (filter pred)
                               (filter (partial symbol-undefined? state)))
        new-state (if (empty? sexprs) ok (+exprs ok sexprs))]
    (cond
      (symbol-undefined? state f)
      (-> new-state
          (+error (str "Illegal first token for s-expression: " f)))
      
      (seq undefined-symbols)
      (-> new-state
          (+error (->>
                   undefined-symbols
                   (map #(.value %))
                   vec
                   (str "Undefined symbols in s-expression: "))))
      
      :else new-state)))

(defn check-sexpr [state sexpr]
  (let [f (first sexpr)
        dispatch {:define check-define
                  :lambda check-lambda
                  :quote check-quote
                  :println check-print-utility
                  :print check-print-utility
                  :read check-read-utility
                  :readln check-read-utility
                  :let check-let}]
    (if (nil? f)
      (-> ok
          (+error "First token in s-expression must be function"))
      (if-let [impl (get dispatch (.value f))]
        (impl sexpr)
        (check-dynamic-list state sexpr)))))
      
(defn check-atom [state expr]
  (if (and (or (is? expr symbol?) (is? expr keyword?))
           (symbol-undefined? state expr))
    (-> ok (+error (str "Undefined symbol: " expr)))
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
                (recur (into g g2)
                       (conj l l2)
                       (into e e2)
                       (-> s
                           pop
                           (conj (pop current-level))
                           (conj s2))))))))

(defn analyze-lonely-atom [_ expr]
  (->> expr
       (str "Only s-expressions allowed in program top-level, found: ")
       (+error ok)
       (drop-last 1)))

(defn analyze-file-expr [state expr]
  (let [analyze (if (is-sexpr? expr)
                  analyze-sexpr-tree
                  analyze-lonely-atom)]
    (analyze state expr)))

(defn raise-semantics-errors [errors]
  (->> errors
       (interpose (str \newline))
       (apply str)
       (str "Semantics analysis failed, " (count errors) " error(s):" \newline)
       raise))

(defn semantics [program]
  (let [errors (->> program
                    (reduce analyze-file-expr [])
                    last)]
    (if (empty? errors)
      program
      (raise-semantics-errors errors))))
