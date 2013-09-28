(ns jacket.test.codegen.compile
  (:use [clojure.test]
        [jacket.codegen.compile]
        [jacket.lexer.fsm]
        [jacket.codegen.instructions]
        ))

(deftest generate-to-string-conversion-test
  (are [result]
    (= result (:ops (generate-to-string-conversion)))

    [["invokestatic"
      "java/lang/String/valueOf(Ljava/lang/Object;)Ljava/lang/String;"]]

    ))

(deftest boolean?-test
  (is (true? (boolean? true)))
  (is (false? (boolean? 42))))

(deftest generate-test
  (are [args gen-fn result]
    (= result (:ops (apply gen-fn {:closed {} :label 0
                                   :closure (agent 0) :local '([] ())} (to-tokens args))))

    [42] generate-print-single
    [["new" "java/lang/Long"]
     "dup"
     ["ldc_w" 42]
     "i2l"
     ["invokenonvirtual" "java/lang/Long/<init>(J)V"]
     ["invokestatic" "Console/print(Ljava/lang/Object;)V"]
     ]
    
    [[1 1.0 "1.0"]] generate-print
    [[".limit stack" 30]
     ["new" "java/lang/Long"]
     "dup"
     ["ldc_w" 1]
     "i2l"
     ["invokenonvirtual" "java/lang/Long/<init>(J)V"]
     ["invokestatic" "Console/print(Ljava/lang/Object;)V"]
     ["new" "java/lang/Double"]
     "dup"
     ["ldc_w" 1.0]
     "f2d"
     ["invokenonvirtual" "java/lang/Double/<init>(D)V"]
     ["invokestatic" "Console/print(Ljava/lang/Object;)V"]
     ["ldc" "\"1.0\""]
     ["invokestatic" "Console/print(Ljava/lang/Object;)V"]]

    [[1 1.0 "1.0"]] generate-println
    [[".limit stack" 30]
     ["new" "java/lang/Long"]
     "dup"
     ["ldc_w" 1]
     "i2l"
     ["invokenonvirtual" "java/lang/Long/<init>(J)V"]
     ["invokestatic" "Console/print(Ljava/lang/Object;)V"]
     ["new" "java/lang/Double"]
     "dup"
     ["ldc_w" 1.0]
     "f2d"
     ["invokenonvirtual" "java/lang/Double/<init>(D)V"]
     ["invokestatic" "Console/print(Ljava/lang/Object;)V"]
     ["ldc" "\"1.0\""]
     ["invokestatic" "Console/print(Ljava/lang/Object;)V"]
     ["invokestatic" "Console/println()V"]
     "aconst_null"]

    [nil] generate-readln
    [["invokestatic" "Console/readln()Ljava/lang/String;"]]
    
    ["cake"] generate-string-const
    [["ldc" "\"cake\""]]

    [42.0] generate-float-const
    [["new" "java/lang/Double"]
     "dup"
     ["ldc_w" 42.0]
     "f2d"
     ["invokenonvirtual" "java/lang/Double/<init>(D)V"]]

    [42] generate-integer-const
    [["new" "java/lang/Long"]
     "dup"
     ["ldc_w" 42]
     "i2l"
     ["invokenonvirtual" "java/lang/Long/<init>(J)V"]]

    [1] generate-number-const
    [["new" "java/lang/Long"]
     "dup"
     ["ldc_w" 1]
     "i2l"
     ["invokenonvirtual" "java/lang/Long/<init>(J)V"]]

    [1.0] generate-number-const
    [["new" "java/lang/Double"]
     "dup"
     ["ldc_w" 1.0]
     "f2d"
     ["invokenonvirtual" "java/lang/Double/<init>(D)V"]]

    [1.0] (partial generate-single-generic (generate-single-arithmetic 'add) generate-ast)
    [["new" "java/lang/Double"]
     "dup"
     ["ldc_w" 1.0]
     "f2d"
     ["invokenonvirtual" "java/lang/Double/<init>(D)V"]
     ["invokestatic" "Numbers/add(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"]]

    [[1.0 2.0]] (partial generate-several generate-single-arithmetic 'add generate-ast)
    [["new" "java/lang/Double"]
     "dup"
     ["ldc_w" 1.0]
     "f2d"
     ["invokenonvirtual" "java/lang/Double/<init>(D)V"]
     ["new" "java/lang/Double"]
     "dup"
     ["ldc_w" 2.0]
     "f2d"
     ["invokenonvirtual" "java/lang/Double/<init>(D)V"]
     ["invokestatic" "Numbers/add(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"]]

    [[1.0 2]] generate-add
    [["new" "java/lang/Double"]
     "dup"
     ["ldc_w" 1.0]
     "f2d"
     ["invokenonvirtual" "java/lang/Double/<init>(D)V"]
     ["new" "java/lang/Long"]
     "dup"
     ["ldc_w" 2]
     "i2l"
     ["invokenonvirtual" "java/lang/Long/<init>(J)V"]
     ["invokestatic" "Numbers/add(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"]]

    [[1.0 2]] generate-mul
    [["new" "java/lang/Double"]
     "dup"
     ["ldc_w" 1.0]
     "f2d"
     ["invokenonvirtual" "java/lang/Double/<init>(D)V"]
     ["new" "java/lang/Long"]
     "dup"
     ["ldc_w" 2]
     "i2l"
     ["invokenonvirtual" "java/lang/Long/<init>(J)V"]
     ["invokestatic" "Numbers/mul(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"]]

    [[1.0 2]] generate-div
    [["new" "java/lang/Double"]
     "dup"
     ["ldc_w" 1.0]
     "f2d"
     ["invokenonvirtual" "java/lang/Double/<init>(D)V"]
     ["new" "java/lang/Long"]
     "dup"
     ["ldc_w" 2]
     "i2l"
     ["invokenonvirtual" "java/lang/Long/<init>(J)V"]
     ["invokestatic" "Numbers/div(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"]]

    [[1.0 2]] generate-sub
    [["new" "java/lang/Double"]
     "dup"
     ["ldc_w" 1.0]
     "f2d"
     ["invokenonvirtual" "java/lang/Double/<init>(D)V"]
     ["new" "java/lang/Long"]
     "dup"
     ["ldc_w" 2]
     "i2l"
     ["invokenonvirtual" "java/lang/Long/<init>(J)V"]
     ["invokestatic" "Numbers/sub(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"]]

    [[true false]] generate-and
    [["getstatic" "java/lang/Boolean/TRUE Ljava/lang/Boolean;"]
     ["invokestatic" "Logic/toBoolean(Ljava/lang/Object;)Ljava/lang/Boolean;"]
     ["getstatic" "java/lang/Boolean/FALSE Ljava/lang/Boolean;"]
     ["invokestatic" "Logic/toBoolean(Ljava/lang/Object;)Ljava/lang/Boolean;"]
     ["invokestatic" "Logic/and(Ljava/lang/Boolean;Ljava/lang/Boolean;)Ljava/lang/Boolean;"]]

    [[false false]] generate-or
    [["getstatic" "java/lang/Boolean/FALSE Ljava/lang/Boolean;"]
     ["invokestatic" "Logic/toBoolean(Ljava/lang/Object;)Ljava/lang/Boolean;"]
     ["getstatic" "java/lang/Boolean/FALSE Ljava/lang/Boolean;"]
     ["invokestatic" "Logic/toBoolean(Ljava/lang/Object;)Ljava/lang/Boolean;"]
     ["invokestatic" "Logic/or(Ljava/lang/Boolean;Ljava/lang/Boolean;)Ljava/lang/Boolean;"]]

    [[true false]] generate-xor
    [["getstatic" "java/lang/Boolean/TRUE Ljava/lang/Boolean;"]
     ["invokestatic" "Logic/toBoolean(Ljava/lang/Object;)Ljava/lang/Boolean;"]
     ["getstatic" "java/lang/Boolean/FALSE Ljava/lang/Boolean;"]
     ["invokestatic" "Logic/toBoolean(Ljava/lang/Object;)Ljava/lang/Boolean;"]
     ["invokestatic" "Logic/xor(Ljava/lang/Boolean;Ljava/lang/Boolean;)Ljava/lang/Boolean;"]]

    [[true]] generate-not
    [["getstatic" "java/lang/Boolean/TRUE Ljava/lang/Boolean;"]
     ["invokestatic" "Logic/toBoolean(Ljava/lang/Object;)Ljava/lang/Boolean;"]
     ["invokestatic" "Logic/not(Ljava/lang/Boolean;)Ljava/lang/Boolean;"]]

    [[2 3]] generate-eq
    [["new" "java/lang/Long"]
     "dup"
     ["ldc_w" 2]
     "i2l"
     ["invokenonvirtual" "java/lang/Long/<init>(J)V"]
     ["new" "java/lang/Long"]
     "dup"
     ["ldc_w" 3]
     "i2l"
     ["invokenonvirtual" "java/lang/Long/<init>(J)V"]
     ["invokevirtual" "java/lang/Object/equals(Ljava/lang/Object;)Z"]
     ["invokestatic" "java/lang/Boolean/valueOf(Z)Ljava/lang/Boolean;"]]

    [[2 3]] generate-neq
    [["new" "java/lang/Long"]
     "dup"
     ["ldc_w" 2]
     "i2l"
     ["invokenonvirtual" "java/lang/Long/<init>(J)V"]
     ["new" "java/lang/Long"]
     "dup"
     ["ldc_w" 3]
     "i2l"
     ["invokenonvirtual" "java/lang/Long/<init>(J)V"]
     ["invokevirtual" "java/lang/Object/equals(Ljava/lang/Object;)Z"]
     ["invokestatic" "java/lang/Boolean/valueOf(Z)Ljava/lang/Boolean;"]
     ["invokestatic" "Logic/not(Ljava/lang/Boolean;)Ljava/lang/Boolean;"]]

    [[42 42.0 45.0]] generate-le
    [["new" "java/lang/Long"]
     "dup"
     ["ldc_w" 42]
     "i2l"
     ["invokenonvirtual" "java/lang/Long/<init>(J)V"]
     ["new" "java/lang/Double"]
     "dup"
     ["ldc_w" 42.0]
     "f2d"
     ["invokenonvirtual" "java/lang/Double/<init>(D)V"]
     ["invokestatic" "Comparison/lessOrEqual(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Boolean;"] ["new" "java/lang/Double"]
     "dup"
     ["ldc_w" 42.0]
     "f2d"
     ["invokenonvirtual" "java/lang/Double/<init>(D)V"]
     ["new" "java/lang/Double"]
     "dup"
     ["ldc_w" 45.0]
     "f2d"
     ["invokenonvirtual" "java/lang/Double/<init>(D)V"]
     ["invokestatic" "Comparison/lessOrEqual(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Boolean;"]
     ["invokestatic" "Logic/and(Ljava/lang/Boolean;Ljava/lang/Boolean;)Ljava/lang/Boolean;"]]

    [42] generate-atom
    [["new" "java/lang/Long"]
     "dup"
     ["ldc_w" 42]
     "i2l"
     ["invokenonvirtual" "java/lang/Long/<init>(J)V"]]

    ["cake"] generate-atom
    [["ldc" "\"cake\""]]

    [[:println 42]] generate-sexpr
    [[".limit stack" 30]
     ["new" "java/lang/Long"]
     "dup"
     ["ldc_w" 42]
     "i2l"
     ["invokenonvirtual" "java/lang/Long/<init>(J)V"]
     ["invokestatic" "Console/print(Ljava/lang/Object;)V"]
     ["invokestatic" "Console/println()V"]
     "aconst_null"]


    [21] generate-ast
     [["new" "java/lang/Long"]
      "dup"
      ["ldc_w" 21]
      "i2l"
      ["invokenonvirtual" "java/lang/Long/<init>(J)V"]]

     [[:if true "a" "b"]] generate-ast
     ["\t;>>> if statement Label-1 / Label-2"
      ["getstatic" "java/lang/Boolean/TRUE Ljava/lang/Boolean;"]
      ["invokestatic" "Logic/toBoolean(Ljava/lang/Object;)Ljava/lang/Boolean;"]
      ["invokevirtual" "java/lang/Boolean/booleanValue()Z"]
      ["ifeq" "Label-1"]
      ["ldc" "\"a\""]
      ["goto" "Label-2"]
      "Label-1:"
      ["ldc" "\"b\""]
      "Label-2:"
      "\t;<<< if statement Label-1 / Label-2"]

     [[ :cond true [:println "a"] [:println "b"]]] generate-ast
     ["\t;>>> cond statement Label-1"
      "\t;>>> cond branch Label-2"
      ["getstatic" "java/lang/Boolean/TRUE Ljava/lang/Boolean;"]
      ["invokestatic" "Logic/toBoolean(Ljava/lang/Object;)Ljava/lang/Boolean;"]
      ["invokevirtual" "java/lang/Boolean/booleanValue()Z"]
      ["ifeq" "Label-2"]
      [".limit stack" 30]
      ["ldc" "\"a\""]
      ["invokestatic" "Console/print(Ljava/lang/Object;)V"]
      ["invokestatic" "Console/println()V"]
      "aconst_null"
      ["goto" "Label-1"]
      "Label-2:" "\t;<<< cond branch Label-2"
      "\t;>>> default branch cond Label-1"
      [".limit stack" 30]
      ["ldc" "\"b\""]
      ["invokestatic" "Console/print(Ljava/lang/Object;)V"]
      ["invokestatic" "Console/println()V"]
      "aconst_null"
      "\t;<<< default branch condLabel-1"
      "Label-1:" "\t;<<< cond statement Label-1"]


     [[:list]] generate-ast
     [["new" "java/util/ArrayList"]
      "dup"
      ["invokenonvirtual" "java/util/ArrayList/<init>()V"]]

     [[:list 1]] generate-ast
     [["new" "java/util/ArrayList"]
      "dup"
      ["invokenonvirtual" "java/util/ArrayList/<init>()V"]
      "dup"
      ["new" "java/lang/Long"]
      "dup"
      ["ldc_w" 1]
      "i2l"
      ["invokenonvirtual" "java/lang/Long/<init>(J)V"]
      ["invokevirtual" "java/util/ArrayList/add(Ljava/lang/Object;)Z"]
      "pop"]

     [[:cons [:list 2] 42]] generate-ast
     [["new" "java/util/ArrayList"]
      "dup"
      ["invokenonvirtual" "java/util/ArrayList/<init>()V"]
      "dup"
      ["new" "java/lang/Long"]
      "dup"
      ["ldc_w" 2]
      "i2l"
      ["invokenonvirtual" "java/lang/Long/<init>(J)V"]
      ["invokevirtual" "java/util/ArrayList/add(Ljava/lang/Object;)Z"]
      "pop"
      "dup"
      ["new" "java/lang/Long"]
      "dup"
      ["ldc_w" 42]
      "i2l"
      ["invokenonvirtual" "java/lang/Long/<init>(J)V"]
      ["invokevirtual" "java/util/ArrayList/add(Ljava/lang/Object;)Z"]
      "pop"]

     [[:get [:list 1] 0]] generate-ast
     [["new" "java/util/ArrayList"]
      "dup"
      ["invokenonvirtual" "java/util/ArrayList/<init>()V"]
      "dup"
      ["new" "java/lang/Long"]
      "dup"
      ["ldc_w" 1]
      "i2l"
      ["invokenonvirtual" "java/lang/Long/<init>(J)V"]
      ["invokevirtual" "java/util/ArrayList/add(Ljava/lang/Object;)Z"]
      "pop"
      ["new" "java/lang/Long"]
      "dup"
      ["ldc_w" 0]
      "i2l"
      ["invokenonvirtual" "java/lang/Long/<init>(J)V"]
      ["invokevirtual" "java/lang/Number/intValue()I"]
      ["invokevirtual" "java/util/ArrayList/get(I)Ljava/lang/Object;"]]

     [[:set [:list 42] 0 1]] generate-ast
     [["new" "java/util/ArrayList"]
      "dup"
      ["invokenonvirtual" "java/util/ArrayList/<init>()V"]
      "dup"
      ["new" "java/lang/Long"]
      "dup"
      ["ldc_w" 42]
      "i2l"
      ["invokenonvirtual" "java/lang/Long/<init>(J)V"]
      ["invokevirtual" "java/util/ArrayList/add(Ljava/lang/Object;)Z"]
      "pop"
      "dup"
      ["new" "java/lang/Long"]
      "dup"
      ["ldc_w" 0]
      "i2l"
      ["invokenonvirtual" "java/lang/Long/<init>(J)V"]
      ["invokevirtual" "java/lang/Number/intValue()I"]
      ["new" "java/lang/Long"]
      "dup"
      ["ldc_w" 1]
      "i2l"
      ["invokenonvirtual" "java/lang/Long/<init>(J)V"]
      ["invokevirtual" "java/util/ArrayList/set(ILjava/lang/Object;)Ljava/lang/Object;"]
      "pop"]
     
     [[:+ 1 2]] generate-ast
     [["new" "java/lang/Long"]
      "dup"
      ["ldc_w" 1]
      "i2l"
      ["invokenonvirtual" "java/lang/Long/<init>(J)V"]
      ["new" "java/lang/Long"]
      "dup"
      ["ldc_w" 2]
      "i2l"
      ["invokenonvirtual" "java/lang/Long/<init>(J)V"]
      ["invokestatic" "Numbers/add(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"]]


     [[:let [['a 1]] [:println 'a]]] generate-ast
     [[".limit locals" 10]
      ["new" "java/lang/Long"]
      "dup"
      ["ldc_w" 1]
      "i2l"
      ["invokenonvirtual" "java/lang/Long/<init>(J)V"]
      ["astore" 0]
      [".limit stack" 30]
      ["aload" 0]
      ["invokestatic" "Console/print(Ljava/lang/Object;)V"]
      ["invokestatic" "Console/println()V"]
      "aconst_null"]

     [[:define 'a 42]] generate-ast
     [["new" "java/lang/Long"]
      "dup"
      ["ldc_w" 42]
      "i2l"
      ["invokenonvirtual" "java/lang/Long/<init>(J)V"]
      ["putstatic" "/a Ljava/lang/Object;"]]


     [[:java.lang.String. "cake"]] generate-ast
     [["ldc_w" "\":java.lang.String\""]
      ["ldc_w" 1]
      ["anewarray" "java/lang/Object"]
      "dup"
      ["ldc_w" 0]
      ["ldc" "\"cake\""]
      "aastore"
      ["invokestatic" "Interop/instantiate(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;"]]
  
     [java.lang.Boolean/TRUE] generate-ast
     [["getstatic" "java/lang/Boolean/TRUE Ljava/lang/Boolean;"]]

     [['. 'java.lang.Boolean 'TRUE]] generate-ast
     [["ldc_w" "\"java.lang.Boolean\""]
      ["ldc_w" "\"TRUE\""]
      ["ldc_w" 0]
      ["anewarray" "java/lang/Object"]
      ["invokestatic" "Interop/accessStatic(Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;"]]

     [['java.lang.Double/valueOf "42E1"]] generate-ast
     [["ldc_w" "\"java.lang.Double\""]
      ["ldc_w" "\"valueOf\""]
      ["ldc_w" 1]
      ["anewarray" "java/lang/Object"]
      "dup"
      ["ldc_w" 0]
      ["ldc" "\"42E1\""]
      "aastore"
      ["invokestatic" "Interop/accessStatic(Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;"]]

     [['. 'java.lang.Double 'valueOf "42E1"]] generate-ast
     [["ldc_w" "\"java.lang.Double\""]
      ["ldc_w" "\"valueOf\""]
      ["ldc_w" 1]
      ["anewarray" "java/lang/Object"]
      "dup"
      ["ldc_w" 0]
      ["ldc" "\"42E1\""]
      "aastore"
      ["invokestatic" "Interop/accessStatic(Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;"]]

     [['.TRUE true]] generate-ast
     [["getstatic" "java/lang/Boolean/TRUE Ljava/lang/Boolean;"]
      ["ldc_w" "\"TRUE\""]
      ["ldc_w" 0]
      ["anewarray" "java/lang/Object"]
      ["invokestatic" "Interop/accessInstance(Ljava/lang/Object;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;"]]

     [['. true 'TRUE]] generate-ast
     [["getstatic" "java/lang/Boolean/TRUE Ljava/lang/Boolean;"]
      ["ldc_w" "\"TRUE\""]
      ["ldc_w" 0]
      ["anewarray" "java/lang/Object"]
      ["invokestatic" "Interop/accessInstance(Ljava/lang/Object;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;"]]

     [['.intValue 42.0]] generate-ast
     [["new" "java/lang/Double"]
      "dup"
      ["ldc_w" 42.0]
      "f2d"
      ["invokenonvirtual" "java/lang/Double/<init>(D)V"]
      ["ldc_w" "\"intValue\""]
      ["ldc_w" 0]
      ["anewarray" "java/lang/Object"]
      ["invokestatic" "Interop/accessInstance(Ljava/lang/Object;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;"]]

     [['. 42.0 'intValue]] generate-ast
     [["new" "java/lang/Double"]
      "dup"
      ["ldc_w" 42.0]
      "f2d"
      ["invokenonvirtual" "java/lang/Double/<init>(D)V"]
      ["ldc_w" "\"intValue\""]
      ["ldc_w" 0]
      ["anewarray" "java/lang/Object"]
      ["invokestatic" "Interop/accessInstance(Ljava/lang/Object;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;"]]
     

    ))

(deftest generate-closures-test
  (are [args result]
    (= result (generate-ast {:label 0 :closed {}
                             :closure (agent 0) :local '({})} (to-tokens args)))

    [[:lambda [] [:println 42]]]


    {:closures [["-closure-0" [[".limit stack" 30]
                               ["new" "java/lang/Long"]
                               "dup"
                               ["ldc_w" 42]
                               "i2l"
                               ["invokenonvirtual" "java/lang/Long/<init>(J)V"]
                               ["invokestatic" "Console/print(Ljava/lang/Object;)V"]
                               ["invokestatic" "Console/println()V"] "aconst_null" "areturn"]]],
     :globals [],
     :macro false,
     :ops [[".limit stack" 30]
           ["new" "-closure-0"]
           "dup"
           ["ldc_w" 0]
           ["anewarray" "java/lang/Object"]
           ["ldc_w" 0]
           ["invokenonvirtual" "-closure-0/<init>([Ljava/lang/Object;I)V"]
           ["checkcast" "AClosure"]
           ["ldc_w" 0]
           ["anewarray" "java/lang/Object"]
           ["invokevirtual" "AClosure/_invoke([Ljava/lang/Object;)Ljava/lang/Object;"]]}))

