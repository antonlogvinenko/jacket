(ns small-jvm-lisp.lexer
  (:require [clojure.tools.reader.reader-types :as rt]))

(def LB :LB)
(def RB :RB)

(def KEYWORDS {"lambda" :lambda
               "def" :def
               "+" :plus "-" :minus
               "/" :divide
               "*" :multiply
               "car" :car "cdr" :cdr "fn" :fn "cons" :cons "quote" :quote
               "print" :print "read" :read
               })

;;better tokens info - map or data type?
;;exception messages

;;exceptions on unknown characters
;;lines numbers - change parser to read line by line

;;test errors

;;complete lexer overview
;;master test - tokenize-test
;;terminating cases for all methods


;;use automata, reduce code
;;weird string characters
;;weird numbers format


(defn matches [coll key]
  (if (coll? coll)
    (-> (partial = key) (some coll) nil? not)
    (= coll key)))

(defn letter? [ch]
  (if (nil? ch) false (Character/isLetter ch)))

(defn digit? [ch]
  (and (-> ch nil? not) (Character/isDigit ch)))

(defn word-symbol? [ch]
  (or (letter? ch) (matches [\+ \- \/ \*] ch)))

(defn whitespace? [ch]
  (or (nil? ch) (Character/isWhitespace ch)))

(defn peep [pred reader]
  (-> reader rt/peek-char pred))

(defn read-while [reader pred sb]
  (while (peep pred reader)
    (->> reader rt/read-char (.append sb)))
  sb)



(defn read-keyword [reader]
  (->> (StringBuffer.)
       (read-while reader word-symbol?)
       .toString
       keyword))

(defn read-boolean-constant [reader]
  (let [first-char (rt/read-char reader)
        second-char (rt/read-char reader)]
    (if (= \# first-char)
      (case second-char
        \t true
        \f false
        :else (throw (RuntimeException. "Oops")))
      (throw (RuntimeException. "Ooops")))))
        
(defn read-string-constant [reader]
  (if (-> reader rt/read-char (= \"))
    (let [sb (StringBuffer.)]
      (read-while reader (partial not= \") sb)
      (if (-> reader rt/read-char (not= \"))
        (throw (RuntimeException. "Ooops"))
        (.toString sb)))
    (throw (RuntimeException. "Ooops"))))
        

(defn read-number-constant [reader]
  (let [sb (StringBuffer.)]
    (read-while reader digit? sb)
    (condp peep reader
      whitespace? (->> sb .toString Integer/parseInt)
      (partial = \.) (do (->> reader rt/read-char (.append sb))
                         (read-while reader digit? sb)
                         (condp peep reader
                           whitespace? (->> sb .toString Double/parseDouble)
                           (throw (RuntimeException. "Ooops XD"))))
      (throw (RuntimeException. "Oops")))))

(defn read-literal [reader]
  (let [first-char (rt/peek-char reader)
        method (condp matches first-char
                 \# read-boolean-constant
                 \" read-string-constant
                 [\0 \1 \2 \3 \4 \5 \6 \7 \8 \9] read-number-constant
                 read-keyword)]
    (method reader)))

(defn read-token [reader]
  (loop [reader reader]
    (let [ch (rt/read-char reader)]
      (condp matches ch
        \( LB
        \) RB
        [\space \return \tab \newline] (recur reader)
        nil nil
        (do (rt/unread reader ch)
            (read-literal reader))))))

(defn tokenize [text]
  (loop [tokens [] reader (rt/string-push-back-reader text)]
    (let [token (read-token reader)]
      (if (nil? token)
        tokens
        (recur (conj tokens token) reader)))))