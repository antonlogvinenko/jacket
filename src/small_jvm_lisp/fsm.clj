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

(defn raise-lexical-error [reader ch state tokens lexem]
  (let [line-num (rt/get-line-number reader)
        column-num (rt/get-column-number reader)]
    (raise-error (str "Lexical error: "
                      "line " line-num ", "
                      "column " column-num ", "
                      "character '" ch "', "
                      "building lexem \"" lexem "\", "
                      "tokens " tokens ", "))))

(defn tokenize-with-grammar [grammar reader]
  (loop [state :done accum nil reader reader tokens []]
    (let [tokens (if (and (= state :done) (-> accum nil? not))
                   (conj tokens accum) tokens)
          accum (if (= state :done) (StringBuffer.) accum)
          ch (rt/read-char reader)]
      (if (and (= state :done) (nil? ch))
        tokens
        (let [transition (-> grammar state (find-transition ch))]
          (if (nil? transition)
            (raise-lexical-error reader ch state tokens accum)
            (let [next-state (first transition)
                  next-accum (modify accum reader ch (rest transition))]
              (recur next-state next-accum reader tokens))))))))