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

Next episodes:
* Java interop: instantiation, static/instance fields/methods access
* Macros
* Tail recursion macro support
* Web UI in ClojureScript
* Performance tests
