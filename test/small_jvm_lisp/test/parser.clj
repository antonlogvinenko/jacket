(ns small-jvm-lisp.test.parser
  (:use [clojure.test]
        [small-jvm-lisp.parser]
        [small-jvm-lisp.lexer]
        )
  (:import [small_jvm_lisp.fsm Token]
           ))

(defn to-tokens [ts]
  (vec
   (map
    #(if (vector? %) (to-tokens %) (Token. % 1 1))
    ts)))

(deftest conj-last-test
  (are [stack elem new-stack] (= new-stack (conj-last stack elem))
       [[1] [2]] 3
       [[1] [2 3]]

       [[1 2]] 3
       [[1 2 3]]

       [[1 2] [3 4]] 5
       [[1 2] [3 4 5]]
       ))

(deftest read-sexpr-test
  (are [tokens program] (-> tokens to-tokens read-sexpr (= program))
       [:LB :a :RB]
       {:expr [:a] :tokens '()}

       [:LB :define 'sum :LB :lambda :LB 'a 'b :RB :LB :+ 'a 'b :RB :RB :RB]
       {:expr [:define 'sum [:lambda ['a 'b] [:+ 'a 'b]]] :tokens '()}

       [:LB :define 'a 3.14 :RB 42]
       {:expr [:define 'a 3.14] :tokens [42]}
       
       ))

(deftest read-expr-test
  (are [tokens program] (-> tokens to-tokens read-expr (= program))
       [:LB :a :RB]
       {:expr [:a] :tokens []}

       [41 :LB :RB]
       {:expr 41 :tokens [:LB :RB]}
       ))

(deftest read-program-test
  (are [text program] (-> text tokenize read-program (= program))
       "(define sum (lambda (a b) (+ a b))) 42"
       [[:define 'sum [:lambda ['a 'b] [:+ 'a 'b]]] 42]

       ))
