(ns small-jvm-lisp.test.syntax
  (:use [clojure.test]
        [small-jvm-lisp.syntax]
        [small-jvm-lisp.grammar]
        ))

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
  (are [tokens program] (= program (read-sexpr tokens))
       [:LB :a :RB]
       {:expr [:a] :tokens '()}

       [:LB :def 'sum :LB :lambda :LB 'a 'b :RB :LB :+ 'a 'b :RB :RB :RB]
       {:expr [:def 'sum [:lambda ['a 'b] [:+ 'a 'b]]] :tokens '()}

       [:LB :def 'a 3.14 :RB 42]
       {:expr [:def 'a 3.14] :tokens [42]}
       
       ))

(deftest read-expr-test
  (are [tokens program] (= program (read-expr tokens))
       [:LB :a :RB]
       {:expr [:a] :tokens []}

       [41 :LB :RB]
       {:expr 41 :tokens [:LB :RB]}
       ))

(deftest read-program-test
  (are [text program] (-> text tokenize read-program (= program))
       "(def sum (lambda (a b) (+ a b))) 42"
       [[:def 'sum [:lambda ['a 'b] [:+ 'a 'b]]] 42]

       ))
