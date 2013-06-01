(ns jacket.codegen
  (:use [jacket.instructions]))

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

(defn methods-text [code]
  (->> code
       :methods
       (map method-text)))

(defn program-text [code]
  (->> (into [(file-type-text code)
              (super-text code)
              (implements-text code)]
             (methods-text code))
       (interpose \newline)
       (apply str)))

(defn print-file [program-file file-name]
  (->> program-file program-text (spit file-name)))


(def default-init
  {:access :public :name "<init>"
   :arguments [] :return :void
   :instructions [aload_0
                  (invokenonvirtual ['java 'lang 'Object] '<init> [] :void)
                  return]})

(def hello-world
  {:access :public :type :class :name 'HelloWorld
   :super (gen-path 'java 'lang 'Object)
   :methods
   [default-init
    {:access :public :static true :name "main"
     :arguments [[(gen-path 'java 'lang 'String)]] :return :void
     :instructions [
                    (limitstack 2)
                    (getstatic ['java 'lang 'System 'out]
                               ['java 'io 'PrintStream])
                    (ldc "Hello world!!!")
                    (invokevirtual ['java 'io 'PrintStream]
                                   'println
                                   [(gen-path 'java 'lang 'String)]
                                   :void)
                    return
                    ]}]})

(defn gen-hello-world []
  (->> hello-world program-text (spit "HelloWorld.jt")))
