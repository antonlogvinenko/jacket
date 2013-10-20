Jacket
===

Jacket, implementation of Lisp on the JVM.

With this project I mean to:
* Employ JVM runtime system
* Practice theory of compilation
* Build yet another LISP implementation
* Implement partial application with s-expressions
* Have fun

Progress:
* [Lexer with regular grammar](https://github.com/antonlogvinenko/jacket/blob/master/src/jacket/lexer/lexer.clj) recognized by a finite state machine. [Batteries included (FSM)](https://github.com/antonlogvinenko/jacket/blob/master/src/jacket/lexer/fsm.clj)
* Parser
* Semantic analysis
* Language basics, n-arity polymorphic functions
    * [readln](https://github.com/antonlogvinenko/jacket/blob/master/test-programs/readln.jt), [println](https://github.com/antonlogvinenko/jacket/blob/master/test-programs/println.jt), [print](https://github.com/antonlogvinenko/jacket/blob/master/test-programs/print.jt)
    * arithmetics ([sum +](https://github.com/antonlogvinenko/jacket/blob/master/test-programs/println-sum-n.jt), [substraction -](https://github.com/antonlogvinenko/jacket/blob/master/test-programs/println-sub-n.jt), [multiplication *](https://github.com/antonlogvinenko/jacket/blob/master/test-programs/println-mul-n.jt), [division /](https://github.com/antonlogvinenko/jacket/blob/master/test-programs/println-div-n.jt))
    * logical operations ([and](https://github.com/antonlogvinenko/jacket/blob/master/test-programs/bool-and-n-arity.jt), [or](https://github.com/antonlogvinenko/jacket/blob/master/test-programs/bool-or.jt), [xor](https://github.com/antonlogvinenko/jacket/blob/master/test-programs/bool-xor.jt), [not](https://github.com/antonlogvinenko/jacket/blob/master/test-programs/bool-not.jt))
    * comparison operations ([less <](https://github.com/antonlogvinenko/jacket/blob/master/test-programs/comp-less.jt), [less or equal <=](https://github.com/antonlogvinenko/jacket/blob/master/test-programs/comp-less-or-equal.jt), [greater >](https://github.com/antonlogvinenko/jacket/blob/master/test-programs/comp-greater.jt), [greater or equal >=](https://github.com/antonlogvinenko/jacket/blob/master/test-programs/comp-greater-or-equal.jt), [equal =](https://github.com/antonlogvinenko/jacket/blob/master/test-programs/comp-equal.jt), [not equal !=](https://github.com/antonlogvinenko/jacket/blob/master/test-programs/comp-nequal.jt))
* Conditionals
    * [if-expressions](https://github.com/antonlogvinenko/jacket/blob/master/test-programs/cond-if-false.jt)
    * [cond-expressions](https://github.com/antonlogvinenko/jacket/blob/master/test-programs/cond-cond.jt)
* Lists and list operations
    * [list](https://github.com/antonlogvinenko/jacket/blob/master/test-programs/list-list-args.jt)
    * [cons](https://github.com/antonlogvinenko/jacket/blob/master/test-programs/list-cons.jt)
    * [get](https://github.com/antonlogvinenko/jacket/blob/master/test-programs/list-get.jt)
    * [set](https://github.com/antonlogvinenko/jacket/blob/master/test-programs/list-set.jt)
* Variables
    * [local definitions with let](https://github.com/antonlogvinenko/jacket/blob/master/test-programs/let-cool-one.jt)
    * [global definitions with define](https://github.com/antonlogvinenko/jacket/blob/master/test-programs/define.jt)
    * [variable evaluation](https://github.com/antonlogvinenko/jacket/blob/master/test-programs/define-big.jt)
* Functions
    * [First-class](https://github.com/antonlogvinenko/jacket/blob/master/test-programs/closure-first-class.jt)
    * [Anonymous](https://github.com/antonlogvinenko/jacket/blob/master/test-programs/closure-single-arg.jt)
    * [Closures](https://github.com/antonlogvinenko/jacket/blob/master/test-programs/closure-closed.jt)
    * [Partially applied](https://github.com/antonlogvinenko/jacket/blob/master/test-programs/closure-currying.jt)
* Java interop
    * Objects intantiation [(class. args)](https://github.com/antonlogvinenko/jacket/blob/master/test-programs/interop-instantiate.jt)
    * Static fields access: [(println java.lang.Short/MAX_VALUE)](https://github.com/antonlogvinenko/jacket/blob/master/test-programs/interop-static-field.jt)
    * Static methods invokation: [(java.lang.Double/valueOf "42E1")](https://github.com/antonlogvinenko/jacket/blob/master/test-programs/interop-static-method.jt)
    * Instance fields access: [(.FALSE #t)](https://github.com/antonlogvinenko/jacket/blob/master/test-programs/interop-instance-get-field.jt)
    * Instance methods invokation: [(.after (java.util.Date. 1) (java.util.Date. 0))](https://github.com/antonlogvinenko/jacket/blob/master/test-programs/interop-instance-invoke-method.jt)
    * Shortcut syntax for all above: [(. x method args), (. x field)](https://github.com/antonlogvinenko/jacket/blob/master/test-programs/interop-instance-static-field-method.jt)
* Macros - in progress
    * [The simplest identity macro](https://github.com/antonlogvinenko/jacket/blob/master/test-programs/macro-definition.jt)
    * [The simplest macro processing s-expression](https://github.com/antonlogvinenko/jacket/blob/master/test-programs/macro-sexpr.jt)
    * [Using list operations to modify s-expressions](https://github.com/antonlogvinenko/jacket/blob/master/test-programs/macro-list.jt)
    * Quote operation
        * [lists](https://github.com/antonlogvinenko/jacket/blob/master/test-programs/macro-quoted-list.jt)
        * [keywords](https://github.com/antonlogvinenko/jacket/blob/master/test-programs/macro-quoted-keyword.jt)
        * [symbols](https://github.com/antonlogvinenko/jacket/blob/master/test-programs/macro-quoted-symbol.jt)
        * [atoms](https://github.com/antonlogvinenko/jacket/blob/master/test-programs/macro-quoted-atom.jt)
    * [Backtick, unquote operations](https://github.com/antonlogvinenko/jacket/blob/master/test-programs/macro-backtick-unquoted.jt)
    
    * [Uquote-splicing](https://github.com/antonlogvinenko/jacket/blob/master/test-programs/macro-backtick-unquote-splicing.jt)
    * variable names generation - jacket-50

    * errors of macroexpansion + circular dependences - jacket-51
    * Check macros using macros - jacket-52

    * Partially applied macros? X_X or O_O?
    
    * macro system - remove "defmacro f" as "define f (lambda...", add Token info, don't add Token. after expansion
* Web UI in ClojureScript

Probably:
* eval, REPL?
* Tail recursion support
* Performance testing
* Small bugfixes

Ideas:
* Namespaces?
* Implement (optional) type system (defn-t determinant [m] [Matrix -> Number] (...))
* Green threads scheduler, actor model implementation (option for lmax disruptor scheduling mode)