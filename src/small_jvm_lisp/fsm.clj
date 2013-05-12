(ns small-jvm-lisp.fsm
  (:use [small-jvm-lisp.errors])
  (:require [clojure.tools.reader.reader-types :as rt]))

(defn skip-all [sb reader f]
  nil)
(defn return-char [sb reader char]
  (rt/unread reader char)
  sb)
(defn append [sb reader char]
  (.append sb char))
(defn skip-char [sb reader char]
  sb)

(def eof? nil?)

(defn parse-integer [sb]
  (-> sb .toString Integer/parseInt))
(defn parse-double [sb]
  (-> sb .toString Double/parseDouble))
(defn parse-string [sb]
  (-> sb .toString))

(defn transition-ok? [ch t]
  (cond (char? t) (= t ch)
        (fn? t) (t ch)
        (vector? t) (->> t
                         (map (partial transition-ok? ch))
                         (some true?)
                         boolean)
        :else false))

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

(defn raise-lexical-error [reader position ch state lexem]
  (raise-at-pos position
                (str "Lexical error: "
                     "encountered character '" ch "' "
                     "while building lexem \"" lexem "\""
                     "(character at line " (rt/get-line-number reader) ", "
                     "column " (dec (rt/get-column-number reader)) ")")))

(defn get-position [reader]
  [(rt/get-line-number reader)
   (-> reader rt/get-column-number dec)])

(defprotocol TokenProtocol
  (equals [this])
  (toString [this])
  (is? [this pred]))

(deftype Token [value line column]
  TokenProtocol
  (toString [this] (str "t/" value "/"))
  (equals [this v]
    (if (-> v type (= Token))
      (= (.value v) value)
      (= value v)))
  (is? [this pred] (pred value)))

(defn to-tokens [ts]
  (if (vector? ts)
    (->> ts (map to-tokens) vec)
    (Token. ts 1 1)))

(defn tokenize-with-grammar [grammar reader]
  (loop [state :done accum nil reader reader tokens [] pos [0 0]]
    (let [tokens (if (and (= state :done) (-> accum nil? not))
                   (conj tokens (Token. accum (first pos) (second pos))) tokens)
          accum (if (= state :done) (StringBuffer.) accum)
          ch (rt/read-char reader)]
      (if (and (= state :done) (nil? ch))
        tokens
        (let [pos (if (= state :done) (get-position reader) pos)
              transition (-> grammar state (find-transition ch))]
          (if (nil? transition)
            (raise-lexical-error reader pos ch state accum)
            (let [next-state (first transition)
                  next-accum (modify accum reader ch (rest transition))]
              (recur next-state next-accum reader tokens pos))))))))