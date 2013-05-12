(ns small-jvm-lisp.test.semantics
  (:use [clojure.test]
        [small-jvm-lisp.semantics]
        [small-jvm-lisp.fsm]
        ))

(deftest is-sexpr?-test
  (are [expr pred] (-> expr to-tokens is-sexpr? pred)
       [:def 'a] true?
       'a false?
       
       ))

(deftest check-define-test
  (are [symtable-in sexpr-in errors symtable]
       (= {:symtable symtable :errors errors}
          (check-define symtable-in (to-tokens sexpr-in)))

;;       ['a] [:def 'b 42]
  ;;     [] ['a 'b]

       ))
       
       
       