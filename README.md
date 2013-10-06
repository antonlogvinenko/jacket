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
    * if-expressions
    * cond-expressions
* Lists and list operations: list, cons, get, set
* Variables
    * local definitions with let
    * global definitions with define
    * variable evaluation
* Functions
    * [First-class](https://github.com/antonlogvinenko/jacket/blob/master/test-programs/closure-first-class.jt)
    * [Anonymous](https://github.com/antonlogvinenko/jacket/blob/master/test-programs/closure-single-arg.jt)
    * [Closures](https://github.com/antonlogvinenko/jacket/blob/master/test-programs/closure-closed.jt)
    * [Partially applied](https://github.com/antonlogvinenko/jacket/blob/master/test-programs/closure-currying.jt)
* Java interop
    * Objects intantiation [(class. args)](https://github.com/antonlogvinenko/jacket/blob/master/test-programs/interop-instantiate.jt)
    * Static fields access: (println java.lang.Short/MAX_VALUE)
    * Static methods invokation: (java.lang.Double/valueOf "42E1")
    * Instance fields access: (.FALSE #t)
    * Instance methods invokation: (.after (java.util.Date. 1) (java.util.Date. 0))
    * Shortcut syntax for all above: (. x method args), (. x field)
* Macros - in progress
    * lists correct support
    * `, ~, ~@, lists correct support
    * variable names generation

    * Ignore errors of macroexpansion (circular dependences)
    * What about macros using macros? Exclude from expansion or preexpand them?
    
    * partially applied macros? O_O

Next episodes:
* Web UI in ClojureScript (oct 5, 6; oct 12, 13)
* Tail recursion macro support (oct 19, 20)
* Performance tests (oct 26, 27)

Ideas:
* Implement (optional) type system (defn-t determinant [m] [Matrix -> Number] (...))
* Green threads scheduler, actor model implementation (option for lmax disruptor scheduling mode)