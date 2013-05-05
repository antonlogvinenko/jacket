(ns small-jvm-lisp.test.lexer
  (:require [clojure.tools.reader.reader-types :as rt])
  (:use [clojure.test]
        [small-jvm-lisp.lexer]))

(deftest is-letter-test
  (are [char result] (= result (is-letter char))
       nil false
       \a true
       \b true
       \space false
       \1 false
       ))

(deftest read-literal-test
  (are [text word] (= (->> text rt/string-push-back-reader read-literal) word)
       "cake is a lie" "cake"
       " cake is a lie" ""
       "(cake is a lie" ""
        "" ""
       ))

(deftest matches-test
  (are [coll key result] (= result (matches coll key))
       [1 2 3] 1 true
       [1 2 3] 4 false
       1 1 true
       1 2 false
       ))

(deftest read-token-test
  (are [text token] (= token (-> text rt/string-push-back-reader read-token))
       "a" "a"
       "(" :LB
       ")" :RB
       "" nil
       "\t" nil
       "\r" nil
       "\n" nil

       "a asdasd (" "a"
       "a\t(asdasd asdas" "a"
       "a(b" "a"
       ))

(deftest tokenize-test
  (are [text tokens] (= tokens (tokenize text))
       "a b c" ["a" "b" "c"]
       "a ( b" ["a" :LB "b"]
       "a ) c" ["a" :RB "c"]

       "ab\rcd\tef\ngh ij" ["ab" "cd" "ef" "gh" "ij"]
       ))
