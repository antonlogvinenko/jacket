(ns jacket.codegen)

(def TYPES {:class ".class" :interface ".interface"})
(def ACCESS {:public "public" :private "private" :protected "protected" :package "package"})
(defn create-name [name-parts]
  (->> name-parts (interpose "/") (apply str)))

(defn gen-file [type access name super-parts & methods]
  {:type (TYPES type)
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

;;todo
(defn gen-arguments [arguments] "[Ljava/lang/String;")
;;F / I / L<class>; / [ / V
;;todo
(defn gen-type [type] "V")


;;todo several implements
;;todo optional super

(defn instruction-text [instruction]
  (str \tab
       (cond
         (string? instruction) instruction
         (vector? instruction) (->> instruction (interpose \space) (apply str))
         :else (-> "Unknown instruction format in codegenerator"
                   RuntimeException.
                   throw))))

(defn instructions-text [instructions]
  (->> instructions
       (map instruction-text)
       (interpose \newline)
       (apply str)))

(defn file-type-text [code]
  (str (:type code)
       \space
       (:access code)
       \space
       (:name code)))

(defn super-text [code]
  (str ".super" (:super code)))

(defn method-text [method]
  (str ".method"
       \space
       (method :access)
       \space
       (if (-> method :static nil?) "" "static")
       \space
       (method :name)
       \( (method :arguments) \)
       (method :return)
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