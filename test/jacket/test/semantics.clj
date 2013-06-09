(ns jacket.test.semantics
  (:use [clojure.test]
        [jacket.semantics]
        [jacket.lexer.fsm]
        )
  (:import [jacket.lexer.fsm Token])
  )

(def sexpr-z {:symtable [] :errors []})

(deftest is-sexpr?-test
  (are [expr pred] (-> expr to-tokens is-sexpr? pred)
       [:define 'a] true?
       'a false?
       
       ))

(deftest check-define-test
  (are [sexpr sym-g sym-l errors sexprs]
       (= (check-define (to-tokens sexpr))
          [sym-g sym-l errors sexprs])

       [:define 'b 42]
       ['b] ['b] [] [42]
       
       [:define 'b 42 42]
       [] [] ["Wrong arguments count to 'define' (4)"] []
       
       [:define 42 42]
       [] [] ["First 'define' argument must be symbol, not '42'"] [42]

       [:define 'a [:bla 100500]]
       ['a] ['a] [] [[:bla 100500]]
       ))

(deftest check-lambda-test
  (are [sexpr sym-g sym-l errors sexprs]
       (= (check-lambda (to-tokens sexpr))
          [sym-g sym-l errors sexprs])

       [:lambda ['a 'b] true]
       [] ['a 'b] [] [true]

       [:lambda ['a 'b] [:+ 'a 'c]]
       [] ['a 'b] [] [[:+ 'a 'c]]

       [:lambda ['a] true true]
       [] [] ["Wrong 'lambda' arguments count: 4"] []

       [:lambda ['a 42] true]
       [] [] ["Wrong lambda function arguments, must be symbols"] [true]
       ))

(deftest merge-states-test
  (are [states sym-g sym-l errors sexprs]
       (= (merge-states (to-tokens states))
          [sym-g sym-l errors sexprs])
       
       [['a 42] ['b 1] 32 [1 2]]
       [] ['a 'b] ["Pair must be a list of 2 elements" "Pair first element must be symbol"] [42 1 2]
       
  ))

(deftest check-pair-test
  (are [sexpr sym-g sym-l errors sexprs]
       (= (check-pair (to-tokens sexpr))
          [sym-g sym-l errors sexprs])

       ['a 42]
       [] ['a] [] [42]

       ['a 42 42]
       [] [] ["Pair must be a list of 2 elements"] [42]

       [42 'a]
       [] [] ["Pair first element must be symbol"] ['a]

       ['a [:+ 1 2]]
       [] ['a] [] [[:+ 1 2]] 

       42
       [] [] ["Pair must be a list of 2 elements"] []
       ))

(deftest check-let-test 
  (are [sexpr sym-g sym-l errors sexprs]
       (= (check-let (to-tokens sexpr))
          [sym-g sym-l errors sexprs])
       
       [:let [['a 41] ['b 1]] [:+ 'a 'b]]
       [] ['a 'b] [] [41 1 [:+ 'a 'b]]

       [:let [[52 1]] [:+ 1 1]]
       [] [] ["Pair first element must be symbol"] [1 [:+ 1 1]]

       [:let [42 ['a 4]] [:+ 1 1]]
       [] ['a] ["Pair must be a list of 2 elements"] [4 [:+ 1 1]]

       ))
       

(deftest check-quote-test
  (are [sexpr sym-g sym-l errors sexprs]
       (= (check-quote (to-tokens sexpr))
          [sym-g sym-l errors sexprs])

       [:quote ['a 'b] true]
       [] [] ["Quote may have a single argument"] []

       [:quote ['a 'b]]
       [] [] [] []

       ))

(deftest check-print-utility-test
  (are [sexpr sym-g sym-l errors sexprs]
    (= (check-print-utility (to-tokens sexpr))
       [sym-g sym-l errors sexprs])

    [:println]
    [] [] ["':println' requires at least a single argument"] []
    [:print]
    [] [] ["':print' requires at least a single argument"] []

    [:println 'a 'b]
    [] [] [] []

    [:print 'a 'b]
    [] [] [] []

    ))

(deftest check-read-utility-test
  (are [sexpr sym-g sym-l errors sexprs]
    (= (check-read-utility (to-tokens sexpr))
       [sym-g sym-l errors sexprs])

    [:readln 1]
    [] [] ["':readln' requires no arguments"] []
    [:read 1]
    [] [] ["':read' requires no arguments"] []

    [:readln]
    [] [] [] []

    [:read]
    [] [] [] []

    ))

(deftest check-dynamic-list-test
  (are [sexpr sym-g sym-l errors
        sym-g-out sym-l-out errors-out sexprs-out]
       (= (check-dynamic-list [sym-g sym-l errors] (to-tokens sexpr))
          [sym-g-out sym-l-out errors-out sexprs-out])

       ['a 3]
       [] [] []
       [] [] ["Illegal first token for s-expression: a"] []

       ['a [:+ 'a 3]]
       [] ['a] []
       [] [] [] [[:+ 'a 3]]

       ['b [:+ 41 1]]
       ['b] [] []
       [] [] [] [[:+ 41 1]]

       ['g [:+ 1 1]]
       [] [] []
       [] [] ["Illegal first token for s-expression: g"] [[:+ 1 1]]
       
       ))

(deftest check-sexpr-test
  (are [sexpr sym-g sym-l errors sexprs]
       (= (check-sexpr [] (to-tokens sexpr))
          [sym-g sym-l errors sexprs])

       []
       [] [] ["First token in s-expression must be function"] []
       
       [:define 'b 42]
       ['b] ['b] [] [42]

       [:define 42]
       [] [] ["Wrong arguments count to 'define' (2)"] []

       [:define 42 42]
       [] [] ["First 'define' argument must be symbol, not '42'"] [42]

       [:lambda ['a]]
       [] [] ["Wrong 'lambda' arguments count: 2"] []

       [:lambda ['a 42] 42]
       [] [] ["Wrong lambda function arguments, must be symbols"] [42]
       
       [:de 42]
       [] [] ["Illegal first token for s-expression: :de"] []

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
       [] [] ["Undefined symbol: :de"] []

       [[] [] []] 'abc
       [] [] ["Undefined symbol: abc"] []

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
       [] [] ["Undefined symbol: a"] []

       [['a] [] []] 'a
       [] [] [] []

       [] [:def 'a 43]
       [] [] ["Illegal first token for s-expression: :def"] []

       [] [:define 'a 43]
       ['a] ['a] [] [43]

       ))

(deftest analyze-sexpr-tree-test
  (are [sexpr sym-g sym-l errors]
       (= (analyze-sexpr-tree [[] [] []] (to-tokens sexpr))
          [sym-g sym-l errors])
       
       [:define 'a [:lambda ['a '42] 42]]
       ['a] [] ["Wrong lambda function arguments, must be symbols"]

       [:define 'a [:quote ['a 'b 'c] 42]]
       ['a] [] ["Quote may have a single argument"]

       [:define 'a 42 32]
       [] [] ["Wrong arguments count to 'define' (4)"]

       [:define 'c [:lambda ['a 'b] [:+ 'a 'b]]]
       ['c] [] []

       [:define 'c 42]
       ['c] [] []

       [:define 'a [:quote ['a 'b]]]
       ['a] [] []

       [:define 'c [:lambda ['a] [:+ 'r 'a]]]
       ['c] [] ["Undefined symbols in s-expression: [r]"]

       [:lambda ['a] [:let [['b 1] ['c 2]]
                      [:lambda ['d] [:+ 'a 'b 'c 'd]]
                      [:+ 'a 'b 'c]]]
       [] [] []

       [:lambda ['a] [:let [['b 1] ['c 2]]
                      [:lambda ['d] [:+ 'a 'b 'c 'd 'e]]
                      [:+ 'a 'b 'c 'd 'e]]]
       [] [] ["Undefined symbols in s-expression: [d e]" "Undefined symbols in s-expression: [e]"]
       
       ))


(deftest analyze-file-expr-test
  (are [sexpr sym-g sym-l errors]
       (= (analyze-file-expr [[] [] []] (to-tokens sexpr))
          [sym-g sym-l errors])

       [:define 'a 42]
       ['a] [] []

       [:define 'a 'b]
       ['a] [] ["Undefined symbol: b"]
        
       42
       [] [] ["Only s-expressions allowed in program top-level, found: 42"]
       
       ))
          

(deftest semantics-test
  (are [program]
       (= (to-tokens program) (-> program to-tokens semantics))

       [[:define 'c [:lambda ['a] [:+ 'a 3]]]]

       [[:define 'a 1] [:define 'b [:lambda ['x 'y] [:+ 'x 'y 'a]]]]

       ))       

