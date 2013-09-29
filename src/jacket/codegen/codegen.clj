(ns jacket.codegen.codegen
  (:use [jacket.codegen.classgen]
        [jacket.codegen.instructions]
        [jacket.codegen.compile]
        [jacket.codegen.macro]
        [jacket.lexer.lexer]
        [jacket.parser]
        [clojure.java.shell]
        [jacket.semantics])
  (:import [jacket.lexer.fsm Token]))


(defn get-fields-names [ast]
  (->> ast
       (filter #(-> % first (= :define)))
       (map second)))

(defn generate [[ast macro] name]
  (let [fields (get-fields-names ast)
        code (loop [code [], ast ast, globals []]
               (if (empty? ast) code
                   (let [ops (generate-ast
                                      {:label 0 :class name
                                       :macro macro
                                       :closure (agent 0)
                                       :globals globals :arguments {} :closed {} :local '()}
                                      (first ast))]
                     (recur (conj code ops) (rest ast) (into globals (:globals ops))))))
        ops (map :ops code)
        defs (map :def code)
        macro-usage (map (comp true? :macro) code)
        closures (map :closures code)]
    {:programs (conj (->> closures (apply concat) generate-closures)
                     [name (->> ops
                                (apply concat)
                                (generate-main-class name fields (apply concat defs)))])
     :macros macro-usage}))

(defn compile-code [{asm :programs macros :macros} location name]
  (doall
   (for [[name code] asm]
     (do
       (->> code program-text (spit (str location name ".jasm")))
       (with-sh-dir "." (sh "./compile-wardrobe-single.sh" name))))))

(defn expand-and-compile [asm location name]
  (loop [asm asm]
    (let [assembly (generate asm name)
          macro-usage (assembly :macros)
          macro-expand-required (reduce #(or %1 %2) macro-usage)]
      (compile-code assembly location name)
      (if macro-expand-required
        (-> asm (expand-macro macro-usage) recur)
        true))))

(defn compile-jacket [in location name]
  (-> in
      slurp
      tokenize
      parse
      semantics
      macro-definitions
      (expand-and-compile location name)))
