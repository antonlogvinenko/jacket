(ns jacket.connect
  (:require [clojure.browser.repl :as repl]))

(defn cn []
    (repl/connect "http://localhost:9000/repl"))

(cn)
