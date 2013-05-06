(ns small-jvm-lisp.test.lexer
  (:require [clojure.tools.reader.reader-types :as rt])
  (:use [clojure.test]
        [small-jvm-lisp.lexer]))

(deftest letter?-test
  (are [char result] (= result (letter? char))
       nil false
       \a true
       \b true
       \space false
       \1 false
       ))

(deftest digit?-test
  (are [chars pred] (->> chars (map digit?) (every? pred))
       [\0 \1 \2 \3 \4 \5 \6 \7 \8 \9] true?
       [\a \space \tab \\ \"] false?
       ))

(deftest whitespace?-test
  (are [chars pred] (->> chars (map whitespace?) (every? pred))
       [\space nil \tab \return \newline] true?
       [\1 \a \\ \=] false?
       ))

(deftest matches-test
  (are [coll key result] (= result (matches coll key))
       [1 2 3] 1 true
       [1 2 3] 4 false
       1 1 true
       1 2 false
       ))



(deftest read-boolean-constant-test)

(deftest read-string-constant-test
  (are [text result] (->> text (rt/string-push-back-reader) read-string-constant (= result))
       "\"cake\"" "cake"))

(deftest read-number-constant-test
  (are [text number] (->> text (rt/string-push-back-reader) read-number-constant
                          (= number))
       "3" 3
       "4." 4.0
       "4.1" 4.1
       ))

(deftest read-keyword-test
  (are [text keyword] (->> text (rt/string-push-back-reader) read-keyword (= keyword))
       "asd" :asd
       "lambda" :lambda "def" :def "+" :+ "print" :print
       ))

(deftest read-literal-test
  (are [text word] (= (->> text rt/string-push-back-reader read-literal) word)
       "cake is a lie" :cake
       ))

(deftest read-token-test
  (are [text token] (= token (-> text rt/string-push-back-reader read-token))
       "a" :a
       "(" :LB
       ")" :RB
       "" nil
       "\t" nil
       "\r" nil
       "\n" nil

       "a asdasd (" :a
       "a\t(asdasd asdas" :a
       "a(b" :a
       ))

(deftest tokenize-test
  (are [text tokens] (= tokens (tokenize text))
       "a b c" [:a :b :c]
       "a ( b" [:a :LB :b]
       "a ) c" [:a :RB :c]

       "(def a (fn (a b) (+ a b)))"
       [:LB :def :a :LB :fn :LB :a :b :RB :LB :+ :a :b :RB :RB :RB]

       "ab\rcd\tef\ngh ij" [:ab :cd :ef :gh :ij]
       ))
