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
* Lexer with regular grammar recognized by a finite state machine. Batteries included (FSM)
* Parser
* Semantic analysis
* Basic language constructs: readln; println, print, arithmetics (+, -, *, /), logical operations (and, or, xor, not), comparison operations(<, <=, >, >=, =, !=). All operations are polymorphic, n-arity functions meaning "(<= 100 101 200)", "(= 100 100 100)" and "(and (not true) true false)" are legal expressions.
* Conditionals: if- and cond-expressions
* Lists and list operations: list, cons, get, set
* Local variables scopes, let-expressions, variables evaluation
* Global variables
* First-class partially applied closures
* Java interop:  objects intantiation (class. args),  fields and methods access: (.FALSE #t), (.after (java.util.Date. 1) (java.util.Date. 0)); static methods access (java.lang.Double/valueOf "42E1"), static fields access (println java.lang.Short/MAX_VALUE); invokation (. x method args), field access (. x field).
* Macros - in progress
    * new keywords: defmacro, quote ~, ~@
    * new compile phase: preprocessor
    * working quote ~ ~@
    * working preprocessor
    * ' keyword
    * variable names generation
    * ?

Next episodes:
* Web UI in ClojureScript (oct 5, 6; oct 12, 13)
* Tail recursion macro support (oct 19, 20)
* Performance tests (oct 26, 27)

Ideas:
* Implement (optional) type system (defn-t determinant [m] [Matrix -> Number] (...))
* Green threads scheduler, actor model implementation (option for lmax disruptor scheduling mode)