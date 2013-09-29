(ns jacket.codegen.helloworld
  (:use [jacket.codegen.classgen]
        [jacket.codegen.instructions]))

                                        ;HELLO WORLD, for experiments
(def hello-world
  (generate-main-class
   'HelloWorld
   []
   []
   (concat
    [(limitstack 10)
     (ldc "Hey hey!!!")
     (invokestatic ['Console]
                   'println
                   [(gen-path 'java 'lang 'Object)]
                   :void)]
    [return])))

(defn gen-hello-world []
  (->> hello-world program-text (spit "wardrobe/HelloWorld.jasm")))
