(ns jacket.core
    (:use compojure.core)
    (:require [compojure.handler :as handler]
              [compojure.route :as route]))

(defn info []
  (println "A little hungry compiler"))

(defroutes app-routes
  (GET "/" [] "<p>Hello from compojure</p>")
  
  (route/resources "/")
  
  (route/not-found "Page not found"))

(def handler
  (handler/site app-routes))

(defn -main [file & other]
  (info))

