(ns jacket.test.codegen
  (:use [clojure.test]
        [jacket.codegen]
        [jacket.instructions]))

(deftest get-path-test
  (are [parts path]
       (= path (apply gen-path parts))

       ['java 'lang 'String] "java/lang/String"
       
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

       {:super "Class"} ".super Class"

       ))

(deftest method-text-test
  (are [code text]
       (= text (method-text code))

       {:access :private
        :static true
        :name "main"
        :arguments "Ljava.lang.String;"
        :return "V"
        :instructions [dadd (iinc 1 2) (ret 42)]
        } ".method private static main(Ljava.lang.String;)V\n\tdadd\n\tiinc 1 2\n\tret 42\n.end"

          ))

(deftest methods-text-test
  (are [code text]
       (= text (methods-text code))

       {:methods
        [{:access :public :static true :name "main"
          :arguments "Ljava.lang.String;" :return "V"
          :instructions [dadd (iinc 1 2) (ret 42)]}
         {:access :private :static false :name "bla"
          :arguments "[I[LJava.lang.String;" :return "V"
          :instructions [fadd (astore 10) aconst_null]}]}

       [".method public static main(Ljava.lang.String;)V\n\tdadd\n\tiinc 1 2\n\tret 42\n.end"
        ".method private bla([I[LJava.lang.String;)V\n\tfadd\n\tastore 10\n\taconst_null\n.end"]
       ))

(def program-under-test
         {:access :public :type :class :name "Cake"
        :super "java.lang.Object"
        :methods
        [{:access :public :static true :name "main"
          :arguments ["Ljava.lang.String;"] :return "V"
          :instructions [dadd (iinc 1 2) aconst_null (ret 42)]}
         {:access :private :static false :name "bla"
          :arguments "[I" :return "Ljava.lang.String;"
          :instructions [aconst_null pop1]}]})

(deftest program-text-test
  (are [code text]
       (= text (program-text code))

       program-under-test
       ".class public Cake\n.super java.lang.Object\n.method public static main([\"Ljava.lang.String;\"])V\n\tdadd\n\tiinc 1 2\n\taconst_null\n\tret 42\n.end\n.method private bla([I)Ljava.lang.String;\n\taconst_null\n\tpop\n.end"
       ))
         
        
       
  
