Jacket
===

Jacket, implementation of Lisp on JVM.
This is a pet project. It's goals:
* Learning JVM internals
* Practicing theory of compilation and interpretation
* Building yet another LISP implementation
* Doing something fun, big and ambitious

Also, have an idea to try to implement currying in s-expressions.

What's ready:
* Lexer with regular grammar described by the means of finite state machine. Batteries included (fsm).
* Parser.
* Semantic analysis.

Under development:
* Code generation. Polymorphic n-arity funcitons: println, + operator.
* Coming soon: print, readln, read functions; all arithmetic, comparison, logic operators

Not yet
* Code generation: conditionals, lists and operations, variables and scopes, lambda functions

Probably
* Macros
* S-expressions currying
* Optimizations

Not really serious about optimizations and production ready release. For now. :)