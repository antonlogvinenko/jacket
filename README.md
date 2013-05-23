jacket
===

Jacket, implementation of Lisp (probably in future - Racket) on JVM.
Pet project pursuing clear goals:
* Learning JVM internals
* Practicing theory of compilation and interpretation
* Building yeat another lisp implementation
* Doing something fun, big and ambitious
* - in that order

Current state:
* Lexer with regular grammar described by the means of finite state machine. Batteries (fsm) included. Ready.
* Parser. Ready.
* Semantic analysis. Ready.
* Code generation. In progress, "jacket-2" branch.
* Optimizations. Probably.

Not really serious about optimizations and production ready release. For now. :)