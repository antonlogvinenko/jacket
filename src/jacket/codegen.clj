(ns jacket.codegen
  (:use [jacket.instructions]))

(def FILE-TYPES
  {:class ".class" :interface ".interface"})
(def ACCESS
  {:public "public" :private "private" :protected "protected" :package "package"})
(def TYPE
  {:int "I" :boolean "B" :char "C" :float "F" :double "D" :long "L" :void "V"})

(defn create-name [name-parts]
  (->> name-parts (interpose "/") (apply str)))

(defn gen-type [type]
  (cond
    (vector? type) (str \[ (apply gen-type type))
    (string? type) (str \L type \;)
    :else (TYPE type)))

(defn gen-arguments [arguments]
  (->> arguments (map gen-type) (apply str)))


(defn gen-path [& parts]
  (->> parts (map str) (interpose \/) (apply str)))


;;todo several implements
;;todo optional super

(defn instruction-text [instruction]
  (cond
    (string? instruction) instruction
    (vector? instruction) (->> instruction (interpose \space) (apply str))
    :else (-> "Unknown instruction format in codegenerator"
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
  (str ".super " (:super code)))

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

(defn methods-text [code]
  (->> code
       :methods
       (map method-text)))

(defn program-text [code]
  (->> (into [(file-type-text code)
              (super-text code)]
             (methods-text code))
       (interpose \newline)
       (apply str)))

(defn print-file [program-file file-name]
  (->> program-file program-text (spit file-name)))


(def prog
  {:access :public :type :class :name 'Cake
   :super (gen-path 'java 'lang 'Object)
   :methods
   [
    {:access :public :name "<init>"
     :arguments [] :return :void
     :instructions [aload_0
                    "invokenonvirtual java/lang/Object/<init>()V"
                    return]}
    
    {:access :public :static true :name "main"
     :arguments [[(gen-path 'java 'lang 'String)]] :return :void
     :instructions [
                    return
                    ]
     }
    
    ]})