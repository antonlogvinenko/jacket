(ns jacket.test.codegen.codegen
  (:use [jacket.codegen.codegen]
        [clojure.test]
        [clojure.java.shell]
        [clojure.string :only [split]]))

(->> "./compile-libs.sh" sh (with-sh-dir "."))
;;(precompile-libraries "jasm-impl")


(defn run-program [name in]
  (let [result (->> (sh "./run-program.sh" name :in in)
                    (with-sh-dir ".")
                    :out)]
    (-> result (split #"\n") last)))

(deftest hello-world-test
  (are [result]
    (do (gen-hello-world)
        (= result (run-program "HelloWorld" [])))

    "Hey hey!!!"
    ))

(deftest run-program-test
  (let [location "wardrobe/"]
    (are [name in result]
      (do (compile-jacket (str "test-programs/" name ".jt") location name)
          (= result (run-program name in)))

      "bool-and" "" "true false"
      "bool-or" "" "true false"
      "bool-xor" "" "true false"
      "bool-not" "" "true false"
      "bool-and-n-arity" "" "false"
      "bool-polymorphic" "" "true"

      "cond-if-true" "" "I told you"
      "cond-if-false" "" "Ooops"
      "cond-if-if" "" "222"

      "cond-cond" "" "yes"
      "cond-first-expr" "" "first"
      "cond-default-expr" "" "default"

      "list-list" "" "[]"
      "list-list-args" "" "[1, 2, 3]"
      "list-cons" "" "[1, 2, 3, 100500, 42]"
      "list-get" "" "42"
      "list-set" "" "[1, 54, 3]"
      "list-combined" "" "[1, 2, 44, 4, 5]"
      
      "comp-less" "" "true false"
      "comp-greater" "" "true false"
      "comp-greater-or-equal" "" "true false"
      "comp-less-or-equal" "" "true false"
      "comp-equal" "" "false true true false"
      "comp-nequal" "" "true false true false"
      
      "readln" "cake is a" "cake is a lie"
      "println" ""  "cake"
      "print" "" "cakezzz"
      "println-answer" ""  "the answer"
      "println-sum-float" ""  "3.0"
      "println-sum-int" ""  "3"
      "println-sum-n" ""  "6.0"
      "println-sum-1" ""  "1"
      "println-n-arity" ""  "Cake is a 42.0 lie"
      "print-sum-n" "" "6.0"
      "println-mul-n" "" "36.0"
      "println-sub-n" "" "5"
      "println-div-n" "" "20.0"

      )))
