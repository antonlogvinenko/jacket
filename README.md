Jacket
===

Jacket, implementation of Lisp on the JVM.

With this project I mean to:
* Learn JVM internals
* Practice theory of compilation and interpretation
* Build yet another LISP implementation
* Implement partial application with s-expressions
* Have fun after all

Progress:
* Lexer with regular grammar recognized by a finite state machine. Batteries included (FSM)
* Parser
* Semantic analysis
* Basic language constructs: readln; println, print, arithmetics (+, -, *, /), logical operations (and, or, xor, not), comparison operations(<, <=, >, >=, =, !=). All operations are polymorphic, n-arity functions meaning "(<= 100 101 200)", "(= 100 100 100)" and "(and (not true) true false)" are legal expressions.
* Conditionals: if-expressions, cond-expressions
* Lists and list operations: list, cons, get, set
* Local variables scopes, let-expressions, variables evaluation
* Global variables
* First-class partially applied anonymous functions

Under development:
* Lexical closures

Next episodes:
* Macros
* Tail recursions support
* Java interop
* REPL with a simple Swing GUI and automatic github errors reports
