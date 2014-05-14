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
    (s (comp g f) y)))

(defn filter [s p]
  (fn [g y]
    (s (fn [x] (if (p x) (g x) (- y 1))) y)))

(def s1 (set 1)) ; 1
(def s2 (set 2)) ; 2
(def s3 (set 3)) ; 3
(def s4 (set 4)) ; 4
(def s12 (union s1 s2)) ; 1 2
(def s34 (union s3 s4)) ; 3 4
(def s (union s12 s34)) ; 1 2 3 4
(def sf (filter s (partial < 2))) ; 3 4
(def mf (map sf (partial * 2))) ; 6 8

