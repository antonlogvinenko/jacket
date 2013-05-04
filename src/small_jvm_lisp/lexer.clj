(ns small-jvm-lisp.lexer
  (:require [clojure.tools.reader.reader-types :as rt]))

(def LB :LB)
(def RB :RB)


;;read-string-constant
;;read-number-constant
;;read and validate keywords - def lambda + - / * . readln println ' cons car cdr fn
;;test all read-literal cases and subfunctions

;;exceptions on unknown characters
;;exceptions at all

;;better tokens info - map or data type?
;;lines numbers - change parser to read line by line

(defn is-letter [ch]
  (if (nil? ch) false (Character/isLetter ch)))

;;make read-word from read-literal
;;parsing #t #f numbers strings, then word if created, then exception
(defn read-keyword [reader]
  (let [word (StringBuffer.)]
    (while (->> reader rt/peek-char is-letter)
      (->> reader rt/read-char (.append word)))
    (.toString word)))

(defn read-boolean-constant [reader]
  (let [first-char (rt/read-char reader)
        second-char (rt/read-char reader)]
    (if (= \# first-char)
      (case second-char
        \t :true
        \f :false
        :else (throw (RuntimeException. "Oops")))
      (throw (RuntimeException. "Ooops")))))
        
(defn read-string-constant [reader])
(defn read-number-constant [reader])

(defn read-literal [reader]
  (let [first-char (rt/peek-char reader)
        method (condp matches first-char
                 \# read-boolean-constant
                 \" read-string-constant
                 [\0 \1 \2 \3 \4 \5 \6 \7 \8 \9] read-number-constant
                 read-keyword)]
    (method reader)))

(defn matches [coll key]
  (if (coll? coll)
    (-> (partial = key) (some coll) nil? not)
    (= coll key)))

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
      
    
    