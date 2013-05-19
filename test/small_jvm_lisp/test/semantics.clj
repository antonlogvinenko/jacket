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
       ['b] ['b] [] [42]
       
       [:define 'b 42 42]
       [] [] ["Wrong arguments amount to define (4)"] []
       
       [:define 42 42]
       [] [] ["Not a symbol (42)"] [42]

       [:define 'a [:bla 100500]]
       ['a] ['a] [] [[:bla 100500]]
       ))

(deftest check-lambda-test
  (are [sexpr sym-g sym-l errors sexprs]
       (= (check-lambda [] (to-tokens sexpr))
          [sym-g sym-l errors sexprs])

       [:lambda ['a 'b] true]
       [] ['a 'b] [] [true]

       [:lambda ['a 'b] [:+ 'a 'c]]
       [] ['a 'b] [] [[:+ 'a 'c]]

       [:lambda ['a] true true]
       [] [] ["Wrong arguments amount to lambda (4)"] []

       [:lambda ['a 42] true]
       [] [] ["Wrong arguments at lambda"] [true]
       ))

(deftest merge-states-test
  (are [states sym-g sym-l errors sexprs]
       (= (merge-states (to-tokens states) [[][][][]])
          [sym-g sym-l errors sexprs])
       
       [['a 42] ['b 1] 32 [1 2]]
       [] ['a 'b] ["Must be a list" "Must be token"] [42 1 2]
       
  ))

(deftest check-pair-test
  (are [sexpr sym-g sym-l errors sexprs]
       (= (check-pair [] (to-tokens sexpr))
          [sym-g sym-l errors sexprs])

       ['a 42]
       [] ['a] [] [42]

       ['a 42 42]
       [] [] ["Wrong arguments for let"] [42]

       [42 'a]
       [] [] ["Must be token"] ['a]

       ['a [:+ 1 2]]
       [] ['a] [] [[:+ 1 2]]

       42
       [] [] ["Must be a list"] []
       ))

(deftest check-let-test 
  (are [sexpr sym-g sym-l errors sexprs]
       (= (check-let [] (to-tokens sexpr))
          [sym-g sym-l errors sexprs])
       
       [:let [['a 41] ['b 1]] [:+ 'a 'b]]
       [] ['a 'b] [] [41 1 [:+ 'a 'b]]

       [:let [[52 1]] [:+ 1 1]]
       [] [] ["Must be token"] [1 [:+ 1 1]]

       [:let [42 ['a 4]] [:+ 1 1]]
       [] ['a] ["Must be a list"] [4 [:+ 1 1]]
       
       ))
       

(deftest check-quote-test
  (are [sexpr sym-g sym-l errors sexprs]
       (= (check-quote [] (to-tokens sexpr))
          [sym-g sym-l errors sexprs])

       [:quote ['a 'b] true]
       [] [] ["Wrong arguments count to quote"] []

       [:quote ['a 'b]]
       [] [] [] []

       ))

(deftest check-dynamic-list-test
  (are [sexpr sym-g sym-l errors
        sym-g-out sym-l-out errors-out sexprs-out]
       (= (check-dynamic-list [sym-g sym-l errors] (to-tokens sexpr))
          [sym-g-out sym-l-out errors-out sexprs-out])

       ['a 3]
       [] [] []
       [] [] ["Illegal first token for s-expression"] []

       ['a [:+ 'a 3]]
       [] ['a] []
       [] [] [] [[:+ 'a 3]]

       ['b [:+ 41 1]]
       ['b] [] []
       [] [] [] [[:+ 41 1]]

       ['g [:+ 1 1]]
       [] [] []
       [] [] ["Illegal first token for s-expression"] [[:+ 1 1]]
       
       ))

(deftest check-sexpr-test
  (are [sexpr sym-g sym-l errors sexprs]
       (= (check-sexpr [] (to-tokens sexpr))
          [sym-g sym-l errors sexprs])

       []
       [] [] ["expected a function"] []
       
       [:define 'b 42]
       ['b] ['b] [] [42]

       [:define 42]
       [] [] ["Wrong arguments amount to define (2)"] []

       [:define 42 42]
       [] [] ["Not a symbol (42)"] [42]

       [:lambda ['a]]
       [] [] ["Wrong arguments amount to lambda (2)"] []

       [:lambda ['a 42] 42]
       [] [] ["Wrong arguments at lambda"] [42]
       
       [:de 42]
       [] [] ["Illegal first token for s-expression"] []

       [:define 'a 42]
       ['a] ['a] [] [42]

       [:lambda ['a 'b] [:+ 'a 'b]]
       [] ['a 'b] [] [[:+ 'a 'b]]
       
       ))

(deftest check-atom-test
  (are [state sexpr sym-g sym-l errors sexprs]
       (= (check-atom state (to-tokens sexpr))
          [sym-g sym-l errors sexprs])
       
       [[] [] []] :de
       [] [] ["Undefined symbol :de"] []

       [[] [] []] 'abc
       [] [] ["Undefined symbol abc"] []

       [[] ['abcde] []] 'abcde
       [] [] [] []
       
       ))

(deftest check-expr-test
  (are [state sexpr sym-g sym-l errors sexprs]
       (= (check-expr state (to-tokens sexpr))
          [sym-g sym-l errors sexprs])

       [] 42
       [] [] [] []

       [] 'a
       [] [] ["Undefined symbol a"] []

       [['a] [] []] 'a
       [] [] [] []

       [] [:def 'a 43]
       [] [] ["Illegal first token for s-expression"] []

       [] [:define 'a 43]
       ['a] ['a] [] [43]

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


(deftest analyze-file-expr-test
  (are [sexpr sym-g sym-l errors]
       (= (analyze-file-expr [] (to-tokens sexpr))
          [sym-g sym-l errors])

       [:define 'a 42]
       ['a] [] []

       [:define 'a 'b]
       ['a] [] ["Undefined symbol b"]
        
       42
       [] [] ["What is that 42"]
       
       ))
          

(deftest semantics-test
  (are [program]
       (= (to-tokens program) (-> program to-tokens semantics))

       [[:define 'c [:lambda ['a] [:+ 'a 3]]]]

       [[:define 'a 1] [:define 'b [:lambda ['x 'y] [:+ 'x 'y 'a]]]]

       ))       
