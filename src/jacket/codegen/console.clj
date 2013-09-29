(ns jacket.codegen.console
  (:use [jacket.codegen.classgen]
        [jacket.codegen.instructions]))

                                        ;Codegenerated console interaction library
(def console-library
  {:access :public :type :class :name 'Console
   :super object-path
   :methods [default-init
             {:access :public :static true :name 'readln
              :arguments []
              :return (gen-path 'java 'lang 'String)
              :instructions [(limitstack 5)
                             (jnew (gen-path 'java 'util 'Scanner))
                             dup
                             (getstatic ['java 'lang 'System 'in] ['java 'io 'InputStream])
                             (invokenonvirtual ['java 'util 'Scanner]
                                               '<init>
                                               [(gen-path 'java 'io 'InputStream)]
                                               :void)
                             (invokevirtual ['java 'util 'Scanner]
                                            'nextLine
                                            []
                                            (gen-path 'java 'lang 'String))
                             areturn]
              }
             
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

(defn precompile-libraries [dir]
  (->> console-library program-text (spit (str dir \/ "Console.jasm"))))
