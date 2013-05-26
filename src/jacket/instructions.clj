(ns jacket.instructions)


;;This is the coolest definition in this file.
(defmacro def-n [& args]
  `(do ~@(->> args
              (partition 2)
              (map (partial cons 'def)))))

(defmacro defn-n [& args]
  `(do ~@(->> args
              (partition 3)
              (map (partial cons 'defn)))))



;;pop object<reference>
;;push value
(defn getfield [idxbyte1 idxbyte2] ["getfield" idxbyte1 idxbyte2])
(defn getstatic [idxbyte1 idxbyte2] ["getstatic" idxbyte1 idxbyte2])
;;pop object<reference>, value
(defn putfield [idxbyte1 idxbyte2] ["putfield" idxbyte1 idxbyte2])
;;pop value
(defn putstatic [idxbyte1 idxbyte2] ["putstatic" idxbyte1 idxbyte2])



;;pop arg1[, arg2[, ...]]
(defn inbokedynamic [idxbyte1 idxbyte2]
  ["invokedynamic" idxbyte1 idxbyte2 0 0])
;;pop object<reference>, arg1[, arg2[, ...]]
(defn invokeinterface [idxbyte1 idxbyte2 count]
  ["invokeinterface" idxbyte1 idxbyte2 count])
;;pop object<reference>, arg1[, arg2[, ...]]
;;pop arg1[, arg2[, ...]]
(defn invokestatic [idxbyte1 idxbyte2]
  ["invokestatic" idxbyte1 idxbyte2])
;;pop object<reference>, arg1[, arg2[, ...]]
(defn invokevirtual [idxbyte1 idxbyte2]
  ["inbokevirtual" idxbyte1 idxbyte2])
(defn invokespecial [idxbyte1 idxbyte2]
  ["invokespecial" idxbyte1 idxbyte2])



;;pop object<reference>
(def-n monitorenter "monitorenter", monitorexit "monitorexit")



(defn goto [brbyte1 brbyte2] ["goto" brbyte1 brbyte2])
(defn goto_2 [brbyte1 brbyte2 brbyte3 brbyte4]
  ["goto_2" brbyte1 brbyte2 brbyte3 brbyte4])
;;pop object<type>, object<type>
(defn-n
  if_acmpeq [brbyte1 brbyte2] ["if_acmpeq" brbyte1 brbyte2]
  if_acmpne [brbyte1 brbyte2] ["if_acmpne" brbyte1 brbyte2]
  if_icmpeq [brbyte1 brbyte2] ["if_icmpeq" brbyte1 brbyte2]
  if_icmpne [brbyte1 brbyte2] ["if_icmpne" brbyte1 brbyte2]
  if_icmplt [brbyte1 brbyte2] ["if_icmplt" brbyte1 brbyte2]
  if_icmple [brbyte1 brbyte2] ["if_icmple" brbyte1 brbyte2]
  if_icmpgt [brbyte1 brbyte2] ["if_icmpgt" brbyte1 brbyte2]
  if_icmpge [brbyte1 brbyte2] ["if_icmpge" brbyte1 brbyte2])
;;pop value
(defn-n
  ifeq [brbyte1 brbyte2] ["ifeq" brbyte1 brbyte2]
  ifne [brbyte1 brbyte2] ["ifne" brbyte1 brbyte2]
  iflt [brbyte1 brbyte2] ["iflt" brbyte1 brbyte2]
  ifgt [brbyte1 brbyte2] ["ifgt" brbyte1 brbyte2]
  ifle [brbyte1 brbyte2] ["ifle" brbyte1 brbyte2]
  ifge [brbyte1 brbyte2] ["ifge" brbyte1 brbyte2])
;;pop value<reference>
(defn-n
  ifnonnull [brbyte1 brbyte2] ["ifnonnull" brbyte1 brbyte2]
  ifnull [brbyte1 brbyte2] ["ifnull" brbyte1 brbyte2])
;;push returnAddress
(defn-n
  jsr [brbyte1 brbyte2] ["jsr" brbyte1 brbyte2]
  jsr [brbyte1 brbyte2 brbyte3 brbyte4]
  ["jsr" brbyte1 brbyte2 brbyte3 brbyte4])
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
;;idx in constant pool refers to a class/interface/array type
(defn anewarray [idxbyte1 idxbyte2] ["anewarray" idxbyte1 idxbyte2])
;;pop count<int>
;;push array<reference>
;;bool-4, char-5, float-6, double-7, byte-8, short-9, int-10, long-11
(defn newarray [atype] ["newarray" atype])
;;push object<reference>
;;idx in constant pool refers to a class/interface type
(defn new [idxbyte1 idxbyte2] ["new" idxbyte1 idxbyte2])
;;pop count1, count2, ...
;;push array<reference>
(defn multinewarray [idxbyte1 idxbyte2 dimensions]
  ["multinewarray" idxbyte1 idxbyte2 dimensions])
;;pop array<ref>
;;push length<int>
(def arraylength "arraylength")



;;pop arrayref<reference>, idx<int>
;;push value<op-type>
(def-n
  aaload "aaload" baload "baload" caload "caload" daload "daload"
  faload "faload" iaload "iaload" laload "laload" saload "saload")
;;pop arrayref<reference>, idx<int>, value<op-type>
;;put value<op-type>, value type must be compatible
(def-n
  aastore "aastore" bastore "bastore" castore "castore" dastore "dastore"
  fastore "fastore" iastore "iastore" lastore "lastore" sastore "sastore")



(defn-n
  astore [idx] ["astore" idx]
  dstore [idx] ["dstore" idx]
  fstore [idx] ["fstore" idx]
  istore [idx] ["istore" idx]
  lstore [idx] ["lstore" idx])
(def-n
  dstore_0 "dstore_0" dstore_1 "dstore_1" dstore_2 "dstore_2" dstore_3 "dstore_3"
  astore_0 "astore_0" astore_1 "astore_1" astore_2 "astore_2" astore_3 "astore_3"
  fstore_0 "fstore_0" fstore_1 "fstore_1" fstore_2 "fstore_2" fstore_3 "fstore_3"
  istore_0 "istore_0" istore_1 "istore_1" istore_2 "istore_2" istore_3 "istore_3"
  lstore_0 "lstore_0" lstore_1 "lstore_1" lstore_2 "lstore_2" lstore_3 "lstore_3")

(defn-n
  dload [idx] ["dload" idx]
  fload [idx] ["fload" idx]
  iload [idx] ["iload" idx]
  lload [idx] ["lload" idx]
  aload [idx] ["aload" idx])
(def-n
  aload_0 "aload_0" aload_1 "aload_1" aload_2 "aload_2" aload_3 "adlod_3"
  dload_0 "dload_0" dload_1 "dload_1" dload_2 "dload_2" dload_3 "dload_3"
  fload_0 "fload_0" fload_1 "fload_1" fload_2 "fload_2" fload_3 "fload_3"
  iload_0 "iload_0" iload_1 "iload_1" iload_2 "iload_2" iload_3 "iload_3"
  lload_0 "lload_0" lload_1 "lload_1" lload_2 "lload_2" lload_3 "lload_3")



;;push null
(def aconst_null "aconst_null")



;;pop object<reference>
;;push object<reference>
(defn checkcast [idxbyte1 idxbyte2]
  ["checkcast" idxbyte1 idxbyte2])
;;pop object<reference>
;;push value
(defn instanceof [idxbyte1 idxbyte2] ["instanceof" idxbyte1 idxbyte2])



(def-n
  d2f "d2f" d2i "d2i" d2l "d2l"
  f2d "f2d" f2i "f2i" f2l "f2l"
  i2b "i2b" i2c "i2c" i2d "i2d" i2f "i2f" i2l "i2l" i2s "i2s"
  l2d "l2d" l2f "l2f" l2i "l2i")



(def-n
  dadd "dadd" fadd "fadd" iadd "iadd" ladd "ladd"
  ddiv "ddiv" fdib "fdiv" idiv "idiv" ldiv "ldiv"
  dmul "dmul" fmul "fmul" imul "imul" lmul "lmul"
  dneg "dneg" fneg "fneg" ineg "ineg" lneg "lneg"
  frem "frem" irem "irem" lrem "lrem" drem "drem"
  dsub "dsub" fsub "fsub" isub "isub" lsub "lsub")



(defn iinc [idx const] ["iinc" idx const])
(defn-n
  wide-sl [opcode idxbyte1 idxbyte2] ["wide" opcode idxbyte1 idxbyte2]
  wide-iinc [idxbyte1 idxbyte2 constbyte1 constbyte2]
  ["wide" "iinc" idxbyte1 idxbyte2 constbyte1 constbyte2])



(def-n ishl "ishl" lshl "lshl"
  ishr "ishr" lshr "lshr"
  iushr "iushr" lushr "lushr"
  lxor "lxor" ixor "ixor")



(def-n iand "iand" land "land" ior "ior" lor "lor")



(def-n
  dcmpg "dcmpg" fcmpg "fcmpg"
  dcmpl "dcmpl" fcmpl "fcmpl"
  lcmp "lcmp")



;;push value
(def-n
  dconst_0 "dconst_0" dconst_1 "dconst_1"
  fconst_0 "fconst_0" fconst_1 "fconst_1"
  iconst_0 "iconst_0" iconst_1 "iconst_1"
  lconst_0 "lconst_0" lconst_1 "lconst_1")



;;push from run-time constant pool
(defn-n
  ldc [idx] ["ldc" idx]
  ldc_2 [idxbyte1 idxbyte2] ["ldc_w" idxbyte1 idxbyte2]
  ldc2_w [idxbyte1 idxbyte2] ["ldc2_2" idxbyte1 idxbyte2])



;;duplicate top stack element
(def-n
  dup "dup" dup_x1 "dup_x1" dup_x2 "dup_x2"
  dup2 "dup2" dup2_x1 "dup2_x1" dup2_x2 "dup2_x2"
  pop1 "pop" pop2 "pop2"
  swap "swap")
(defn sipush [byte1 byte2] ["sipush" byte1 byte2])


  
;;pop and return
(def-n
  return "return" areturn "areturn"
  ireturn "ireturn" dreturn "dreturn" freturn "freturn" lreturn "lreturn")
;;return to returnAddress at local variable 'idx'
(defn ret [idx] ["ret" idx])



(defn add-comment [comment] (str \; comment))
(def nop "nop")