(ns jacket.codegen.compile
  (:use [jacket.codegen.classgen]
        [jacket.codegen.instructions]
        [jacket.lexer.lexer]
        [jacket.lexer.fsm]
        [jacket.parser]
        [jacket.semantics])
  (:import [jacket.lexer.fsm Token]))

                                        ;Program file to jasmin file
(defn debug [x] (println x) x)

(defn codegen-error []
  (throw (RuntimeException. "Who is Mr. Putin?")))

(def ops [])

(defn with [ops arg1 & args]
  (into ops (if (vector? arg1)
              arg1
              (if (empty? args)
                [arg1] 
                [(apply arg1 args)]))))


(defn generate-sexpr [])
(defn generate-ast [])

(defn generate-to-string-conversion []
  (with ops
        invokestatic ['java 'lang 'String]
        'valueOf
        [(gen-path 'java 'lang 'Object)]
        (gen-path 'java 'lang 'String)))

(defn generate-print-single [arg]
  (-> ops
      (with (generate-ast arg))
      (with invokestatic ['Console] 'print
            [(gen-path 'java 'lang 'Object)]
            :void)))

(defn generate-print [args]
  (-> ops
      (with limitstack 10)
      (with (->> args
                 (map generate-print-single)
                 (reduce into [])))))

(defn generate-println [args]
  (-> ops
      (with (generate-print args))
      (with invokestatic ['Console] 'println [] :void)))

(defn generate-readln []
  (-> ops
      (with invokestatic ['Console] 'readln [] (gen-path 'java 'lang 'String))))

(defn generate-read []
  (-> ops
      (with invokestatic ['Console] 'read [] (gen-path 'java 'lang 'String))))

(defn generate-string-const [ast]
  (with ops
        ldc (.value ast)))

(defn generate-float-const [ast]
  (-> ops
      (with jnew (gen-path 'java 'lang 'Double))
      (with dup)
      (with ldc_w (.value ast))
      (with f2d)
      (with invokenonvirtual ['java 'lang 'Double] '<init> [:double] :void)))

(defn generate-integer-const [ast]
  (-> ops
      (with jnew (gen-path 'java 'lang 'Long))
      (with dup)
      (with ldc_w (.value ast))
      (with i2l)
      (with invokenonvirtual ['java 'lang 'Long] '<init> [:long] :void)))

(defn generate-number-const [ast]
  (cond
   (is? ast float?) (generate-float-const ast)
   (is? ast integer?) (generate-integer-const ast)
   :else (codegen-error)))

(defn generate-boolean-const [ast]
  (let [value (if (.value ast) 'TRUE 'FALSE)]
    (-> ops
        (with getstatic
              ['java 'lang 'Boolean value]
              ['java 'lang 'Boolean]))))

(defn generate-single-generic [instructions generate-arg arg]
  (-> ops
      (with (generate-arg arg))
      (with instructions)))

(defn generate-several [generate-single instruction generate-arg args]
  (-> ops
      (with (generate-arg (first args)))
      (with (->> args
                 rest
                 (map (partial generate-single-generic
                               (generate-single instruction)
                               generate-arg))
                 (reduce into [])))))

                                        ;Arithmetic operations
(defn generate-single-arithmetic [instruction]
  [(invokestatic ['Numbers]
                 instruction
                 [(gen-path 'java 'lang 'Number) (gen-path 'java 'lang 'Number)]
                 (gen-path 'java 'lang 'Number))])

(defn generate-arithmetic [instruction args]
  (generate-several generate-single-arithmetic instruction generate-ast args))

(defn generate-add [args]
  (generate-arithmetic 'add args))
(defn generate-mul [args]
  (generate-arithmetic 'mul args))
(defn generate-div [args]
  (generate-arithmetic 'div args))
(defn generate-sub [args]
  (generate-arithmetic 'sub args))


                                        ;Logic operations
(defn generate-ast-to-boolean [arg]
  (-> ops
      (with (generate-ast arg))
      (with invokestatic ['Logic]
            'toBoolean
            [(gen-path 'java 'lang 'Object)]
            (gen-path 'java 'lang 'Boolean))))

(defn generate-single-logic [instruction]
  (let [signature (if (= 'not instruction) nil
                      (gen-path 'java 'lang 'Boolean))]
    [(invokestatic ['Logic]
                   instruction
                   (conj [(gen-path 'java 'lang 'Boolean)] signature)
                   (gen-path 'java 'lang 'Boolean))]))

(defn generate-logic [instruction args]
  (generate-several generate-single-logic instruction generate-ast-to-boolean args))

(defn generate-and [args]
  (generate-logic 'and args))
(defn generate-or [args]
  (generate-logic 'or args))
(defn generate-xor [args]
  (generate-logic 'xor args))
(defn generate-not [args]
  (-> ops
      (with (generate-ast-to-boolean (first args)))
      (with (generate-single-logic 'not))))

(defn boolean? [atom]
  (-> atom type (= java.lang.Boolean)))

(defn generate-atom [atom]
  (cond
   (is? atom string?) (generate-string-const atom)
   (is? atom number?) (generate-number-const atom)
   (is? atom boolean?) (generate-boolean-const atom)
   :else (codegen-error)))

(defn generate-sexpr [sexpr]
  (let [type (first sexpr)
        args (rest sexpr)]
    (cond
     (= type :println) (generate-println args)
     (= type :print) (generate-print args)
     (= type :readln) (generate-readln)
     (= type :+) (generate-add args)
     (= type :*) (generate-mul args)
     (= type :-) (generate-sub args)
     (= type (keyword "/")) (generate-div args)
     (= type :and) (generate-and args)
     (= type :or) (generate-or args)
     (= type :not) (generate-not args)
     (= type :xor) (generate-xor args)
     :else (codegen-error))))

(defn generate-ast [ast]
  (if (vector? ast)
    (generate-sexpr ast)
    (generate-atom ast)))
