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



