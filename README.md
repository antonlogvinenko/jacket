jacket
===

Jacket, implementation of Lisp (probably in future - Racket) on JVM.
Pet project pursuing several goals:
* Learning JVM internals
* Practicing theory of compilation and interpretation
* Building yet another LISP implementation
* Doing something fun, big and ambitious
* - in that order

Have an idea to try to implement currying in s-expressions.

Clojure:
>(def f (partial conj [3]))

>(f 1)

>[3 1]

Jacket:
>(def f (conj [3]))

>(f 1)

>[3 1]

"partial" in Jacket will have to have a "greedy" notion attached, for making n-arity functions partial application.

Current state:
* Lexer with regular grammar described by the means of finite state machine. Batteries included (fsm). Complete.
* Parser. Complete.
* Semantic analysis. Complete.
* Code generation. In progress, "jacket-2" branch.
* Optimizations. Probably.

Not really serious about optimizations and production ready release. For now. :)