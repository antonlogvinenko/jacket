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

(defn codegen-error [context args]
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

(defn generate-print-single [context arg]
  (-> ops
      (with (generate-ast context arg))
      (with invokestatic ['Console] 'print
            [(gen-path 'java 'lang 'Object)]
            :void)))

(defn generate-print [context args]
  (-> ops
      (with limitstack 10)
      (with (->> args
                 (map (partial generate-print-single context))
                 (reduce into [])))))

(defn generate-println [context args]
  (-> ops
      (with (generate-print context args))
      (with invokestatic ['Console] 'println [] :void)))

(defn generate-readln [context args]
  (-> ops
      (with invokestatic ['Console] 'readln [] (gen-path 'java 'lang 'String))))

(defn generate-read [context args]
  (-> ops
      (with invokestatic ['Console] 'read [] (gen-path 'java 'lang 'String))))

(defn generate-string-const [context ast]
  (with ops
        ldc (.value ast)))

(defn generate-float-const [context ast]
  (-> ops
      (with jnew (gen-path 'java 'lang 'Double))
      (with dup)
      (with ldc_w (.value ast))
      (with f2d)
      (with invokenonvirtual ['java 'lang 'Double] '<init> [:double] :void)))

(defn generate-integer-const [context ast]
  (-> ops
      (with jnew (gen-path 'java 'lang 'Long))
      (with dup)
      (with ldc_w (.value ast))
      (with i2l)
      (with invokenonvirtual ['java 'lang 'Long] '<init> [:long] :void)))

(defn generate-number-const [context ast]
  (cond
   (is? ast float?) (generate-float-const context  ast)
   (is? ast integer?) (generate-integer-const context ast)
   :else (codegen-error)))

(defn generate-boolean-const [context ast]
  (let [value (if (.value ast) 'TRUE 'FALSE)]
    (-> ops
        (with getstatic
              ['java 'lang 'Boolean value]
              ['java 'lang 'Boolean]))))

(defn generate-single-generic [instructions generate-arg context arg]
  (-> ops
      (with (generate-arg context arg))
      (with instructions)))

(defn generate-several [generate-single instruction generate-arg context args]
  (-> ops
      (with (generate-arg context (first args)))
      (with (->> args
                 rest
                 (map (partial generate-single-generic
                               (generate-single instruction)
                               generate-arg
                               context))
                 (reduce into [])))))


                                        ;Arithmetic operations
(defn generate-single-arithmetic [instruction]
  [(invokestatic ['Numbers]
                 instruction
                 [(gen-path 'java 'lang 'Number) (gen-path 'java 'lang 'Number)]
                 (gen-path 'java 'lang 'Number))])

(defn generate-arithmetic [instruction context args]
  (generate-several generate-single-arithmetic instruction generate-ast context args))

(defn generate-add [context args]
  (generate-arithmetic 'add context args))
(defn generate-mul [context args]
  (generate-arithmetic 'mul context args))
(defn generate-div [context args]
  (generate-arithmetic 'div context args))
(defn generate-sub [context args]
  (generate-arithmetic 'sub context args))


                                        ;Logic operations
(defn generate-ast-to-boolean [context arg]
  (-> ops
      (with (generate-ast context arg))
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

(defn generate-logic [instruction context args]
  (generate-several
   generate-single-logic instruction generate-ast-to-boolean context args))

(defn generate-and [context args]
  (generate-logic 'and context args))
(defn generate-or [context args]
  (generate-logic 'or context args))
(defn generate-xor [context args]
  (generate-logic 'xor context args))
(defn generate-not [context args]
  (-> ops
      (with (generate-ast-to-boolean context (first args)))
      (with (generate-single-logic 'not))))

(defn boolean? [atom]
  (-> atom type (= java.lang.Boolean)))



                                        ;Comparison
(defn generate-single-comparison [instruction]
  [(invokestatic ['Comparison]
                 instruction
                 [(gen-path 'java 'lang 'Number) (gen-path 'java 'lang 'Number)]
                 (gen-path 'java 'lang 'Boolean))])

(defn generate-comparison [instruction context args]
  (generate-several generate-single-logic 'and
                    (fn [context [a b]] (-> ops
                                            (with (generate-ast context a))
                                            (with (generate-ast context b))
                                            (with instruction)))
                    context
                    (->> args (drop 1) (interleave args) (partition 2))))

(defn generate-eq [context args]
  (generate-comparison [(invokevirtual ['java 'lang 'Object]
                                       'equals
                                       [(gen-path 'java 'lang 'Object)]
                                       :boolean)
                        (invokestatic ['java 'lang 'Boolean]
                                      'valueOf
                                      [:boolean]
                                      (gen-path 'java 'lang 'Boolean))]
                       context
                       args))

(defn generate-neq [context args]
  (-> ops
      (with (generate-eq context args))
      (with (generate-single-logic 'not))))

(defn generate-le [context args]
  (generate-comparison (generate-single-comparison 'lessOrEqual) context args))

(defn generate-l [context args]
  (generate-comparison (generate-single-comparison 'less) context args))

(defn generate-g [context args]
  (generate-comparison (generate-single-comparison 'greater) context args))

(defn generate-ge [context args]
  (generate-comparison (generate-single-comparison 'greaterOrEqual) context args))

(defn generate-label [{label :label :as context}]
  (let [last-label (inc label)
        label-name (str "Label-" last-label)]
    [label-name (assoc context :label last-label)]))


                                        ;Conditionals
(defn generate-if [context args]
  (let [[label1 context] (generate-label context)
        [label2 context] (generate-label context)]
    (-> ops
        (with add-comment (str ">>> if statement " label1 " / " label2))
        (with (generate-ast-to-boolean context (first args)))
        (with invokevirtual [(gen-path 'java 'lang 'Boolean)] 'booleanValue [] :boolean)
        (with ifeq label1)
        (with (generate-ast context (second args)))
        (with goto label2)
        (with label label1)
        (with (generate-ast context (nth args 2)))
        (with label label2)
        (with add-comment (str "<<< if statement " label1 " / " label2)))))

(defn generate-cond-branch [end-label label-next context condition code]
  (if (nil? condition)
    (-> ops
        (with add-comment (str ">>> default branch cond " end-label))
        (with (generate-ast context code))
        (with add-comment (str "<<< default branch cond" end-label)))
    (-> ops
        (with add-comment (str ">>> cond branch " label-next))
        (with (generate-ast-to-boolean context condition))
        (with invokevirtual [(gen-path 'java 'lang 'Boolean)] 'booleanValue [] :boolean)
        (with ifeq label-next)
        (with (generate-ast context code))
        (with goto end-label)
        (with label label-next)
        (with add-comment (str "<<< cond branch " label-next)))))

(defn generate-cond-branches [end-label context list]
  (loop [context context, list list, accum []]
    (if (empty? list) accum
        (let [[label context] (generate-label context)]
          (recur context
                 (rest list)
                 (into accum
                       (apply
                        (partial generate-cond-branch end-label label context)
                        (first list))))))))

(defn generate-cond [context args]
  (let [[end-label context] (generate-label context)]
    (-> ops
        (with add-comment (str ">>> cond statement " end-label))
        (with (->> args
                   (partition 2)
                   (generate-cond-branches end-label context)))
        (with (generate-cond-branch end-label nil context nil (last args)))
        (with label end-label)
        (with add-comment (str "<<< cond statement " end-label)))))



                                        ;Lists
(defn generate-single-cons [context arg]
  (-> ops
      (with dup)
      (with (generate-ast context arg))
      (with invokevirtual ['java 'util 'ArrayList]
            'add
            [(gen-path 'java 'lang 'Object)]
            :boolean)
      (with pop1)))

(defn generate-multiple-cons [context args]
  (->> args
       (map (partial generate-single-cons context))
       (apply concat)
       (into [])))

(defn generate-cons [context args]
  (-> ops
      (with (->> args first (generate-ast context)))
      (with (->> args rest (generate-multiple-cons context)))))

(defn generate-list [context args]
  (-> ops
      (with jnew (gen-path 'java 'util 'ArrayList))
      (with dup)
      (with invokenonvirtual ['java 'util 'ArrayList] '<init> [] :void)
      (with (->> args
                 (map (partial generate-single-cons context))
                 (apply concat)
                 (into [])))))

(defn generate-list-get [context args]
  (-> ops
      (with (->> args first (generate-ast context)))
      (with (->> args second (generate-ast context)))
      (with invokevirtual ['java 'lang 'Number] 'intValue [] :int)
      (with invokevirtual ['java 'util 'ArrayList]
            'get [:int] (gen-path 'java 'lang 'Object))))

(defn generate-list-set [context args]
  (-> ops
      (with (->> args first (generate-ast context)))
      (with dup)
      (with (->> args second (generate-ast context)))
      (with invokevirtual ['java 'lang 'Number] 'intValue [] :int)
      (with (->> 2 (nth args) (generate-ast context)))
      (with invokevirtual ['java 'util 'ArrayList]
            'set [:int (gen-path 'java 'lang 'Object)]
            (gen-path 'java 'lang 'Object))
      (with pop1)))


(defn generate-atom [context atom]
  (cond
   (is? atom string?) (generate-string-const context atom)
   (is? atom number?) (generate-number-const context atom)
   (is? atom boolean?) (generate-boolean-const context atom)
   :else (codegen-error context atom)))

(def sexpr-table
  {:println generate-println :print generate-print :readln generate-readln
   :+ generate-add :* generate-mul :- generate-sub (keyword "/") generate-div
   :and generate-and :or generate-or :not generate-not :xor generate-xor
   :< generate-l :> generate-g :<= generate-le :>= generate-ge := generate-eq :!= generate-neq
   :if generate-if :cond generate-cond
   :list generate-list :cons generate-cons :get generate-list-get :set generate-list-set
   })

(defn generate-sexpr [context sexpr]
  (let [type (first sexpr)
        args (rest sexpr)
        handler (get sexpr-table (.value type) codegen-error)]
    (handler context args)))

(defn generate-ast [context ast]
  (if (vector? ast)
    (generate-sexpr context ast)
    (generate-atom context ast)))
