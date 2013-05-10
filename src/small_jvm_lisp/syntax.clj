(ns small-jvm-lisp.syntax
  (:use [small-jvm-lisp.lexer]))

;;two tail recursions may be used at a time
;;one for reading a program as a sequence of expressions
;;another one for reading an s-expression

(def not-list-preds [float? integer? symbol? keyword? string?])

(defn conj-last [stack elem]
  (conj (pop stack)
        (conj (last stack) elem)))

(defn raise-unmatched-brace [stack]
  (->> (last stack)
       (str "Unmatched brace, s-expression: ")
       RuntimeException.
       throw))

(defn raise-unknown-token [token]
  (->> token
       (str "Unknown token")
       RuntimeException.
       throw))

(defn read-sexpr [tokens]
  (loop [tokens tokens stack []]
    (let [token (first tokens)
          tokens (rest tokens)]
      (if (nil? token)
        (raise-unmatched-brace stack)
        (if (and (-> stack count (= 1)) (= token :RB))
          {:expr (first stack) :tokens tokens}
          (let [new-stack (case token
                            :LB (conj stack [])
                            :RB (conj-last (pop stack) (peek stack))
                            (conj-last stack token))]
            (recur tokens new-stack)))))))
  
(defn read-expr [tokens]
  (let [token (first tokens)
        no-list? (->> not-list-preds (map #(% token)) (some true?))]
     (cond
      (= :LB token) (read-sexpr tokens)
      no-list? {:expr token :tokens (rest tokens)}
      :else (raise-unknown-token token))))

(defn read-program [tokens]
  (loop [expressions [] tokens tokens]
    (if (empty? tokens)
      expressions
      (let [{expr :expr tokens :tokens} (read-expr tokens)]
        (recur (conj expressions expr) tokens)))))