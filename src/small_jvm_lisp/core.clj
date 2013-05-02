(ns small-jvm-lisp.core
  (:use [small-jvm-lisp.lexer]))

(defn info []
  (println "A little hungry compiler"))

(defn -main [file & other]
  (info)
  (-> file slurp tokenize))

