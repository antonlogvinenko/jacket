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
       [:def 'a] true?
       'a false?
       
       ))

(deftest check-define-test
  (are [sexpr sym-g sym-l errors sexprs]
       (= (check-define [] (to-tokens sexpr))
          [sym-g sym-l errors sexprs])

       [:def 'b 42]
       ['b] ['b] nil nil
       
       [:def 'b 42 42]
       nil nil ["Wrong arguments amount to def (4)"] nil
       
       [:def 42 42]
       nil nil ["Not a symbol (42)"] nil
       ))

(deftest check-lambda-test
  (are [sexpr sym-g sym-l errors sexprs]
       (= (check-lambda [] (to-tokens sexpr))
          [sym-g sym-l errors sexprs])

       [:lambda ['a 'b] true]
       nil ['a 'b] nil nil

       [:lambda ['a] true true]
       nil nil ["Wrong arguments amount to lambda (4)"] nil

       [:lambda ['a 42] true]
       nil nil ["Wrong arguments at lambda"] nil
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

       [:def 'b 42]
       ['b] ['b] nil nil

       [:def 42]
       nil nil ["Wrong arguments amount to def (2)"] nil

       [:def 42 42]
       nil nil ["Not a symbol (42)"] nil

       [:lambda ['a]]
       nil nil ["Wrong arguments amount to lambda (2)"] nil

       [:lambda ['a 42] 42]
       nil nil ["Wrong arguments at lambda"] nil
       
       [:de 42]
       nil nil ["Illegal first token for s-expression"] nil

       [:def 'a 42]
       ['a] ['a] nil nil

       [:lambda ['a 'b] [:+ 'a 'b]]
       nil ['a 'b] nil [[:+ 'a 'b]]
       
       ))

(deftest analyze-sexpr-tree-test
  (are [sexpr sym-g sym-l errors]
       (= (analyze-sexpr-tree [[] [] []] (to-tokens sexpr))
          [sym-g sym-l errors])
       
       [:def 'a [:lambda ['a '42] 42]]
       ['a] [] ["Wrong arguments at lambda"]

       [:def 'a [:quote ['a 'b 'c] 42]]
       ['a] [] ["Wrong arguments count to quote"]

       [:def 'a 42 32]
       [] [] ["Wrong arguments amount to def (4)"]

       [:def 'c [:lambda ['a 'b] [:+ 'a 'b]]]
       ['c] [] []

       [:def 'c 42]
       ['c] [] []

       [:def 'a [:quote ['a 'b]]]
       ['a] [] []

       ))

(deftest semantics-test
  (are [program]
       (= (to-tokens program) (-> program to-tokens semantics))

       [42]

       [[:def 'c [:lambda ['a] [:+ 'a 3]]] 42]

       ))       
      