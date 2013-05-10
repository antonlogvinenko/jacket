(ns small-jvm-lisp.errors)

(defn raise-error [description]
  (->> description
       str
       RuntimeException.
       throw))