(ns jacket.instructions)


;;This is the coolest definition in this file.
(defmacro def-n [& args]
  `(do ~@(->> args
              (partition 2)
              (map (partial cons 'def)))))



;;pop object<reference>
;;push value
(defn getfield [indexbyte1 indexbyte2] ["getfield" indexbyte1 indexbyte2])
(defn getstatic [indexbyte1 indexbyte2] ["getstatic" indexbyte1 indexbyte2])
;;pop object<reference>, value
(defn putfield [indexbyte1 indexbyte2] ["putfield" indexbyte1 indexbyte2])
;;pop value
(defn putstatic [indexbyte1 indexbyte2] ["putstatic" indexbyte1 indexbyte2])



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
(def-n monitorenter "monitorenter", monitorexit "monitorexit")



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
  aaload "aaload" baload "baload" caload "caload" daload "daload"
  faload "faload" iaload "iaload" laload "laload" saload "saload")
;;pop arrayref<reference>, index<int>, value<op-type>
;;put value<op-type>, value type must be compatible
(def-n
  aastore "aastore" bastore "bastore" castore "castore" dastore "dastore"
  fastore "fastore" iastore "iastore" lastore "lastore" sastore "sastore")



(defn astore [index] ["astore" index])
(defn dstore [index] ["dstore" index])
(defn fstore [index] ["fstore" index])
(defn istore [index] ["istore" index])
(defn lstore [index] ["lstore" index])
(def-n
  dstore_0 "dstore_0" dstore_1 "dstore_1" dstore_2 "dstore_2" dstore_3 "dstore_3"
  astore_0 "astore_0" astore_1 "astore_1" astore_2 "astore_2" astore_3 "astore_3"
  fstore_0 "fstore_0" fstore_1 "fstore_1" fstore_2 "fstore_2" fstore_3 "fstore_3"
  istore_0 "istore_0" istore_1 "istore_1" istore_2 "istore_2" istore_3 "istore_3"
  lstore_0 "lstore_0" lstore_1 "lstore_1" lstore_2 "lstore_2" lstore_3 "lstore_3")

(defn dload [index] ["dload" index])
(defn fload [index] ["fload" index])
(defn iload [index] ["iload" index])
(defn lload [index] ["lload" index])
(defn aload [index] ["aload" index])
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
(defn checkcast [indexbyte1 indexbyte2]
  ["checkcast" indexbyte1 indexbyte2])
;;pop object<reference>
;;push value
(defn instanceof [indexbyte1 indexbyte2] ["instanceof" indexbyte1 indexbyte2])



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



(defn iinc [index const] ["iinc" index const])
(defn wide-sl [opcode indexbyte1 indexbyte2]
  ["wide" opcode indexbyte1 indexbyte2])
(defn wide-iinc [indexbyte1 indexbyte2 constbyte1 constbyte2]
  ["wide" "iinc" indexbyte1 indexbyte2 constbyte1 constbyte2])



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
(defn ldc [index] ["ldc" index])
(defn ldc_2 [indexbyte1 indexbyte2] ["ldc_w" indexbyte1 indexbyte2])
(defn ldc2_w [indexbyte1 indexbyte2] ["ldc2_2" indexbyte1 indexbyte2])



;;duplicate top stack element
(def-n
  dup "dup" dup_x1 "dup_x1" dup_x2 "dup_x2"
  dup2 "dup2" dup2_x1 "dup2_x1" dup2_x2 "dup2_x2"
  pop "pop" pop2 "pop2"
  swap "swap")
(defn sipush [byte1 byte2] ["sipush" byte1 byte2])


  
;;pop and return
(def-n
  return "return" areturn "areturn"
  ireturn "ireturn" dreturn "dreturn" freturn "freturn" lreturn "lreturn")
;;return to returnAddress at local variable 'index'
(defn ret [index] ["ret" index])



(defn add-comment [comment] (str \; comment))
(def nop "nop")