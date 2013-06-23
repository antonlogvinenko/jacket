Jacket
===

Jacket, implementation of Lisp on the JVM.
This is a pet project in the way a dinosaur could be a pet. It's goals:
* Learning JVM internals
* Practicing theory of compilation and interpretation
* Building yet another LISP implementation
* Having fun

Also, have an idea to try to implement currying in s-expressions.

What's ready:
* Lexer with regular grammar recognized by a finite state machine. Batteries included (FSM).
* Parser.
* Semantic analysis.
* Code generation: readln; println, print, arithmetics (+, -, *, /), logical operations (and, or, xor, not), comparison operations(<, <=, >, >=, =, !=). All operations are polymorphic, n-arity functions meaning "(<= 100 101 200)", "(= 100 100 100)" and "(and (not true) true false)" are legal expressions.
* Conditionals: if-expressions, cond-expressions.

Under development:
* lists and operations (cons, car, cdr)

Not yet
* variables and scopes (let, define)
* lambda functions

Probably
* Macros
* S-expressions currying
* Tail recursions support
* Java interop
* REPL with a simple Swing GUI and errors reporting straight to github
