(ns small-jvm-lisp.syntax)

;;two tail recursions may be used at a time
;;one for reading a program as a sequence of expressions
;;another one for reading an s-expression

(def not-list-preds [float? integer? symbol? keyword? string?])

(defn conj-last [stack elem]
  (conj (pop stack)
        (conj (last stack) elem)))

(defn read-sexpr [tokens]
  (loop [tokens tokens stack [[]]]
    (let [token (first tokens)
          tokens (rest tokens)]
      (if (nil? token)
        (if (-> stack count (= 1))
          {:expr (first stack) :tokens tokens}
          (throw (RuntimeException. "Ooops")))
        (let [new-stack (case token
                          :LB (conj stack [])
                          :RB (conj-last (pop stack) (peek stack))
                          (conj-last stack token))]
          (recur tokens new-stack))))))

(defn read-expression [tokens]
  (let [token (first tokens)
        tokens (rest tokens)
        no-list? (->> not-list-preds (map #(% token)) (some true?))]
    (cond
      no-list? {:expr token :tokens tokens}
      (= :LB token) (read-sexpr tokens)
      :else (throw (RuntimeException. "Ooops")))))

(defn read-program [tokens]
  (loop [expressions [] tokens tokens]
    (let [{expr :expr tokens :tokens} (read-next-expression tokens)]
      (recur (conj expressions expr) tokens))))