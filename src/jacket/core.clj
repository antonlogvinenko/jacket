(ns jacket.core
  (:use [jacket.lexer]))

(defn info []
  (println "A little hungry compiler"))

(defn -main [file & other]
  (info)
  (-> file slurp tokenize))

