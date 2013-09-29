(ns jacket.codegen.classgen
  (:use [jacket.codegen.instructions]))

(def FILE-TYPES
  {:class ".class" :interface ".interface"})
(def ACCESS
  {:public "public" :private "private" :protected "protected" :package "package"})

(defn create-name [name-parts]
  (->> name-parts (interpose "/") (apply str)))

(defn instruction-text [instruction]
  (cond
    (string? instruction) instruction
    (vector? instruction) (->> instruction (interpose \space) (apply str))
    :else (-> (str "Unknown instruction format in codegenerator: " instruction)
              RuntimeException.
              throw)))

(defn instructions-text [instructions]
  (->> instructions
       (map (comp (partial str \tab) instruction-text))
       (interpose \newline)
       (apply str)))

(defn file-type-text [code]
  (str (-> code :type FILE-TYPES)
       \space
       (-> code :access ACCESS)
       \space
       (-> :name code str)))

(defn super-text [code]
  (str ".super "
       (get code :super (gen-path 'java 'lang 'Object))))

(defn implements-text [code]
  (->> code
       :implements
       (map (partial str ".implements "))
       (interpose \newline)
       (apply str)))

(defn method-text [method]
  (str ".method"
       \space
       (-> method :access ACCESS)
       \space
       (if (-> method :static true?) (str "static" \space) "")
       (method :name)
       \( (-> :arguments method gen-arguments) \)
       (-> :return method gen-type)
       \newline
       (-> method :instructions instructions-text)
       \newline
       ".end method"))

(defn field-text [field]
  (str ".field"
       \space
       (ACCESS :public)
       \space
       "static"
       \space
       field
       \space
       (gen-type (gen-path 'java 'lang 'Object))))

(defn fields-text [code]
  (->> code
       :fields
       (map field-text)
       (into [])))

(defn methods-text [code]
  (->> code
       :methods
       (map method-text)))

(defn program-text [code]
  (->> (-> [(file-type-text code)
              (super-text code)
              (implements-text code)]
           (into (fields-text code))
           (into (methods-text code)))
       (interpose \newline)
       (apply str)))


(def object-path (gen-path 'java 'lang 'Object))



(def default-init
  {:access :public :name "<init>"
   :arguments [] :return :void
   :instructions [aload_0
                  (invokenonvirtual ['java 'lang 'Object] '<init> [] :void)
                  return]})

(defn clinit [instructions]
  {:access :public :name "<clinit>"
   :arguments [] :return :void
   :instructions (concat [(limitstack 10) (limitlocals 10)] instructions [return])})

(defn generate-class [name super implements fields methods]
  {:access :public :type :class :name name
   :super super
   :implements implements
   :fields fields
   :methods methods})


(defn generate-main-class [name fields clinit-instructions instructions]
  (generate-class name object-path [] fields
                  [default-init
                   (clinit clinit-instructions)
                   {:access :public :static true :name "main"
                    :arguments [[(gen-path 'java 'lang 'String)]]
                    :return :void
                    :instructions (concat instructions [return])}]))

(defn generate-closures [closures]
  (for [[name code] closures]
    [name (generate-class name (gen-path 'AClosure) [] []
                          [{:access :public :static false :name "<init>"
                            :arguments [[(gen-path 'java 'lang 'Object)]  :int]
                            :return :void
                            :instructions [(limitstack 3)
                                           (limitlocals 3)
                                           aload_0
                                           aload_1
                                           iload_2
                                           (invokenonvirtual
                                            ['AClosure] '<init>
                                            [[(gen-path 'java 'lang 'Object)] :int]
                                            :void)
                                           return]}
                           {:access :public :static false :name "invoke"
                            :arguments []
                            :return (gen-path 'java 'lang 'Object)
                            :instructions (into [(limitstack 30)
                                                 (limitlocals 30)]
                                                code)}])]))
