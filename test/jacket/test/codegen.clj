
(ns jacket.test.codegen
  (:use [clojure.test]
        [jacket.codegen]
        [jacket.instructions]))

(deftest gen-path-test
  (are [parts path]
       (= path (apply gen-path parts))

       ['java 'lang 'String] "java/lang/String"
       
       ))

(deftest gen-type-test
  (are [definition type]
       (= type (gen-type definition))

       :void "V"
       :long "L"
       [:long] "[L"
       [(gen-path 'java 'lang 'String)] "[Ljava/lang/String;"
       
       ))

(deftest gen-arguments-test
  (are [arguments text]
    (= (gen-arguments arguments) text)
    
    [:int [:int] [(gen-path 'java 'io 'File)]]
    "I[I[Ljava/io/File;"
    
    ))

(deftest format-constant-test
  (are [constant formatted]
    (= formatted (format-constant constant))

    "Cake or no cake" "\"Cake or no cake\""
    42 42
    0.42 0.42
    true true
    ))

(deftest gen-method-header-test
  (are [header-spec header]
    (= header (apply gen-method-header header-spec))

    ['println [(gen-path 'java 'lang 'String)] :void]
    "println(Ljava/lang/String;)V"

    ['println [(gen-path 'java 'lang 'String)] (gen-path 'java 'lang 'Integer)]
    "println(Ljava/lang/String;)Ljava/lang/Integer;"))

(deftest invoke-some-method-test
  (are [class-path method arguments return invocation-string]
    (= invocation-string (invoke-some-method class-path method arguments return))

    ['java 'io 'PrintStream]
    'println
    [(gen-path 'java 'lang 'String)]
    :void
    "java/io/PrintStream/println(Ljava/lang/String;)V"

    ))

(deftest get-some-field-test
  (are [field-spec descriptor get-instruction]
    (= get-instruction (get-some-field field-spec descriptor))

    ['java 'lang 'System 'out]
    ['java 'io 'PrintStream]
    "java/lang/System/out Ljava/io/PrintStream;"

    ))

(deftest instruction-text-test
  (are [instruction text]
    (= text (instruction-text instruction))
    
    (iinc 1 2) "iinc 1 2"
    dadd "dadd"
    ))

(deftest instructions-text-test
  (are [instructions text]
    (= text (instructions-text instructions))

    [dadd (iinc 1 2)] "\tdadd\n\tiinc 1 2"
    ))

(deftest file-type-text-test
  (are [code text]
       (= text (file-type-text code))

       {:type :class :access :private :name "Cake"} ".class private Cake"

       {:type :interface :access :private :name "Book"} ".interface private Book"
       ))

(deftest super-text-test
  (are [code text]
       (= text (super-text code))

       {:super (gen-path 'java 'lang 'String)}
       ".super java/lang/String"
       
       {}
       ".super java/lang/Object"
       ))

(deftest implements-text-test
  (are [code text]
       (= text (implements-text code))

       {:implements [(gen-path 'java 'io 'Serializable)
                     (gen-path 'java 'lang 'Cloneable)]}
       ".implements java/io/Serializable\n.implements java/lang/Cloneable"
       
       ))
  

(deftest super-text-test
  (are [code text]
       (= text (super-text code))

       {:super "Class"} ".super Class"

       ))

(deftest method-text-test
  (are [code text]
       (= text (method-text code))

       {:access :private
        :static true
        :name "main"
        :arguments [(gen-path 'java 'lang 'String)]
        :return :void
        :instructions [dadd (iinc 1 2) (ret 42)]
        } ".method private static main(Ljava/lang/String;)V\n\tdadd\n\tiinc 1 2\n\tret 42\n.end method"

          ))

(deftest methods-text-test
  (are [code text]
       (= text (methods-text code))

       {:methods
        [{:access :public :static true :name "main"
          :arguments [[(gen-path 'java 'lang 'String)]] :return :void
          :instructions [dadd (iinc 1 2) (ret 42)]}
         {:access :private :static false :name "bla"
          :arguments [[:int] [(gen-path 'java 'lang 'String)]] :return :void
          :instructions [fadd (astore 10) aconst_null]}]}

       [".method public static main([Ljava/lang/String;)V\n\tdadd\n\tiinc 1 2\n\tret 42\n.end method"
        ".method private bla([I[Ljava/lang/String;)V\n\tfadd\n\tastore 10\n\taconst_null\n.end method"]
       ))

(def program-under-test
         {:access :public :type :class :name "Cake"
          :super (gen-path 'java 'lang 'Object)
          :implements [(gen-path 'java 'io 'Serializable)
                       (gen-path 'java 'lang 'Cloneable)]
          :methods
          [{:access :public :static true :name "main"
            :arguments [[(gen-path 'java 'lang 'String)]] :return :void
            :instructions [dadd (iinc 1 2) aconst_null (ret 42)]}
           {:access :private :static false :name "bla"
            :arguments [[:int]] :return (gen-path 'java 'lang 'String)
            :instructions [aconst_null pop1]}]})

(deftest program-text-test
  (are [code text]
       (= text (program-text code))

       program-under-test
       ".class public Cake\n.super java/lang/Object\n.implements java/io/Serializable\n.implements java/lang/Cloneable\n.method public static main([Ljava/lang/String;)V\n\tdadd\n\tiinc 1 2\n\taconst_null\n\tret 42\n.end method\n.method private bla([I)Ljava/lang/String;\n\taconst_null\n\tpop\n.end method"
       ))
         
        
       
  
