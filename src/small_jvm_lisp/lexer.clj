(ns small-jvm-lisp.lexer
  (:use [small-jvm-lisp.fsm])
  (:require [clojure.tools.reader.reader-types :as rt]))

(def KEYWORDS ["-" "*" "+" "=" "!=" "<" ">" ">=" "<="
               "define" "lambda" "let"
               "and" "or" "not"
               "car" "cdr" "cons" "quote"
               "print" "read"])

(defn matches [coll key]
  (if (coll? coll)
    (-> (partial = key) (some coll) nil? not)
    (= coll key)))

(defn letter? [ch]
  (if (eof? ch) false (Character/isLetter ch)))
(defn digit? [ch]
  (and (-> ch nil? not) (Character/isDigit ch)))
(defn whitespace? [ch]
  (or (eof? ch)  (Character/isWhitespace ch)))
(defn separator? [ch]
  (transition-ok? ch [\( \) whitespace? eof? \"]))
(defn word-symbol? [ch]
  (-> ch separator? not))

(defn keywordize [sb]
  (let [str (.toString sb)]
    (if (some (partial = str) KEYWORDS)
      (keyword str)
      (symbol str))))

(def grammar {:done {\( [:done :LB]
                     \) [:done :RB]
                     \" [:string skip-char]
                     whitespace? [:done skip-all]
                     \# [:boolean skip-char]
                     digit? [:integer append]
                     letter? [:name append]
                     [\!] [:not-equal append]
                     [\< \>] [:strict-inequality append]
                     [\+ \\ \* \=] [:keyword append]
                     \- [:minus append]}

              :minus {digit? [:integer append]
                      separator? [:done return-char keywordize]}
              
              :not-equal {\= [:done append keywordize]}

              :strict-inequality {separator? [:done return-char keywordize]
                                  \= [:non-strict-inequality append]}

              :non-strict-inequality {separator? [:done return-char keywordize]}

              :string {letter? [:string append]
                       \" [:done skip-char parse-string]
                       [\tab \newline \return] [:string append]}
              
              :boolean {\t [:boolean true]
                        \f [:boolean false]
                        separator? [:done return-char]}

              :integer {digit? [:integer append]
                        \. [:double append]
                        separator? [:done return-char parse-integer]}

              :double {digit? [:double append]
                       separator? [:done return-char parse-double]}
              
              :name {word-symbol? [:name append]
                     separator? [:done return-char keywordize]}

              :keyword {separator? [:done return-char keywordize]}
              })

(defn tokenize [string]
  (->> string
       rt/string-push-back-reader
       rt/indexing-push-back-reader
       (tokenize-with-grammar grammar)))