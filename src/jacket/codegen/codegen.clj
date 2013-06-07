(ns jacket.codegen.codegen
  (:use [jacket.codegen.classgen]
        [jacket.codegen.instructions]
        [jacket.codegen.compile]
        [jacket.lexer.lexer]
        [jacket.parser]
        [jacket.semantics])
  (:import [jacket.lexer.fsm Token]))

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
     :instructions (concat instructions [return])}]})

(defn generate-entry-point [instructions]
  (generate-main-class 'WearJacket instructions))



                                        ;Codegenerated console interaction library
(def console-library
  {:access :public :type :class :name 'Console ;(gen-path 'jacket 'core 'Console)
   :super object-path
   :methods [default-init
             {:access :public :static true :name 'println
              :arguments [(gen-path 'java 'lang 'Object)]
              :return :void
              :instructions [(limitstack 2)
                             (limitlocals 1)
                             (getstatic ['java 'lang 'System 'out] ['java 'io 'PrintStream])
                             aload_0
                             (invokevirtual ['java 'io 'PrintStream]
                                            'println
                                            [(gen-path 'java 'lang 'Object)] :void)
                             return]}
             {:access :public :static true :name 'print
              :arguments [(gen-path 'java 'lang 'Object)]
              :return :void
              :instructions [(limitstack 2)
                             (limitlocals 1)
                             (getstatic ['java 'lang 'System 'out] ['java 'io 'PrintStream])
                             aload_0
                             (invokevirtual ['java 'io 'PrintStream]
                                            'print
                                            [(gen-path 'java 'lang 'Object)] :void)
                             return]}
             {:access :public :static true :name 'println
              :arguments []
              :return :void
              :instructions [(limitstack 2)
                             (limitlocals 1)
                             (getstatic ['java 'lang 'System 'out] ['java 'io 'PrintStream])
                             (invokevirtual ['java 'io 'PrintStream]
                                            'println
                                            [] :void)
                             return]}
             ]})


                                        ;HELLO WORLD, for experiments
(def hello-world
  (generate-main-class
   'HelloWorld
   (concat
    [(limitstack 10)
     (ldc "Hey hey!!!")
     (invokestatic ['Console]
                   'println
                   [(gen-path 'java 'lang 'Object)]
                   :void)]
    [return])))

(defn precompile-libraries [])

(defn gen-hello-world []
  (precompile-libraries "wardrobe")
  (->> hello-world program-text (spit "wardrobe/HelloWorld.jasm")))

(defn precompile-libraries [dir]
  (->> console-library program-text (spit (str dir \/ "Console.jasm"))))

(defn generate [name ast]
  (generate-main-class name
                       (apply concat (map generate-ast ast))))

(defn compile-jacket [in location name]
  (precompile-libraries location)
  (->> in slurp
       tokenize parse semantics
       (generate name) program-text
       (spit (str location name ".jasm"))))

