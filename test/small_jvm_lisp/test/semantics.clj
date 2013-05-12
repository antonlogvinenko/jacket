(ns small-jvm-lisp.test.semantics
  (:use [clojure.test]
        [small-jvm-lisp.semantics]
        [small-jvm-lisp.fsm]
        )
  (:import [small_jvm_lisp.fsm Token])
  )

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
       
       )
  )
       
       
       