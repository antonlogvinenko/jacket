(ns small-jvm-lisp.lexer
  (:require [clojure.tools.reader.reader-types :as rt]))

(def LB :LB)
(def RB :RB)

;;write tests for all functions
;;more whitespaces?
;;exceptions on unknown characters
;;/string, integer, float contstants
;;keywords - def lambda + - / * . readln println ' cons car cdr fn
;;better tokens info - map or data type?
;;lines numbers - change parser to read line by line

(defn is-letter [ch]
  (if (nil? ch) false (Character/isLetter ch)))

(defn read-word [reader]
  (let [word (StringBuffer.)]
    (while (->> reader rt/peek-char is-letter)
      (->> reader rt/read-char (.append word)))
    word))

(defn matches [coll key]
  (if (coll? coll)
    (some (partial = key) coll)
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
            (read-word reader))))))

(defn tokenize [text]
  (loop [tokens [] reader (rt/string-push-back-reader text)]
    (let [token (read-token reader)]
      (if (nil? token)
        tokens
        (recur (conj tokens token) reader)))))
      
    
    