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
  (are [symtable-in sexpr-in symtable errors]
       (= {:symtable (to-tokens symtable)}
          (check-define (to-tokens symtable-in) (to-tokens sexpr-in)))

       ['a] [:def 'b 42]
       ['b]

       )

  (are [symtable-in sexpr-in errors]
       (= {:errors errors}
          (check-define (to-tokens symtable-in) (to-tokens sexpr-in)))

       ['a] [:def 'b 42 42]
       ["Wrong arguments amount to def (4)"]

       ['a] [:def 42 42]
       ["Not a symbol (42)"]
       ))

(deftest check-lambda-test
  (are [sexpr-in symtable]
       (= {:symtable (to-tokens symtable)} (check-lambda [] (to-tokens sexpr-in)))

       [:lambda ['a 'b] true]
       ['a 'b]
       )

  (are [sexpr-in errors]
       (= {:errors errors} (check-lambda [] (to-tokens sexpr-in)))

       [:lambda ['a] true true]
       ["Wrong arguments amount to lambda (4)"]

       [:lambda ['a 42] true]
       ["Wrong arguments at lambda"]
       )
  )

(deftest analyze-sexpr-test
  (are [sexpr global]
       (= (to-tokens global)
          (first (analyze-sexpr [] [] (to-tokens sexpr))))

       [:def 'b 42] ['b]
       )

  (are [sexpr errors]
       (= errors
          (:errors (second (analyze-sexpr [] [] (to-tokens sexpr)))))

       [:def 42]
       ["Wrong arguments amount to def (2)"]

       [:def 42 42]
       ["Not a symbol (42)"]

       [:lambda ['a]]
       ["Wrong arguments amount to lambda (2)"]

       [:lambda ['a 42] 42]
       ["Wrong arguments at lambda"]
       
       [:de 42]
       ["Illegal first token for s-expression"]
       
       )
  )

(deftest analyze-sexpr-tree-test
  (are [sexpr-tree errors symtable]
       (= errors
          (->> sexpr-tree to-tokens (analyze-sexpr-tree sexpr-z) :errors))

       [:def 'a [:lambda ['a '42] 42]]
       ["Wrong arguments at lambda"]
       []

       [:def 'a [:quote ['a 'b 'c] 42]]
       ["Wrong arguments count to quote"]
       []
       )
  
  (are [sexpr-tree errors]
       (= errors
          (->> sexpr-tree to-tokens (analyze-sexpr-tree sexpr-z) :errors))

       [:def 'a 42 32]
       ["Wrong arguments amount to def (4)"]
       )       

  (are [sexpr-tree symtable]
       (= (to-tokens symtable)
          (->> sexpr-tree to-tokens (analyze-sexpr-tree sexpr-z) :symtable))

       [:def 'c [:lambda ['a 'b] [:+ 'a 'b]]]
       [[] ['a 'b]]

       [:def 'c 42]
       []

       [:def 'a [:quote ['a 'b]]]
       [[]]

       )
  )

(deftest semantics-test
  (are [program]
       (= (to-tokens program) (-> program to-tokens semantics))

       [42]

       [[:def 'c [:lambda ['a] [:+ 'a 3]]] 42]

       ))       
      