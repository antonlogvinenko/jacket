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

(defn generate-sexpr [])
(defn generate-ast [])

(defn generate-println [args]
  (concat
   [(limitstack 10)]
   (-> args first generate-ast)
   [(invokestatic ['Console] 'println [(gen-path 'java 'lang 'String)] :void)]))


(defn generate-string-const [ast]
  [(-> ast .value ldc)])

(defn generate-float-const [ast]
  [(jnew (gen-path 'java 'lang 'Double))
   dup
   (-> ast .value ldc_w)
   f2d
   (invokenonvirtual ['java 'lang 'Double] '<init> [:double] :void)])

(defn generate-integer-const [ast]
  [(jnew (gen-path 'java 'lang 'Long))
   dup
   (-> ast .value ldc_w)
   (invokenonvirtual ['java 'lang 'Long] '<init> [:long] :void)])

(defn generate-number-const [ast]
  (cond
   (is? ast float?) (generate-float-const ast)
   (is? ast integer?) (generate-integer-const ast)
   :else (throw (RuntimeException. "Who is Mr. Putin?"))
   ))

(defn generate-add-of-two [args]
  (concat
   (->> args (map generate-ast) (apply concat))
   [(invokestatic ['Numbers]
                  'add
                  [(gen-path 'java 'lang 'Number) (gen-path 'java 'lang 'Number)]
                  (gen-path 'java 'lang 'String))]))

(defn generate-add [args]
  (let [length (count args)]
    (condp = length
      1 (generate-ast (first args))
      2 (generate-add-of-two args)
      )))

(defn generate-atom [ast]
  (cond
   (is? ast string?) (generate-string-const ast)
   (is? ast number?) (generate-number-const ast)))

(defn generate-sexpr [ast]
  (let [type (first ast)
        args (rest ast)]
    (cond
     (= type :println) (generate-println args)
     (= type :+) (generate-add args)
     :else (throw (RuntimeException.)))))

(defn generate-ast [ast]
  (if (vector? ast)
    (generate-sexpr ast)
    (generate-atom ast)))
