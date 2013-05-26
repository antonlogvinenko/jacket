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

(defn gen-file [type access name super-parts & methods]
  {:type (FILE-TYPES type)
   :access (ACCESS access)
   :name name
   :super (create-name super-parts)
   :methods methods})

(defn gen-method [access static name arguments return instructions]
  {:access access
   :static static
   :name name
   :arguments arguments
   :return return
   :instruction instructions})


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
       (:name code)))

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
       ".end"))

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