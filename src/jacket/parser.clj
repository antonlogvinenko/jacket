(ns jacket.parser
  (:use [jacket.lexer.lexer]
        [jacket.errors]
        [jacket.lexer.fsm]
        [clojure.walk])
  (:import [jacket.lexer.fsm Token]))

(defn conj-last [stack elem]
  (conj (pop stack)
        (conj (last stack) elem)))

(defn raise-unmatched-brace [stack token]
  (let [opening (->> stack first first)]
    (raise-at-token token
                    (str "Closing bracket expected for s-expression "
                         "started at token " (.value opening) " "
                         "on line " (.line opening) ", column " (.column opening)))))

(defn parse-sexpr [tokens]
  (loop [prev nil tokens tokens stack []]
    (let [token (first tokens)
          tokens (rest tokens)]
      (if (nil? token)
        (raise-unmatched-brace stack prev)
        (if (and (-> stack count (= 1)) (= token :RB))
          {:expr (first stack) :tokens tokens}
          (let [new-stack (case (.value token)
                            :LB (conj stack [])
                            :RB (conj-last (pop stack) (peek stack))
                            (conj-last stack token))]
            (recur token tokens new-stack)))))))

(def sugar {:' :quote
            (keyword "`") :backtick
            (keyword "~") :unquote
            (keyword "~@") :unquote-splicing})

(defn ab [coll x]
  (if-let [t (get sugar (last coll))]
    (conj (into [] (drop-last 1 coll)) [(Token. t 1 1) x])
    (conj coll x)))

(defn macro-template [sexpr]
  (if (vector? sexpr)
    (reduce ab [] sexpr)
    sexpr))

(defn parse-expr [tokens]
  (let [token (first tokens)
        token-value (.value token)]
    (cond
      (= token-value :LB) (parse-sexpr tokens)
      :else {:expr token :tokens (rest tokens)})))

(defn parse [tokens]
  (loop [tokens tokens expressions []]
    (if (empty? tokens)
      expressions
      (let [{expr :expr tokens :tokens} (parse-expr tokens)]
        (->> expr (postwalk macro-template) (conj expressions) (recur tokens))))))
