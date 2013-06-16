(ns jacket.test.codegen.compile
  (:use [clojure.test]
        [jacket.codegen.compile]
        [jacket.lexer.fsm]
        [jacket.codegen.instructions]
        ))

(deftest with-testing
  (are [ops arg1 args result]
    (= result (apply with (into (conj [] ops arg1) args)))

    [1 2 3] [4] []
    [1 2 3 4]

    [nop] nop []
    ["nop" "nop"]

    [nop] iinc [1 2]
    ["nop" ["iinc" 1 2]]

    ))

(deftest generate-to-string-conversion-test
  (are [result]
    (= result (generate-to-string-conversion))

    [["invokestatic"
      "java/lang/String/valueOf(Ljava/lang/Object;)Ljava/lang/String;"]]

    ))

(deftest boolean?-test
  (is (true? (boolean? true)))
  (is (false? (boolean? 42))))

(deftest generate-test
  (are [args gen-fn result]
    (= result (apply gen-fn (to-tokens args)))

    [42] generate-print-single
    [["new" "java/lang/Long"]
     "dup"
     ["ldc_w" 42]
     "i2l"
     ["invokenonvirtual" "java/lang/Long/<init>(J)V"]
     ["invokestatic" "Console/print(Ljava/lang/Object;)V"]
     ]
    
    [[1 1.0 "1.0"]] generate-print
    [[".limit stack" 10]
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
    [[".limit stack" 10]
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
     ["invokestatic" "Console/println()V"]]

    [] generate-readln
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
     ["invokestatic" "Numbers/add(Ljava/lang/Number;Ljava/lang/Number;)Ljava/lang/Number;"]]

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
     ["invokestatic" "Numbers/add(Ljava/lang/Number;Ljava/lang/Number;)Ljava/lang/Number;"]]

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
     ["invokestatic" "Numbers/add(Ljava/lang/Number;Ljava/lang/Number;)Ljava/lang/Number;"]]

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
     ["invokestatic" "Numbers/mul(Ljava/lang/Number;Ljava/lang/Number;)Ljava/lang/Number;"]]

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
     ["invokestatic" "Numbers/div(Ljava/lang/Number;Ljava/lang/Number;)Ljava/lang/Number;"]]

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
     ["invokestatic" "Numbers/sub(Ljava/lang/Number;Ljava/lang/Number;)Ljava/lang/Number;"]]

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
     ["invokestatic" "Comparison/lessOrEqual(Ljava/lang/Number;Ljava/lang/Number;)Ljava/lang/Boolean;"] ["new" "java/lang/Double"]
     "dup"
     ["ldc_w" 42.0]
     "f2d"
     ["invokenonvirtual" "java/lang/Double/<init>(D)V"]
     ["new" "java/lang/Double"]
     "dup"
     ["ldc_w" 45.0]
     "f2d"
     ["invokenonvirtual" "java/lang/Double/<init>(D)V"]
     ["invokestatic" "Comparison/lessOrEqual(Ljava/lang/Number;Ljava/lang/Number;)Ljava/lang/Boolean;"]
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
    [[".limit stack" 10]
     ["new" "java/lang/Long"]
     "dup"
     ["ldc_w" 42]
     "i2l"
     ["invokenonvirtual" "java/lang/Long/<init>(J)V"]
     ["invokestatic" "Console/print(Ljava/lang/Object;)V"] ["invokestatic" "Console/println()V"]]


    [21] generate-ast
     [["new" "java/lang/Long"]
      "dup"
      ["ldc_w" 21]
      "i2l"
      ["invokenonvirtual" "java/lang/Long/<init>(J)V"]]

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
      ["invokestatic" "Numbers/add(Ljava/lang/Number;Ljava/lang/Number;)Ljava/lang/Number;"]]
     
    ))
