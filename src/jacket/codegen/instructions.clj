(ns jacket.codegen.instructions)


(def TYPE
  {:int "I" :boolean "Z" :char "C" :float "F" :double "D" :long "J" :void "V"})

(defn gen-type [type]
  (cond
   (vector? type) (str \[ (apply gen-type type))
   (string? type) (str \L type \;)
   :else (TYPE type)))

(defn gen-path [& parts]
  (->> parts (map str) (interpose \/) (apply str)))

(defn gen-arguments [arguments]
  (->> arguments (map gen-type) (apply str)))

(defn format-constant [constant]
  (if (string? constant)
    (str "\"" constant "\"")
    constant))

(defn gen-method-header [method arguments return]
  (str method
       \( (gen-arguments arguments) \)
       (gen-type return)))

(defn invoke-some-method [class-path method arguments return]
  (str (apply gen-path class-path)
       \/
       (gen-method-header method arguments return)))

(defn get-some-field [field-path field-type]
  (str (apply gen-path field-path)
       \space
       (->> field-type (apply gen-path) gen-type)))


                                        ;MACROS FOR INSTRUCTIONS DEFINITIONS
                                        ;http://jasmin.sourceforge.net/instructions.html
(defmacro def-n [& args]
  `(do ~@(->> args
              (partition 2)
              (map (partial cons 'def)))))

(defmacro defn-n [& args]
  `(do ~@(->> args
              (partition 3)
              (map (partial cons 'defn)))))


                                        ;RETURN
(def-n return "return" areturn "areturn"
  dreturn "dreturn" freturn "freturn" ireturn "ireturn" lreturn "lreturn")
(defn ret [var-num] ["ret" var-num])


                                        ;LOCAL VAIRABLES
(defn-n
  astore [var-num] ["astore" var-num]
  dstore [var-num] ["dstore" var-num]
  fstore [var-num] ["fstore" var-num]
  istore [var-num] ["istore" var-num]
  lstore [var-num] ["lstore" var-num])
(def-n
  dstore_0 "dstore_0" dstore_1 "dstore_1" dstore_2 "dstore_2" dstore_3 "dstore_3"
  astore_0 "astore_0" astore_1 "astore_1" astore_2 "astore_2" astore_3 "astore_3"
  fstore_0 "fstore_0" fstore_1 "fstore_1" fstore_2 "fstore_2" fstore_3 "fstore_3"
  istore_0 "istore_0" istore_1 "istore_1" istore_2 "istore_2" istore_3 "istore_3"
  lstore_0 "lstore_0" lstore_1 "lstore_1" lstore_2 "lstore_2" lstore_3 "lstore_3")

(defn-n
  dload [var-num] ["dload" var-num]
  fload [var-num] ["fload" var-num]
  iload [var-num] ["iload" var-num]
  lload [var-num] ["lload" var-num]
  aload [var-num] ["aload" var-num])
(def-n
  aload_0 "aload_0" aload_1 "aload_1" aload_2 "aload_2" aload_3 "adlod_3"
  dload_0 "dload_0" dload_1 "dload_1" dload_2 "dload_2" dload_3 "dload_3"
  fload_0 "fload_0" fload_1 "fload_1" fload_2 "fload_2" fload_3 "fload_3"
  iload_0 "iload_0" iload_1 "iload_1" iload_2 "iload_2" iload_3 "iload_3"
  lload_0 "lload_0" lload_1 "lload_1" lload_2 "lload_2" lload_3 "lload_3")


                                        ;ARRAYS AND LOCAL VARIABLES
(def-n
  aaload "aaload" baload "baload" caload "caload" daload "daload"
  faload "faload" iaload "iaload" laload "laload" saload "saload")
(def-n
  aastore "aastore" bastore "bastore" castore "castore" dastore "dastore"
  fastore "fastore" iastore "iastore" lastore "lastore" sastore "sastore")


                                        ;BIPUSH, SIPUSH, IINC
(defn sipush [int] ["sipush" int])
(defn bipush [int] ["bipush" int])
(defn iinc [var-name amount] ["iinc" var-name amount])


                                        ;BRANCH
(defn goto [label] ["goto" label])
(defn goto_w [label] ["goto_2" label])
(defn-n
  if_acmpeq [label] ["if_acmpeq" label]
  if_acmpne [label] ["if_acmpne" label]
  if_icmpeq [label] ["if_icmpeq" label]
  if_icmpne [label] ["if_icmpne" label]
  if_icmplt [label] ["if_icmplt" label]
  if_icmple [label] ["if_icmple" label]
  if_icmpgt [label] ["if_icmpgt" label]
  if_icmpge [label] ["if_icmpge" label])
(defn-n
  ifeq [label] ["ifeq" label]
  ifne [label] ["ifne" label]
  iflt [label] ["iflt" label]
  ifgt [label] ["ifgt" label]
  ifle [label] ["ifle" label]
  ifge [label] ["ifge" label])
(defn-n
  ifnonnull [label] ["ifnonnull" label]
  ifnull [label] ["ifnull" label])
(defn-n
  jsr [label] ["jsr" label]
  jsr_w [label] ["jsr_w" label])


                                        ;CLASS AND OBJECT OPERATIONS
(defn checkcast [class] ["checkcast" class])
(defn instanceof [class] ["instanceof" class])
(defn jnew [class] ["new" class])


                                        ;METHOD INVOCATION
(defn invokevirtual [class-path method arguments return]
  (vector "invokevirtual" (invoke-some-method class-path method arguments return)))
(defn invokenonvirtual [class-path method arguments return]
  (vector "invokenonvirtual" (invoke-some-method class-path method arguments return)))
(defn invokestatic [class-path method arguments return]
  (vector "invokestatic" (invoke-some-method class-path method arguments return)))
(defn invokeinterface [class-path method arguments return num-args]
  (vector "invokeinterface" (invoke-some-method class-path method arguments return) num-args))


                                        ;FIELD MANIPULATION
(defn-n
  getfield [field-spec descriptor] (vector "getfield" (get-some-field field-spec descriptor))
  getstatic [field-spec descriptor] (vector "getstatic" (get-some-field field-spec descriptor))
  putfield [field-spec descriptor] (vector  "putfield" (get-some-field field-spec descriptor))
  putstatic [field-spec descriptor] (vector "putstatic" (get-some-field field-spec descriptor)))


                                        ;NEWARRAY
(defn-n
  anewarray [class] ["anewarray" class]
  newarray [array-type] ["newarray" array-type]
  multinewarray [array-descriptor num-dimensions]
  ["multinewarray" array-descriptor num-dimensions])
(def arraylength "arraylength")


                                        ;LDC AND LDC_W
(defn-n
  ldc [constant] ["ldc" (format-constant constant)]
  ldc_w [constant] ["ldc_w" (format-constant constant)])


                                        ;LOOKUPSWITCH, TABLESWITCH
(defn lookupswitch [] ["lookupswitch"])
(defn tableswitch [] ["tableswitch"])


                                        ;THROW
(def athrow "athrow")


                                        ;MONITOR
(def-n monitorenter "monitorenter", monitorexit "monitorexit")


                                        ;CONVERSION
(def-n
  d2f "d2f" d2i "d2i" d2l "d2l"
  f2d "f2d" f2i "f2i" f2l "f2l"
  i2b "i2b" i2c "i2c" i2d "i2d" i2f "i2f" i2l "i2l" i2s "i2s"
  l2d "l2d" l2f "l2f" l2i "l2i")


                                        ;ARITHMETICS
(def-n
  dadd "dadd" fadd "fadd" iadd "iadd" ladd "ladd"
  ddiv "ddiv" fdib "fdiv" idiv "idiv" ldiv "ldiv"
  dmul "dmul" fmul "fmul" imul "imul" lmul "lmul"
  dneg "dneg" fneg "fneg" ineg "ineg" lneg "lneg"
  frem "frem" irem "irem" lrem "lrem" drem "drem"
  dsub "dsub" fsub "fsub" isub "isub" lsub "lsub")


                                        ;BITWISE
(def-n ishl "ishl" lshl "lshl"
  ishr "ishr" lshr "lshr"
  iushr "iushr" lushr "lushr"
  lxor "lxor" ixor "ixor")


                                        ;BOOLEAN
(def-n iand "iand" land "land" ior "ior" lor "lor")


                                        ;COMPARISON
(def-n
  dcmpg "dcmpg" fcmpg "fcmpg" dcmpl "dcmpl" fcmpl "fcmpl" lcmp "lcmp")


                                        ;STACK CONSTANTS
(def aconst_null "aconst_null")
(def-n
  dconst_0 "dconst_0" dconst_1 "dconst_1"
  fconst_0 "fconst_0" fconst_1 "fconst_1"
  iconst_0 "iconst_0" iconst_1 "iconst_1"
  lconst_0 "lconst_0" lconst_1 "lconst_1")


                                        ;STACK MANIPULATION
(def-n
  dup "dup" dup_x1 "dup_x1" dup_x2 "dup_x2"
  dup2 "dup2" dup2_x1 "dup2_x1" dup2_x2 "dup2_x2"
  pop1 "pop" pop2 "pop2"
  swap "swap")


                                        ;THINGS
(defn add-comment [comment] (str \tab \; comment))
(def nop "nop")
(defn-n
  limitstack [n] [".limit stack" n]
  limitlocals [n] [".limit locals" n]
  line [n] [".line" n]
  label [l] (str l \:))
