(ns small-jvm-lisp.lexer
  (:require [clojure.tools.reader.reader-types :as rt]))

(def LB :LB)
(def RB :RB)

(defn is-letter [ch]
  (if (nil? ch) false (Character/isLetter ch)))

(defn read-word [reader]
  (let [word (StringBuffer.)]
    (while (is-letter (rt/peek-char reader))
      (.append word (rt/read-char reader)))
    word))

(defn read-token [reader]
  (loop [reader reader]
    (let [ch (rt/peek-char reader)]
      (condp = ch
        \( (do (rt/read-char reader) LB)
        \) (do (rt/read-char reader) RB)
        \space (do (rt/read-char reader)
                (recur reader))
        nil nil
        (read-word reader)))))

(defn tokenize [text]
  (loop [tokens [] reader (rt/string-push-back-reader text)]
    (let [token (read-token reader)]
      (if (nil? token)
        tokens
        (recur (conj tokens token) reader)))))
      
    
    