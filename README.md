Jacket
===

Jacket, implementation of Lisp on the JVM.
This is a pet project. It's goals:
* Learning JVM internals
* Practicing theory of compilation and interpretation
* Building yet another LISP implementation
* Doing something fun, big and ambitious

Also, have an idea to try to implement currying in s-expressions.

What's ready:
* Lexer with regular grammar recognized by a finite state machine. Batteries included (FSM).
* Parser.
* Semantic analysis.
* Code generation: polymorphic n-arity funcitons: println, print, readln; +, -, *, / arithmetics

Under development:
* Coming very soon: all comparison, logic operators

Not yet
* Code generation: conditionals, lists and operations, variables and scopes, lambda functions

Probably
* Macros
* S-expressions currying
* Java interop
* Optimizations