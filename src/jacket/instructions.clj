(ns jacket.instructions)

;;pop object<reference>
;;push value
(defn getfield [indexbyte1 indexbyte2] ["getfield" indexbyte1 indexbyte2])
(defn getstatic [indexbyte1 indexbyte2] ["getstatic" indexbyte1 indexbyte2])
;;pop object<reference>, value
(defn putfield [indexbyte1 indexbyte2] ["putfield" indexbyte1 indexbyte2])
;;pop value
(defn putstatic [indexbyte1 indexbyte2] ["putstatic" indexbyte1 indexbyte2])

(defmacro def-n [& args]
  `(do ~@(->> args
              (partition 2)
              (map (partial cons 'def)))))


;;pop arg1[, arg2[, ...]]
(defn inbokedynamic [indexbyte1 indexbyte2]
  ["invokedynamic" indexbyte1 indexbyte2 0 0])
;;pop object<reference>, arg1[, arg2[, ...]]
(defn invokeinterface [indexbyte1 indexbyte2 count]
  ["invokeinterface" indexbyte1 indexbyte2 count])
;;pop object<reference>, arg1[, arg2[, ...]]
;;pop arg1[, arg2[, ...]]
(defn invokestatic [indexbyte1 indexbyte2]
  ["invokestatic" indexbyte1 indexbyte2])
;;pop object<reference>, arg1[, arg2[, ...]]
(defn invokevirtual [indexbyte1 indexbyte2]
  ["inbokevirtual" indexbyte1 indexbyte2])
(defn invokespecial [indexbyte1 indexbyte2]
  ["invokespecial" indexbyte1 indexbyte2])



;;pop object<reference>
(def monitorenter "monitorenter")
(def monitorexit "monitorexit")



(defn goto [branchbyte1 branchbyte2] ["goto" branchbyte1 branchbyte2])
(defn goto_2 [branchbyte1 branchbyte2 branchbyte3 branchbyte4]
  ["goto_2" branchbyte1 branchbyte2 branchbyte3 branchbyte4])
;;pop object<type>, object<type>
(defn if_acmpeq [branchbyte1 branchbyte2] ["if_acmpeq" branchbyte1 branchbyte2])
(defn if_acmpne [branchbyte1 branchbyte2] ["if_acmpne" branchbyte1 branchbyte2])
(defn if_icmpeq [branchbyte1 branchbyte2] ["if_icmpeq" branchbyte1 branchbyte2])
(defn if_icmpne [branchbyte1 branchbyte2] ["if_icmpne" branchbyte1 branchbyte2])
(defn if_icmplt [branchbyte1 branchbyte2] ["if_icmplt" branchbyte1 branchbyte2])
(defn if_icmple [branchbyte1 branchbyte2] ["if_icmple" branchbyte1 branchbyte2])
(defn if_icmpgt [branchbyte1 branchbyte2] ["if_icmpgt" branchbyte1 branchbyte2])
(defn if_icmpge [branchbyte1 branchbyte2] ["if_icmpge" branchbyte1 branchbyte2])
;;pop value
(defn ifeq [branchbyte1 branchbyte2] ["ifeq" branchbyte1 branchbyte2])
(defn ifne [branchbyte1 branchbyte2] ["ifne" branchbyte1 branchbyte2])
(defn iflt [branchbyte1 branchbyte2] ["iflt" branchbyte1 branchbyte2])
(defn ifgt [branchbyte1 branchbyte2] ["ifgt" branchbyte1 branchbyte2])
(defn ifle [branchbyte1 branchbyte2] ["ifle" branchbyte1 branchbyte2])
(defn ifge [branchbyte1 branchbyte2] ["ifge" branchbyte1 branchbyte2])
;;pop value<reference>
(defn ifnonnull [branchbyte1 branchbyte2] ["ifnonnull" branchbyte1 branchbyte2])
(defn ifnull [branchbyte1 branchbyte2] ["ifnull" branchbyte1 branchbyte2])
;;push returnAddress
(defn jsr [branchbyte1 branchbyte2] ["jsr" branchbyte1 branchbyte2])
(defn jsr [branchbyte1 branchbyte2 branchbyte3 branchbyte4]
  ["jsr" branchbyte1 branchbyte2 branchbyte3 branchbyte4])
;;pop key
;;todo - learn and implement when required
(defn lookupswitch
  [bytespad
   defaultbyte1 defaultbyte2 defaultbyte3 defaultbyte4
   npairs1 npairs2 npairs3 npairs4 & matchoffsetpairs]
  ["lookupswitch"])
;;todo
(defn tableswitch
  [pad
   defaultbyte1 defaultbyte2 defaultbyte3 defaultbyte4
   lowbyte1 lowbyte2 lowbyte3 lowbyte4
   highbyte1 highbyte2 highbyte3 highbyte4
   & jumpoffsets]
  ["tableswitch"])
;;pop object<reference>
(def athrow "athrow")



;;pop count<int>
;;push array<reference>
;;index in constant pool refers to a class/interface/array type
(defn anewarray [indexbyte1 indexbyte2] ["anewarray" indexbyte1 indexbyte2])
;;pop count<int>
;;push array<reference>
;;bool-4, char-5, float-6, double-7, byte-8, short-9, int-10, long-11
(defn newarray [atype] ["newarray" atype])
;;push object<reference>
;;index in constant pool refers to a class/interface type
(defn new [indexbyte1 indexbyte2] ["new" indexbyte1 indexbyte2])
;;pop count1, count2, ...
;;push array<reference>
(defn multinewarray [indexbyte1 indexbyte2 dimensions]
  ["multinewarray" indexbyte1 indexbyte2 dimensions])
;;pop array<ref>
;;push length<int>
(def arraylength "arraylength")



;;pop arrayref<reference>, index<int>
;;push value<op-type>
(def-n
  aaload "aaload"
  baload "baload"
  caload "caload"
  daload "daload"
  faload "faload"
  iaload "iaload"
  laload "laload"
  saload "saload")
;;pop arrayref<reference>, index<int>, value<op-type>
;;put value<op-type>, value type must be compatible
(def-n
  aastore "aastore"
  bastore "bastore"
  castore "castore"
  dastore "dastore"
  fastore "fastore"
  iastore "iastore"
  lastore "lastore"
  sastore "sastore")



;;pop object<reference|returnAddress>
(defn astore [index] ["astore" index])
(def-n astore_0 "astore_0" astore_1 "astore_1" astore_2 "astore_2" astore_3 "astore_3")
;;push reference from local variable
(defn aload [index] ["aload" index])
(def-n aload_0 "aload_0" aload_1 "aload_1" aload_2 "aload_2" aload_3 "adlod_3")
;;pop value
(defn dstore [index] ["dstore" index])
(def dstore_0 "dstore_0")
(def dstore_1 "dstore_1")
(def dstore_2 "dstore_2")
(def dstore_3 "dstore_3")
(defn fstore [index] ["fstore" index])
(def fstore_0 "fstore_0")
(def fstore_1 "fstore_1")
(def fstore_2 "fstore_2")
(def fstore_3 "fstore_3")
(defn istore [index] ["istore" index])
(def istore_0 "istore_0")
(def istore_1 "istore_1")
(def istore_2 "istore_2")
(def istore_3 "istore_3")
(defn lstore [index] ["lstore" index])
(def lstore_0 "lstore_0")
(def lstore_1 "lstore_1")
(def lstore_2 "lstore_2")
(def lstore_3 "lstore_3")
;;push from local variable
(defn dload [index] ["dload" index])
(def dload_0 "dload_0")
(def dload_1 "dload_1")
(def dload_2 "dload_2")
(def dload_3 "dload_3")
(defn fload [index] ["fload" index])
(def fload_0 "fload_0")
(def fload_1 "fload_1")
(def fload_2 "fload_2")
(def fload_3 "fload_3")
(defn iload [index] ["iload" index])
(def iload_0 "iload_0")
(def iload_1 "iload_1")
(def iload_2 "iload_2")
(def iload_3 "iload_3")
(defn lload [index] ["lload" index])
(def lload_0 "lload_0")
(def lload_1 "lload_1")
(def lload_2 "lload_2")
(def lload_3 "lload_3")



;;push null
(def aconst_null "aconst_null")



;;pop object<reference>
;;push object<reference>
(defn checkcast [indexbyte1 indexbyte2]
  ["checkcast" indexbyte1 indexbyte2])
;;pop object<reference>
;;push value
(defn instanceof [indexbyte1 indexbyte2] ["instanceof" indexbyte1 indexbyte2])



(def d2f "d2f")
(def d2i "d2i")
(def d2l "d2l")
(def f2d "f2d")
(def f2i "f2i")
(def f2l "f2l")
(def i2b "i2b")
(def i2c "i2c")
(def i2d "i2d")
(def i2f "i2f")
(def i2l "i2l")
(def i2s "i2s")
(def l2d "l2d")
(def l2f "l2f")
(def l2i "l2i")



(def dadd "dadd")
(def fadd "fadd")
(def iadd "iadd")
(def ladd "ladd")
(def ddiv "ddiv")
(def fdib "fdiv")
(def idiv "idiv")
(def ldiv "ldiv")
(def dmul "dmul")
(def fmul "fmul")
(def imul "imul")
(def lmul "lmul")
(def dneg "dneg")
(def fneg "fneg")
(def drem "drem")
(def ineg "ineg")
(def lneg "lneg")
(def frem "frem")
(def irem "irem")
(def lrem "lrem")
(def dsub "dsub")
(def fsub "fsub")
(def isub "isub")
(def lsub "lsub")



(defn iinc [index const] ["iinc" index const])
(defn wide-sl [opcode indexbyte1 indexbyte2]
  ["wide" opcode indexbyte1 indexbyte2])
(defn wide-iinc [indexbyte1 indexbyte2 constbyte1 constbyte2]
  ["wide" "iinc" indexbyte1 indexbyte2 constbyte1 constbyte2])



(def ishl "ishl")
(def ishr "ishr")
(def iushr "iushr")
(def ixor "ixor")
(def lshl "lshl")
(def lshr "lshr")
(def lushr "lushr")
(def lxor "lxor")



(def iand "iand")
(def ior "ior")
(def land "land")
(def lor "lor")



(def dcmpg "dcmpg")
(def dcmpl "dcmpl")
(def fcmpg "fcmpg")
(def fcmpl "fcmpl")
(def lcmpl "lcmpl")
(def lcmpr "lcmpr")



;;push value
(def dconst_0 "dconst_0")
(def dconst_1 "dconst_1")
(def fconst_0 "fconst_0")
(def fconst_1 "fconst_1")
(def iconst_0 "iconst_0")
(def iconst_1 "iconst_1")
(def lconst_0 "lconst_0")
(def lconst_1 "lconst_1")



;;push from run-time constant pool
(defn ldc [index] ["ldc" index])
(defn ldc_2 [indexbyte1 indexbyte2] ["ldc_w" indexbyte1 indexbyte2])
(defn ldc2_w [indexbyte1 indexbyte2] ["ldc2_2" indexbyte1 indexbyte2])



;;duplicate top stack element
(def dup "dup")
(def dup_x1 "dup_x1")
(def dup_x2 "dup_x2")
(def dup2 "dup2")
(def dup2_x1 "dup2_x1")
(def dup2_x2 "dup2_x2")
(def pop "pop")
(def pop2 "pop2")
(defn sipush [byte1 byte2] ["sipush" byte1 byte2])
(def swap "swap")


  
;;pop and return
(def areturn "areturn")
(def ireturn "ireturn")
(def dreturn "dreturn")
(def freturn "freturn")
(def lreturn "lreturn")
(def return "return")
;;return to returnAddress at local variable 'index'
(defn ret [index] ["ret" index])



(defn add-comment [comment] (str \; comment))
(def nop "nop")