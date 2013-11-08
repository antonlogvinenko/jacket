(ns jacket.codegen.compile
  (:use [jacket.codegen.instructions]
        [jacket.lexer.lexer]
        [jacket.lexer.fsm]
        [jacket.parser]
        [jacket.semantics])
  (:import [jacket.lexer.fsm Token]))


                                        ;Program file to jasmin file
(defn debug [x] (println x) x)

(defn codegen-error [context args]
  (throw (RuntimeException. "Who is Mr. Putin?")))

(def ops {:def [] :ops [] :closures [] :globals [] :macro false})

(defmulti with (fn [ops arg1 & args] (class arg1)))
(defmethod with clojure.lang.PersistentArrayMap [{macro :macro :as p} arg1]
  (assoc (merge-with into (dissoc p :macro) arg1)
    :macro (or macro (:macro arg1))))
(defmethod with clojure.lang.PersistentVector [{ops :ops :as p} arg1]
  (assoc p :ops (into ops arg1)))
(defmethod with clojure.lang.Fn [{ops :ops :as p} arg1 & args]
  (assoc p :ops (into ops [(apply arg1 args)])))
(defmethod with java.lang.String [{ops :ops :as p} arg1]
  (assoc p :ops (into ops [arg1])))

(defn with-def [{ops :ops def :def closures :closures globals :globals macro :macro :as p}
            arg1 & args]
  (if (map? arg1)
    (assoc (merge-with into (dissoc p :macro :def) arg1)
      :macro (or macro (:macro arg1))
      :def (into def (arg1 :ops))
      :ops ops)
    {:closures closures
     :globals globals
     :macro macro
     :ops ops
     :def (into def (cond (vector? arg1) arg1
                          (empty? args) [arg1]
                          :else [(apply arg1 args)]))}))


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
      (with limitstack 30)
      (with (->> args
                 (map (partial generate-print-single context))
                 (reduce with ops)))))

(defn generate-println [context args]
  (-> ops
      (with (generate-print context args))
      (with invokestatic ['Console] 'println [] :void)
      (with aconst_null)))

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
              (gen-type (gen-path 'java 'lang 'Boolean))))))

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
                 (reduce with ops)))))


                                        ;Arithmetic operations
