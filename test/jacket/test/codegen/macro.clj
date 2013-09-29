(ns jacket.test.codegen.macro
  (:use [jacket.codegen.codegen]
        [clojure.test]
        [clojure.java.shell]
        [clojure.string :only [split]]))

;(->> "./prepare-wardrobe.sh" sh (with-sh-dir "."))
;(gen-hello-world)
;(with-sh-dir "." (sh "./compile-wardrobe-single.sh" "HelloWorld"))
;(def a (Class/forName "HelloWorld"))


