(ns small-jvm-lisp.errors)

(defn raise [message]
  (->> str
       message
       RuntimeException.
       throw))

(defn raise-at [where message]
  (raise (str where message)))

(defn raise-at-token [token message]
  (raise-at (str "At token '" (.value token) "', "
                 "line " (.line token)
                 ", column " (.column token)
                 ":\n")
            message))

(defn raise-at-pos [pos message]
  (raise-at (str "At line " (first pos)
                 ", column " (second pos)
                 ":\n")
            message))