(ns small-jvm-lisp.test.semantics
  (:use [clojure.test]
        [small-jvm-lisp.semantics]
        [small-jvm-lisp.fsm]
        )
  (:import [small_jvm_lisp.fsm Token])
  )

(def sexpr-z {:symtable [] :errors []})

(deftest is-sexpr?-test
  (are [expr pred] (-> expr to-tokens is-sexpr? pred)
       [:define 'a] true?
       'a false?
       
       ))

(deftest check-define-test
  (are [sexpr sym-g sym-l errors sexprs]
       (= (check-define [] (to-tokens sexpr))
          [sym-g sym-l errors sexprs])

       [:define 'b 42]
       ['b] ['b] nil [42]
       
       [:define 'b 42 42]
       nil nil ["Wrong arguments amount to define (4)"] nil
       
       [:define 42 42]
       nil nil ["Not a symbol (42)"] nil
       ))

(deftest check-lambda-test
  (are [sexpr sym-g sym-l errors sexprs]
       (= (check-lambda [] (to-tokens sexpr))
          [sym-g sym-l errors sexprs])

       [:lambda ['a 'b] true]
       nil [['a 'b]] nil [true]

       [:lambda ['a] true true]
       nil nil ["Wrong arguments amount to lambda (4)"] nil

       [:lambda ['a 42] true]
       nil nil ["Wrong arguments at lambda"] nil
       ))

(deftest check-pair-test
  (are [sexpr sym-g sym-l errors sexprs]
       (= (check-pair [] (to-tokens sexpr))
          [sym-g sym-l errors sexprs])

       ['a 42]
       nil ['a] nil nil

       ['a 42 42]
       nil nil ["Wrong arguments for let"] nil

       [42 'a]
       nil nil ["Must be token"] nil
       ))

(deftest check-let-test 
  (are [sexpr sym-g sym-l errors sexprs]
       (= (check-let [] (to-tokens sexpr))
          [sym-g sym-l errors sexprs])
       
       [:let [['a 41] ['b 1]] [:+ 'a 'b]]
       [] ['a 'b] [] [[:+ 'a 'b]]
       ))
       

(deftest check-quote-test
  (are [sexpr sym-g sym-l errors sexprs]
       (= (check-quote [] (to-tokens sexpr))
          [sym-g sym-l errors sexprs])

       [:quote ['a 'b] true]
       nil nil ["Wrong arguments count to quote"] nil

       [:quote ['a 'b]]
       nil nil nil nil

       ))

(deftest analyze-sexpr-test
  (are [sexpr sym-g sym-l errors sexprs]
       (= (analyze-sexpr [] (to-tokens sexpr))
          [sym-g sym-l errors sexprs])

       []
       nil nil ["expected a function"] nil
       
       [:define 'b 42]
       ['b] ['b] nil [42]

       [:define 42]
       nil nil ["Wrong arguments amount to define (2)"] nil

       [:define 42 42]
       nil nil ["Not a symbol (42)"] nil

       [:lambda ['a]]
       nil nil ["Wrong arguments amount to lambda (2)"] nil

       [:lambda ['a 42] 42]
       nil nil ["Wrong arguments at lambda"] nil
       
       [:de 42]
       nil nil ["Illegal first token for s-expression"] []

       [:define 'a 42]
       ['a] ['a] nil [42]

       [:lambda ['a 'b] [:+ 'a 'b]]
       nil [['a 'b]] nil [[:+ 'a 'b]]
       
       ))

(deftest analyze-sexpr-tree-test
  (are [sexpr sym-g sym-l errors]
       (= (analyze-sexpr-tree [[] [] []] (to-tokens sexpr))
          [sym-g sym-l errors])
       
       [:define 'a [:lambda ['a '42] 42]]
       ['a] [] ["Wrong arguments at lambda"]

       [:define 'a [:quote ['a 'b 'c] 42]]
       ['a] [] ["Wrong arguments count to quote"]

       [:define 'a 42 32]
       [] [] ["Wrong arguments amount to define (4)"]

       [:define 'c [:lambda ['a 'b] [:+ 'a 'b]]]
       ['c] [] []

       [:define 'c 42]
       ['c] [] []

       [:define 'a [:quote ['a 'b]]]
       ['a] [] []

       [:define 'c [:lambda ['a] [:+ 'r 'a]]]
       ['c] [] ["Undefined symbols: [r]"]
       
       ))

(deftest semantics-test
  (are [program]
       (= (to-tokens program) (-> program to-tokens semantics))

       [[:define 'c [:lambda ['a] [:+ 'a 3]]]]

       [[:define 'a 1] [:define 'b [:lambda ['x 'y] [:+ 'x 'y 'a]]]]

       ))       
      