(ns small-jvm-lisp.lexer
  (:require [clojure.tools.reader.reader-types :as rt]))

(def KEYWORDS {"lambda" :lambda
               "def" :def
               "+" :plus "-" :minus
               "/" :divide
               "*" :multiply
               "car" :car "cdr" :cdr "fn" :fn "cons" :cons "quote" :quote
               "print" :print "read" :read
               })

;;terminating cases for all methods + tests
;;automatic exceptions on unknown characters + tests
;;add line numbers at all exceptions
;;test errors

;;parsing keywords
;;weird string characters

;;complete lexer overview;
;;master test - tokenize-test

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
  (or (nil? ch)  (Character/isWhitespace ch)))

  
(defn append [sb reader char]
  (.append sb char))
(defn skip-char [sb reader char]
  sb)
(defn parse-integer [sb]
  (-> sb .toString Integer/parseInt))
(defn parse-double [sb]
  (-> sb .toString Double/parseDouble))
(defn keywordize [sb]
  (-> sb .toString keyword))
(defn skip-all [sb reader f]
  nil)
(defn return-char [sb reader char]
  (rt/unread reader char)
  sb)

(def grammar {:done {\( [:done :LB]
                     \) [:done :RB]
                     \" [:string skip-char]
                     whitespace? [:done skip-all]
                     \# [:boolean skip-char]
                     digit? [:integer append]
                     word-symbol? [:name append]}
              
              :string {letter? [:string append]
                       \" [:done skip-char #(.toString %)]
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
              })

(defn transition-ok? [ch t]
  (cond (char? t) (= t ch)
        (fn? t) (t ch)
        (vector? t) (->> t
                         (map (partial transition-ok? ch))
                         (some true?)
                         boolean)
        :else false))

(defn separator? [ch]
  (transition-ok? ch [\( \) whitespace? eof?]))

(defn find-transition [transitions ch]
  (->> transitions
       keys
       (filter (partial transition-ok? ch))
       first
       transitions))
  
(defn modify [accum reader ch modifications]
  (if (-> modifications first fn? not)
    (first modifications)
    ((apply comp (reverse modifications)) accum reader ch)))

(defn tokenize-with-grammar [grammar reader]
  (loop [state :done accum nil reader reader tokens []]
    (let [tokens (if (and (= state :done) (-> accum nil? not))
                   (conj tokens accum) tokens)
          accum (if (= state :done) (StringBuffer.) accum)
          ch (rt/read-char reader)]
      (if (and (= state :done) (nil? ch))
        tokens
        (let [transition (-> grammar state (find-transition ch))
              next-state (first transition)
              next-accum (modify accum reader ch (rest transition))]
          (recur next-state next-accum reader tokens))))))

(defn tokenize [string]
  (->> string
       rt/string-push-back-reader
       (tokenize-with-grammar grammar)))