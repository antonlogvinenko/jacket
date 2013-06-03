(ns jacket.test.lexer
  (:require [clojure.tools.reader.reader-types :as rt])
  (:use [clojure.test]
        [jacket.lexer.fsm]
        [jacket.lexer.lexer]))

(deftest letter?-test
  (are [char result] (= result (letter? char))
       nil false
       \a true
       \b true
       \space false
       \1 false
       ))

(deftest eof?-test
  (are [char result] (= result (eof? char))
       nil true
       42 false))

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

(deftest keywordize-test
  (are [str result] (= result (keywordize str))
       "define" :define
       "asd" 'asd))

(deftest matches-test
  (are [coll key result] (= result (matches coll key))
       [1 2 3] 1 true
       [1 2 3] 4 false
       1 1 true
       1 2 false
       ))

(deftest read-boolean-constant-test
  (are [text result] (-> text tokenize first (= result))
       "#t" true
       "#f" false))


(deftest read-string-constant-test
  (are [text result] (-> text tokenize first (= result))
       "\"cake\"" "cake"
       "\"42\"" "42"
       "\"the answer 42\"" "the answer 42"
       ))

(deftest read-number-constant-test
  (are [text number] (-> text tokenize first (= number))
       "3" 3
       "4." 4.0
       "4.1" 4.1
       ))

(deftest read-keyword-test
  (are [text keyword] (-> text tokenize first (= keyword))
       "asd" 'asd
       "lambda" :lambda "define" :define "+" :+ "print" :print
       ))

(deftest read-literal-test
  (are [text word] (-> text tokenize first (= word))
       "cake is a lie" 'cake
       ))

(deftest read-token-test
  (are [text token] (-> text tokenize first (= token))
       "a" 'a
       "(" :LB
       ")" :RB
       "" nil
       "\t" nil
       "\r" nil
       "\n" nil

       "a asdasd (" 'a
       "a\t(asdasd asdas" 'a
       "a(b" 'a
       ))

(deftest tokenize-test
  (are [text tokens] (-> text tokenize (= tokens))
       "a b c" ['a 'b 'c]
       "a ( b" ['a :LB 'b]
       "a ) c" ['a :RB 'c]

       "(define a (lambda (a b) (+ a b)))"
       [:LB :define 'a :LB :lambda :LB 'a 'b :RB :LB :+ 'a 'b :RB :RB :RB]

       "ab\rcd\tef\ngh ij" ['ab 'cd 'ef 'gh 'ij]

       "(define sum (a b) (+ a b))"
       [:LB :define 'sum :LB 'a 'b :RB :LB :+ 'a 'b :RB :RB]

       "- 1 2"
       [:- 1 2]

       "-1 -1.2"
       [-1 -1.2]
       
       "\"\n\"define\"asd\"4.56\"s\")lambda+4.56lambda\r\"\"lambda()quote"
       ["\n" :define "asd" 4.56 "s" :RB 'lambda+4.56lambda "" :lambda :LB :RB :quote]
       ))
