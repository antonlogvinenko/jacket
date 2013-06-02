(ns jacket.test.codegen
  (:use [jacket.codegen.codegen]
        [clojure.test]
        [clojure.java.shell]
        [clojure.string :only [split]]))

(defn run-program []
  (let [result (->> "./hello-world.sh"
                    sh
                    (with-sh-dir ".")
                    :out)]
    (-> result (split #"\n") last)))


(deftest hello-world-test
  (are [result]
    (do (gen-hello-world)
        (= result (run-program)))

    "Hey hey!!!"


    ))


