(ns jacket.test.codegen.codegen
  (:use [jacket.codegen.codegen]
        [clojure.test]
        [clojure.java.shell]
        [clojure.string :only [split]]))

(->> "./compile-libs.sh" sh (with-sh-dir "."))
;;(precompile-libraries "jasm-impl")


(defn run-program [name]
  (let [result (->> (sh  "./run-program.sh" name)
                    (with-sh-dir ".")
                    :out)]
    (-> result (split #"\n") last)))

(deftest hello-world-test
  (are [result]
    (do (gen-hello-world)
        (= result (run-program "HelloWorld")))

    "Hey hey!!!"
    ))

(deftest run-program-test
  (let [location "wardrobe/"]
    (are [name result]
      (do (compile-jacket (str "test-programs/" name ".jt") location name)
          (= result (run-program name)))

      "println" "cake"
      "println-answer" "the answer"
      "println-sum-float" "3.0"
      "println-sum-int" "3"
      "println-sum-n" "6.0"
      "println-sum-1" "1"
      "println-n-arity" "Cake is a 42.0 lie"

      )))
