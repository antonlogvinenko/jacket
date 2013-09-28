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
* Language basics, n-arity polymorphic functions
    * readln, println, print
    * arithmetics (+, -, *, /)
    * logical operations (and, or, xor, not)
    * comparison operations (<, <=, >, >=, =, !=)
* Conditionals
    * if-expressions
    * cond-expressions
* Lists and list operations: list, cons, get, set
* Variables
    * local definitions with let
    * global definitions with define
    * variable evaluation
* Functions
    * First-class
    * Anonymous
    * Closures
    * Partially applied
* Java interop
    * Objects intantiation (class. args)
    * Static fields access: (println java.lang.Short/MAX_VALUE)
    * Static methods invokation: (java.lang.Double/valueOf "42E1")
    * Instance fields access: (.FALSE #t)
    * Instance methods invokation: (.after (java.util.Date. 1) (java.util.Date. 0))
    * Shortcut syntax for all above: (. x method args), (. x field)
* Macros - in progress
    * macros do not call functions
	* START: compile all (something uses macro - don't compile it, report ast position)
	* if nothing uses macro (prev step reported nothing) then we're DONE, all code compiled
	* on reported pieces invoke macroexpansions, supress errors (not compiled macro)
	* start from the beginning with the new AST

        * lists correct support
	* remove old code

    * `, ~, ~@, lists correct support
    * variable names generation

    * partially applied macros? O_O

Next episodes:
* Web UI in ClojureScript (oct 5, 6; oct 12, 13)
* Tail recursion macro support (oct 19, 20)
* Performance tests (oct 26, 27)

Ideas:
* Implement (optional) type system (defn-t determinant [m] [Matrix -> Number] (...))
* Green threads scheduler, actor model implementation (option for lmax disruptor scheduling mode)