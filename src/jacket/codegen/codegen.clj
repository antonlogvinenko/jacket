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

(defn generate-class [name super implements fields methods]
  {:access :public :type :class :name name
   :super super
   :implements implements
   :fields fields
   :methods methods})

(defn generate-closures [closures]
  (for [[name code] closures]
    [name (generate-class name (gen-path 'AClosure) [] []
                          [{:access :public :static false :name "<init>"
                            :arguments [:int]
                            :return :void
                            :instructions [(limitstack 2)
                                           (limitlocals 2)
                                           aload_0
                                           iload_1
                                           (invokenonvirtual ['AClosure] '<init> [:int] :void)
                                           return]}
                           {:access :public :static false :name "invoke"
                            :arguments []
                            :return (gen-path 'java 'lang 'Object)
                            :instructions (into [(limitstack 10)
                                                 (limitlocals 10)]
                                                code)}])]))

(defn generate-main-class [name fields instructions]
  (generate-class name object-path [] fields
                  [default-init
                   {:access :public :static true :name "main"
                    :arguments [[(gen-path 'java 'lang 'String)]]
                    :return :void
                    :instructions (concat instructions [return])}]))

(defn generate-entry-point [instructions]
  (generate-main-class 'WearJacket []  instructions))



                                        ;Codegenerated console interaction library
(def console-library
  {:access :public :type :class :name 'Console ;(gen-path 'jacket 'core 'Console)
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


                                        ;HELLO WORLD, for experiments
(def hello-world
  (generate-main-class
   'HelloWorld
   []
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
  (->> hello-world program-text (spit "wardrobe/HelloWorld.jasm")))

(defn precompile-libraries [dir]
  (->> console-library program-text (spit (str dir \/ "Console.jasm"))))

(defn get-fields-names [ast]
  (->> ast
       (filter #(-> % first (= :define)))
       (map second)))

(defn generate [name ast]
  (let [fields (get-fields-names ast)
        code (map
              (partial generate-ast
                       {:label 0 :class name
                        :closure [0 (str name "-closure-")]
                        :arguments {} :local '()})
              ast)
        ops (map :ops code)
        closures (map :closures code)]
    (conj (->> closures (apply concat) generate-closures)
          [name (->> ops (apply concat) (generate-main-class name fields))])))

(defn compile-jacket [in location name]
  (let [asm (->> in slurp tokenize parse semantics (generate name))]
    (doall
     (for [[name code] asm]
       (->> code
            program-text
            (spit (str location name ".jasm")))))))

