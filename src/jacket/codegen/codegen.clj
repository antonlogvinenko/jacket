(ns jacket.codegen.codegen
  (:use [jacket.codegen.program]
        [jacket.codegen.instructions]
        ))


                                        ;Main class, entry point
(def default-init
  {:access :public :name "<init>"
   :arguments [] :return :void
   :instructions [aload_0
                  (invokenonvirtual ['java 'lang 'Object] '<init> [] :void)
                  return]})

(defn generate-main-class [name instructions]
  {:access :public :type :class :name name
   :super object-path
   :methods
   [default-init
    {:access :public :static true :name "main"
     :arguments [[(gen-path 'java 'lang 'String)]]
     :return :void
     :instructions instructions}]})

(defn generate-entry-point [instructions]
  (generate-main-class 'WearJacket instructions))



                                        ;Codegenerated console interaction library
(def console-library
  {:access :public :type :class :name 'Console ;(gen-path 'jacket 'core 'Console)
   :super object-path
   :methods [default-init
             {:access :public :static true :name 'println
              :arguments [(gen-path 'java 'lang 'String)]
              :return :void
              :instructions [(limitstack 2)
                             (limitlocals 1)
                             (getstatic ['java 'lang 'System 'out] ['java 'io 'PrintStream])
                             aload_0
                             (invokevirtual ['java 'io 'PrintStream]
                                            'println
                                            [(gen-path 'java 'lang 'String)] :void)
                             return
                             ]}]})


                                        ;HELLO WORLD, for experiments
(def hello-world
  (generate-main-class
   'HelloWorld
   [(limitstack 2)
    (ldc "Hey hey!!!")
    (invokestatic ['Console]
                  'println
                  [(gen-path 'java 'lang 'String)]
                  :void)
    return
    ]))

(defn spit-class [f content]
  (spit (str "wardrobe" \/ f) content))

(defn gen-hello-world []
  (->> hello-world program-text (spit-class "HelloWorld.jt"))
  (->> console-library program-text (spit-class "Console.jt")))
