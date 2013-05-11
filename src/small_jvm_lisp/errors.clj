(ns small-jvm-lisp.errors)

(defn raise-error [message]
  (->> message
       str
       RuntimeException.
       throw))