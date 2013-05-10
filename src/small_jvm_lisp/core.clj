(ns small-jvm-lisp.core
  (:use [small-jvm-lisp.grammar]))

(defn info []
  (println "A little hungry compiler"))

(defn -main [file & other]
  (info)
  (-> file slurp tokenize))

