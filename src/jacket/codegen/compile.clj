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

(def ^:dynamic last-label 0)

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
(defn generate-ast-with-context [])

(defn generate-to-string-conversion []
  (with ops
        invokestatic ['java 'lang 'String]
        'valueOf
        [(gen-path 'java 'lang 'Object)]
        (gen-path 'java 'lang 'String)))

(defn generate-print-single [arg]
  (-> ops
      (with (generate-ast-with-context arg))
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
  (generate-several generate-single-arithmetic instruction generate-ast-with-context args))

(defn generate-add [args]
  (generate-arithmetic 'add args))
(defn generate-mul [args]
  (generate-arithmetic 'mul args))
(defn generate-div [args]
  (generate-arithmetic 'div args))
(defn generate-sub [args]
  (generate-arithmetic 'sub args))


                                        ;Logic operations
(defn generate-ast-with-context-to-boolean [arg]
  (-> ops
      (with (generate-ast-with-context arg))
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
  (generate-several generate-single-logic instruction generate-ast-with-context-to-boolean args))

(defn generate-and [args]
  (generate-logic 'and args))
(defn generate-or [args]
  (generate-logic 'or args))
(defn generate-xor [args]
  (generate-logic 'xor args))
(defn generate-not [args]
  (-> ops
      (with (generate-ast-with-context-to-boolean (first args)))
      (with (generate-single-logic 'not))))

(defn boolean? [atom]
  (-> atom type (= java.lang.Boolean)))



                                        ;Comparison
(defn generate-single-comparison [instruction]
  [(invokestatic ['Comparison]
                 instruction
                 [(gen-path 'java 'lang 'Number) (gen-path 'java 'lang 'Number)]
                 (gen-path 'java 'lang 'Boolean))])

(defn generate-comparison [instruction args]
  (generate-several generate-single-logic 'and
                    (fn [[a b]] (-> ops
                                    (with (generate-ast-with-context a))
                                    (with (generate-ast-with-context b))
                                    (with instruction)))
                    (->> args (drop 1) (interleave args) (partition 2))))

(defn generate-eq [args]
  (generate-comparison [(invokevirtual ['java 'lang 'Object]
                                       'equals
                                       [(gen-path 'java 'lang 'Object)]
                                       :boolean)
                        (invokestatic ['java 'lang 'Boolean]
                                      'valueOf
                                      [:boolean]
                                      (gen-path 'java 'lang 'Boolean))]
                       args))

(defn generate-neq [args]
  (-> ops
      (with (generate-eq args))
      (with (generate-single-logic 'not))))

(defn generate-le [args]
  (generate-comparison (generate-single-comparison 'lessOrEqual) args))

(defn generate-l [args]
  (generate-comparison (generate-single-comparison 'less) args))

(defn generate-g [args]
  (generate-comparison (generate-single-comparison 'greater) args))

(defn generate-ge [args]
  (generate-comparison (generate-single-comparison 'greaterOrEqual) args))

(defn generate-label []
  (set! last-label (inc last-label))
  (str "Label-" last-label))



                                        ;Conditionals
(defn generate-if [args]
  (let [label1 (generate-label)
        label2 (generate-label)]
    (-> ops
        (with add-comment (str ">>> if statement " label1 " / " label2))
        (with (generate-ast-with-context-to-boolean (first args)))
        (with invokevirtual [(gen-path 'java 'lang 'Boolean)] 'booleanValue [] :boolean)
        (with ifeq label1)
        (with (generate-ast-with-context (second args)))
        (with goto label2)
        (with label label1)
        (with (generate-ast-with-context (nth args 2)))
        (with label label2)
        (with add-comment (str "<<< if statement " label1 " / " label2)))))

(defn generate-cond-branch [end-label condition code]
  (if (nil? condition)
    (-> ops
        (with add-comment (str ">>> default branch cond " end-label))
        (with (generate-ast-with-context code))
        (with add-comment (str "<<< default branch cond" end-label)))
    (let [label-next (generate-label)]
      (-> ops
          (with add-comment (str ">>> cond branch " label-next))
          (with (generate-ast-with-context-to-boolean condition))
          (with invokevirtual [(gen-path 'java 'lang 'Boolean)] 'booleanValue [] :boolean)
          (with ifeq label-next)
          (with (generate-ast-with-context code))
          (with goto end-label)
          (with label label-next)
          (with add-comment (str "<<< cond branch " label-next))))))

(defn generate-cond [args]
  (let [end-label (generate-label)]
    (-> ops
        (with add-comment (str ">>> cond statement " end-label))
        (with (->> args
                   (partition 2)
                   (map (partial apply generate-cond-branch end-label))
                   (apply concat)
                   (into [])))
        (with (generate-cond-branch end-label nil (last args)))
        (with label end-label)
        (with add-comment (str "<<< cond statement " end-label)))))



                                        ;Lists
(defn generate-single-cons [arg]
  (-> ops
      (with dup)
      (with (generate-ast-with-context arg))
      (with invokevirtual ['java 'util 'ArrayList]
            'add
            [(gen-path 'java 'lang 'Object)]
            :boolean)
      (with pop1)))

(defn generate-multiple-cons [args]
  (->> args
       (map generate-single-cons)
       (apply concat)
       (into [])))

(defn generate-cons [args]
  (-> ops
      (with (-> args first generate-ast-with-context))
      (with (-> args rest generate-multiple-cons))))

(defn generate-list [args]
  (-> ops
      (with jnew (gen-path 'java 'util 'ArrayList))
      (with dup)
      (with invokenonvirtual ['java 'util 'ArrayList] '<init> [] :void)
      (with (->> args
                 (map generate-single-cons)
                 (apply concat)
                 (into [])))))

(defn generate-list-get [args]
  (-> ops
      (with (-> args first generate-ast-with-context))
      (with (-> args second generate-ast-with-context))
      (with invokevirtual ['java 'lang 'Number] 'intValue [] :int)
      (with invokevirtual ['java 'util 'ArrayList]
            'get [:int] (gen-path 'java 'lang 'Object))))

(defn generate-list-set [args]
  (-> ops
      (with (-> args first generate-ast-with-context))
      (with dup)
      (with (-> args second generate-ast-with-context))
      (with invokevirtual ['java 'lang 'Number] 'intValue [] :int)
      (with (-> args (nth 2) generate-ast-with-context))
      (with invokevirtual ['java 'util 'ArrayList]
            'set [:int (gen-path 'java 'lang 'Object)]
            (gen-path 'java 'lang 'Object))
      (with pop1)))


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
     (= type :<) (generate-l args)
     (= type :>) (generate-g args)
     (= type :>=) (generate-ge args)
     (= type :<=) (generate-le args)
     (= type :=) (generate-eq args)
     (= type :!=) (generate-neq args)
     (= type :if) (generate-if args)
     (= type :cond) (generate-cond args)
     (= type :list) (generate-list args)
     (= type :cons) (generate-cons args)
     (= type :get) (generate-list-get args)
     (= type :set) (generate-list-set args)
     :else (codegen-error))))

(defn generate-ast-with-context [ast]
  (if (vector? ast)
    (generate-sexpr ast)
    (generate-atom ast)))


(defn generate-ast [ast]
  (binding [last-label 0]
    (generate-ast-with-context ast)))