(defn generate-single-arithmetic [instruction]
  [(invokestatic ['Numbers]
                 instruction
                 [(gen-path 'java 'lang 'Object) (gen-path 'java 'lang 'Object)]
                 (gen-path 'java 'lang 'Object))])

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
                 [(gen-path 'java 'lang 'Object) (gen-path 'java 'lang 'Object)]
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

(defn get-unique-id [id-agent] (swap! id-agent + 1) @id-agent)

(defn generate-fun [{closure-atom :closure class-name :class}]
  (->> closure-atom get-unique-id (str class-name "-closure-")))


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
  (loop [context context, list list, accum ops]
    (if (empty? list) accum
        (let [[label context] (generate-label context)]
          (recur context
                 (rest list)
                 (with accum
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
      (with (generate-ast context arg))
      (with invokevirtual ['clojure 'lang 'PersistentVector]
            'cons
            [(gen-path 'java 'lang 'Object)]
            (gen-path 'clojure 'lang 'PersistentVector))))

(defn generate-multiple-cons [context args]
  (->> args
       (map (partial generate-single-cons context))
       (reduce with ops)))

(defn generate-cons [context args]
  (-> ops
      (with (->> args first (generate-ast context)))
      (with checkcast (gen-path 'clojure 'lang 'PersistentVector))
      (with (->> args rest (generate-multiple-cons context)))))

(defn generate-invokation-arguments [context args])
(defn generate-list-with-fn [generate-fn context args]
  (-> ops
      (with ldc_w (count args))
      (with anewarray (gen-path 'java 'lang 'Object))
      (with (generate-invokation-arguments generate-fn context args))
      (with invokestatic ['clojure 'lang 'PersistentVector]
            'create [[(gen-path 'java 'lang 'Object)]]
            (gen-path 'clojure 'lang 'PersistentVector))))
(defn generate-list [context args]
  (generate-list-with-fn generate-ast context args))

(defn generate-list-get [context args]
  (-> ops
      (with (->> args first (generate-ast context)))
      (with checkcast (gen-path 'clojure 'lang 'PersistentVector))
      (with (->> args second (generate-ast context)))
      (with invokevirtual ['java 'lang 'Number] 'intValue [] :int)
      (with invokevirtual ['clojure 'lang 'PersistentVector]
            'get [:int] (gen-path 'java 'lang 'Object))))

(defn generate-list-set [context args]
  (-> ops
      (with (->> args first (generate-ast context)))
      (with checkcast (gen-path 'clojure 'lang 'PersistentVector))
      (with (->> args second (generate-ast context)))
      (with (->> 2 (nth args) (generate-ast context)))
      (with invokevirtual ['clojure 'lang 'PersistentVector]
            'assoc [(gen-path 'java 'lang 'Object) (gen-path 'java 'lang 'Object)]
            (gen-path 'clojure 'lang 'IPersistentVector))))


                                        ;Let expression
;; context = {:local '({:a 1} {:a 1 :b 2})}
;; new :local list for each lambda
;; new {} for each let inside another one
(defn generate-let-variables [context pairs]
  (loop [context (->> {}
                      (conj (context :local))
                      (assoc context :local))
         pairs pairs
         code ops]
    (if (empty? pairs)
      [context code]
      (let [pair (first pairs)
            instructions (with code (->> pair second (generate-ast context)))
            local (->> context :local first)
            variable-name (first pair)
            variable-number (->> context :local (map count) (apply +))
            instructions (with instructions astore variable-number)
            context (->> variable-number
                         (assoc local variable-name)
                         (conj (drop 1 (context :local)))
                         (assoc context :local))]
        (recur context (rest pairs) instructions)))))

(defn generate-let [context args]
  (let [[context code] (generate-let-variables context (first args))
        body (generate-ast context (second args))]
    (-> ops
        (with limitlocals 10) 
        (with code)
        (with body))))

(defn generate-define [context args]
  (-> ops
      (with-def (->> args second (generate-ast context)))
      (with-def putstatic
            [(context :class) (first args)]
            (gen-type (gen-path 'java 'lang 'Object)))
      (with {:globals [(first args)]})))


                                        ;Closures
(defn generate-single-closure [context [arg idx]]
  (-> ops
      (with dup)
      (with ldc_w idx)
      (with (generate-ast context arg))
      (with aastore)))

(defn generate-closed-arguments [context closed]
  (->> closed
       (map (partial generate-single-closure context))
       (reduce with ops)))

(defn generate-closure [context args]
  (let [new-closure-name (generate-fun context)
        closed (concat
                (->> :globals context)
                (->> :closed context keys)
                (->> :arguments context keys)
                (->> :local context (apply merge) keys))
        closed (->> closed
                  (map-indexed (fn [i x] [x i]))
                  (into {}))
        new-context (assoc context
                      :local []
                      :class new-closure-name
                      :closed closed
                      :arguments (->> args
                                      first
                                      (map-indexed (fn [i x] [x i]))
                                      (into {})))
        body (->> args second (generate-ast new-context))
        closures (conj (:closures body)
                       [new-closure-name (conj (:ops body) areturn)])]
    (-> ops
        (with {:macro (:macro body)})
        (with {:closures closures})
        (with limitstack 30)
        (with jnew (gen-path new-closure-name))
        (with dup)

        (with ldc_w (-> :closed new-context count))
        (with anewarray (gen-path 'java 'lang 'Object))
        (with (generate-closed-arguments context closed))

        (with ldc_w (-> args first count))
        (with invokenonvirtual [new-closure-name]
              '<init> [[(gen-path 'java 'lang 'Object)] :int] :void))))

                                        ;Variables
(defn get-variable-number [context atom]
  (->> (-> context :local vec)
       (map #(get % atom))
       (filter (comp not nil?))
       first))

(defn generate-global-variable [context atom]
  (with ops getstatic [(context :class) atom] (gen-type (gen-path 'java 'lang 'Object))))

(defn get-argument-number [context atom]
  (-> context :arguments (get atom)))

(defn generate-fun-variable [name number context atom]
  (-> ops
      (with aload_0)
      (with getfield
            [(context :class) name]
            (gen-type [(gen-path 'java 'lang 'Object)]))
      (with ldc_w number)
      (with aaload)))

(defn generate-argument-variable [number context atom]
  (generate-fun-variable "arguments" number context atom))

(defn get-closed-variable-number [context atom]
  (-> context :closed (get atom)))

(defn generate-closed-variable [number context atom]
  (generate-fun-variable "closed" number context atom))

(defn generate-variable [context atom]
  (if-let [local-number (get-variable-number context atom)]
    (with ops aload local-number)
    (if-let [argument-number (get-argument-number context atom)]
      (generate-argument-variable argument-number context atom)
      (if-let [closed-number (get-closed-variable-number context atom)]
        (generate-closed-variable closed-number context atom)
        (generate-global-variable context atom)))))

(defn generate-single-argument [generate-fn context idx arg]
  (-> ops
      (with dup)
      (with ldc_w idx)
      (with (generate-fn context arg))
      (with aastore)))

(defn generate-invokation-arguments [generate-fn context args]
  (->> args
       (map-indexed (partial generate-single-argument generate-fn context))
       (reduce with ops)))

(defn generate-invokation [context args]
  (let [fun-args (rest args)
        name (-> args first)
        macro-names (:macro context)]
    (-> ops
        (with {:macro (and (-> name vector? not)
                           (some (partial = (.value name)) macro-names))})
        (with (->> args first (generate-ast context)))
        (with checkcast (gen-path 'AClosure))
        (with ldc_w (count fun-args))
        (with anewarray (gen-path 'java 'lang 'Object))
        (with (generate-invokation-arguments generate-ast context fun-args))
        (with invokevirtual ['AClosure]
              '_invoke
              [[(gen-path 'java 'lang 'Object)]]
              (gen-path 'java 'lang 'Object)))))


                                        ;java interop: static access
(defn static-something [context invokation arguments]
  (let [invokation (str invokation)
        [class-name access-name] (.split invokation "/")]
    (-> ops
        (with ldc_w class-name)
        (with ldc_w access-name)
        (with ldc_w (count arguments))
        (with anewarray (gen-path 'java 'lang 'Object))
        (with (generate-invokation-arguments generate-ast context arguments))
        (with invokestatic ['Interop]
              'accessStatic
              [(gen-path 'java 'lang 'String)
               (gen-path 'java 'lang 'String)
               [(gen-path 'java 'lang 'Object)]]
              (gen-path 'java 'lang 'Object)))))

(defn generate-static-method [context args]
  (let [invokation (->> args first .value .toString)
        fun-args (rest args)]
    (-> ops
        (with (static-something context invokation fun-args)))))

(defn generate-static-field [context atom]
  (let [[class-name access-name] (-> atom .value .toString (.split "/"))]
    (-> ops
        (with (static-something context (-> atom .value .toString) [])))))

                                        ;java interop: instantiation
(defn generate-instantiation [context args]
  (let [class-name (->> args first .value .toString seq (drop-last 1) (apply str))
        fun-args (rest args)]
    (-> ops
        (with ldc_w class-name)
        (with ldc_w (count fun-args))
        (with anewarray (gen-path 'java 'lang 'Object))
        (with (generate-invokation-arguments generate-ast context fun-args))
        (with invokestatic ['Interop]
              'instantiate
              [(gen-path 'java 'lang 'String) [(gen-path 'java 'lang 'Object)]]
              (gen-path 'java 'lang 'Object)))))

                                        ;java interop: instance access
(defn instance-something [context ref thing arguments]
  (-> ops
      (with (generate-ast context ref))
      (with ldc_w thing)
      (with ldc_w (count arguments))
      (with anewarray (gen-path 'java 'lang 'Object))
      (with (generate-invokation-arguments generate-ast context arguments))
      (with invokestatic ['Interop]
            'accessInstance
            [(gen-path 'java 'lang 'Object)
             (gen-path 'java 'lang 'String)
             [(gen-path 'java 'lang 'Object)]]
            (gen-path 'java 'lang 'Object))))

(defn generate-instance-access [context args]
  (let [access-name (->> args first .value .toString (drop 1) vec (apply str))
        ref (second args)
        fun-args (drop 2 args)]
    (-> ops
        (with (instance-something context ref access-name fun-args)))))

(defn generate-oop-access [context args]
  (let [arg1 (-> args first)
        arg2 (-> args second)
        arguments (drop 2 args)]
    (if (and (-> arg1 vector? not)
             (is? arg1 symbol?)
             (-> arg1 .value .toString (.contains "."))) 
      (static-something context (str (.value arg1) \/ (.value arg2)) arguments)
      (instance-something context arg1 (-> arg2 .value str) arguments))))


                                        ;Macros
(defn generate-atom [context args])
(defn generate-screened-list [context args])
(defn generate-screen [context args])
(defn generate-screened-symbol [context sym])
(defn generate-screened-keyword [context key])

(defn generate-screened-object [context expr]
  (cond
   (vector? expr) (generate-screened-list context expr)
   (is? expr symbol?) (generate-screened-symbol context expr)
   (is? expr keyword?) (generate-screened-keyword context expr)
   :else (generate-atom context expr)))

(defn generate-screened [context args]
  (let [expr (first args)]
    (generate-screened-object context expr)))

(defn generate-backtick [context args]
  (generate-screened (assoc context :unquote true) args))

(defn generate-quote [context args]
  (generate-screened (assoc context :unquote false) args))



                                        ;Macro list
(defn generate-unquote-splicing-and-into [context arg]
  (-> ops
      (with dup)
      (with (generate-ast (dissoc context :unquote) arg))
      (with invokevirtual ['java 'util 'LinkedList]
            'addAll [(gen-path 'java 'util 'Collection)]
            :boolean)
      (with pop1)))

(defn generate-screened-object-and-conj [context args]
  (-> ops
      (with dup)
      (with (generate-screened-object context args))
      (with invokevirtual ['java 'util 'LinkedList]
            'add [(gen-path 'java 'lang 'Object)]
            :boolean)
      (with pop1)))

(defn generate-screened-list-object [context args]
  (if (-> args vector? not)
    (generate-screened-object-and-conj context args)
    (let [arg1 (first args)
          arg2 (second args)]
      (if (and (= arg1 :unquote-splicing) (:unquote context))
        (generate-unquote-splicing-and-into context arg2)
        (generate-screened-object-and-conj context args)))))

(defn generate-macro-list [context args]
  (-> ops
      (with jnew (gen-path 'java 'util 'LinkedList))
      (with dup)
      (with invokenonvirtual ['java 'util 'LinkedList] '<init> [] :void)
      (with (->> args
                 (map (partial generate-screened-list-object context))
                 (reduce with ops)))
      (with invokestatic ['clojure 'lang 'PersistentVector]
            'create [(gen-path 'java 'util 'List)]
            (gen-path 'clojure 'lang 'PersistentVector))))

(defn generate-screened-list [context args]
  (let [arg1 (first args)
        arg2 (second args)]
    (if (and (= arg1 :unquote) (:unquote context))
      (generate-ast (dissoc context :unquote) arg2)
      (generate-macro-list context args))))

(defn get-name [name]
  (str name (System/currentTimeMillis) "#" (System/nanoTime)))

(defn get-unique-name [names name]
  (if-let [presented-name (get @names name)]
    presented-name
    (loop [new-name (get-name name)]
      (if (some (partial = new-name) (vals @names))
        (recur (get-name name))
        new-name))))

(defn generate-name [context name]
  (if (.endsWith name "#")
    (let [hygienic (:hygienic context)
          new-name (get-unique-name hygienic name)]
      (-> hygienic (swap! assoc name new-name) (get name)))
    name))

(defn generate-screened-symbol [context sym]
  (let [name (->> sym .value .getName (generate-name context))]
    (-> ops
        (with ldc_w name)
        (with invokestatic ['clojure 'lang 'Symbol]
              'create
              [(gen-path 'java 'lang 'String)]
              (gen-path 'clojure 'lang 'Symbol)))))

(defn generate-screened-keyword [context key]
  (let [name (-> key .value .getName)]
    (-> ops
        (with ldc_w name)
        (with invokestatic ['clojure 'lang 'Keyword]
              'intern
              [(gen-path 'java 'lang 'String)]
              (gen-path 'clojure 'lang 'Keyword)))))



(def sexpr-table
  {:println generate-println :print generate-print :readln generate-readln
   :+ generate-add :* generate-mul :- generate-sub (keyword "/") generate-div
   :and generate-and :or generate-or :not generate-not :xor generate-xor
   :< generate-l :> generate-g :<= generate-le :>= generate-ge := generate-eq :!= generate-neq
   :if generate-if :cond generate-cond
   :list generate-list :cons generate-cons :get generate-list-get :set generate-list-set
   :let generate-let :define generate-define
   :lambda generate-closure
   :quote generate-quote :backtick generate-backtick
   })

(defn generate-sexpr [context sexpr]
  (let [type (first sexpr)
        args (rest sexpr)
        handler (if (vector? type) generate-invokation
                    (get sexpr-table (.value type)
                         (let [value (-> type .value .toString)]
                           (cond
                            (= value ".") generate-oop-access
                            (.endsWith value ".") generate-instantiation
                            (.startsWith value ".") generate-instance-access
                            (.contains value "/") generate-static-method
                            :else generate-invokation))))]
    (if (some (partial = handler)
              [generate-invokation generate-instantiation generate-instance-access
               generate-static-method])
      (handler context sexpr)
      (handler context args))))

(defn generate-atom [context atom]
  (cond
   (is? atom string?) (generate-string-const context atom)
   (is? atom number?) (generate-number-const context atom)
   (is? atom boolean?) (generate-boolean-const context atom)
   (is? atom symbol?) (let [idx (-> atom .value .toString (.lastIndexOf "/"))]
                        (if (= idx -1)
                          (generate-variable context atom)
                          (generate-static-field context atom)))
   :else (codegen-error context atom)))

(defn generate-ast [context ast]
  (if (vector? ast)
    (generate-sexpr context ast)
    (generate-atom context ast)))
