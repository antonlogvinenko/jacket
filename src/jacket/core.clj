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



(defn set [x]
  (fn [g y] (= y (g x))))

(defn union [s1 s2]
  (fn [g y]
    (or (s1 g y) (s2 g y))))

(defn exists [s p]
  (s p true))

(defn map [s f]
  (fn [g y]
    (exists s (comp g f))))

(defn filter [s f]
  (fn [g y]
    (and (f y) (s g y))))

