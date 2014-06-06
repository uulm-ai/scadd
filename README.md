scadd
=====

A Scala-based library for Decision Diagrams.

Decision Diagrams
-----------------
Decision Diagrams (DDs) represent functions from n Boolean or finite-domain variables to arbitrary values as directed acyclic graphs. Special cases are [Binary Decision Diagrams (BDDs)](http://en.wikipedia.org/wiki/Binary_decision_diagram) for Boolean-valued functions and Algebraic Decision Diagrams (ADDs) for number-valued functions. DDs are often compact, offer a unique representation for a given function, and support efficient application of binary operations for combining DDs, such as conjunction for BDDs or multiplication for ADDs.

Why Scadd?
----------
The design ideas behind scadd are
* to have an easy to use and read DD library that
* uses immutable data structures for representing DDs,
* is thread-safe,
* supports multiple variable orderings simultaneously,
* supports finite-domain variables,
* supports both ADDs and BDDs, and
* stictly separates API and diagram implementation.

There exist many implementations for DDs, many of which are probably faster than scadd (see, again, [here](http://en.wikipedia.org/wiki/Binary_decision_diagram), for a list of other implementations).

Scadd currently does not support automatic variable reordering. Implementing this remains future work. Manual variable reordering can be achieved by creating contexts with different variable orders and using `convertToContext`.

Since API and diagram implementation are separated in scadd, it would not be too difficult to add an implementation of, e.g., Sentential Decision Diagrams (SDDs) without changing the existing API.

Scadd usage example
-------------------
Here is an example for creating a variable, creating an ADD, and performing a few operations. Some more explanation is given below.

    import de.uniulm.dds.base._
    import de.uniulm.dds.leanimpl._
    implicit val context: Context[String, Double] = Context(Parameters[String, Double](lexicographicOrdering))
    val variable = Variable("w", Set("yes", "no", "maybe"))
    val constantOne = DecisionDiagram(1d)
    val oneForYes = DecisionDiagram(variable, Map("yes" -> 1d, "no" -> 0d, "maybe" -> 0d))
    val summed: DecisionDiagram[String, Double] = constantOne + oneForYes
    println(summed(Map(variable -> "yes"))) // prints 2.0
    println(summed eq DecisionDiagram(1d) / (DecisionDiagram(1d) / summed)) // prints true
    println(summed.restrict(Map(variable -> "yes")) eq DecisionDiagram(2d)) // prints true
    
Ignoring the `context` for the moment, variables and DDs are created and used as follows:
    
Variables
---------
Both Boolean and finite-domain variables are supported via the class `Variable[V]`, where `V` is the type of the elements in the domain of the variable, e.g. `Boolean`. `Variable("v")` creates a Boolean variable with the name `"v"`, whereas `Variable("w", Set("yes", "no", "maybe"))` creates a `String`-valued variable named `"w"` that can assume three values.

DDs
---
DDs are represented as `DecisionDiagram[V, T]`, where `V` is the variable domain type as above and `T` is the type the diagram maps to, e.g. `Boolean` for BDDs.

Diagrams are created and combined as follows:
* `DecisionDiagram(1d)` creates a diagram that represents the constant function that always returns 1.
* `DecisionDiagram(variable, Map("a" -> 1d, "b" -> 0d, "c" -> 0d)` creates a diagram that only depends on variable `variable` and maps `"a"` to 1, and so on.
* More complex diagrams (e.g., ones that depend on more than one variable) are created by combining simple diagrams created above using the method `def applyBinaryOperation(leafOperation: (T, T) => T, other: DecisionDiagram[V, T]): DecisionDiagram[V, T]` defined on `DecisionDiagram`. It takes an operation that combines the values the diagram maps to, e.g. `+` for numbers, so that `constantOne` and `oneForYes` can be added together using `constantOne.applyBinaryOperation(_ + _, oneForYes)`. Scadd offers some syntactic sugar for arithmetic operations like `+`, `*`, etc. for ADDs, so that the above can also simply be written as `constantOne + oneForYes`. Similarly, operations `&&` and so on are defined for Boolean-valued DDs.
* Analogously to binary operations, unary operations are supported via `def applyUnaryOperation(leafOperation: T => T): DecisionDiagram[V, T]`. Again, unary `-` and negation `!` are there for number-valued and Boolean-valued DDs, respectively.

Since DDs represent function, they can be evaluated just like other functions in Scala by supplying an assignment of their variables to values:

    println(summed(Map(variable -> "yes"))) // prints 2.0
    
DDs also offer a `restrict` operation that returns the diagram that results from assigning a certain value to some variables, akin to currying:

    println(summed.restrict(Map(variable -> "yes")) eq DecisionDiagram(2d)) // prints true
    
Finally, DDs offer a `transform` method that recursively transforms a diagram bottom up: `def transform[K](leafTransformation: T => K, innerNodeTransformation: (Variable[V], Map[V, K]) => K): K`. E.g., `DecisionDiagram.toString` is implemented as a transformation:

    override def toString(): String = {
        def innerNodeTransformation(variable: Variable[V], map: Map[V, String]): String = "(" + variable + map.foldLeft("")((intermediate, tuple) => intermediate + "\n(" + tuple._1 + " " + tuple._2 + ")").replace("\n", "\n\t") + ")"
        transform("(" + _ + ")", innerNodeTransformation)
    }
    
So that `println(summed.toString)` gives

    (w
    	(yes (2.0))
    	(no (1.0))
    	(maybe (1.0)))


Contexts
--------
DDs are only unique for a given variable ordering. `Context[V, T]` maintains this ordering and takes care of ensuring diagram uniqueness. `Context[V, T]` offers methods for creating DDs. However, as seen above, these are usually not used directly and someting like `DecisionDiagram(1d)` is used instead. Calling `DecisionDiagram(1d)` requires an `implicit` context to be defined. Scadd is designed like this because it is often satisfactory to only work with a single context. It is therefore convenient to declare the context using an `implicit val` and then using `DecisionDiagram(1d)` and the like:

    implicit val context: Context[String, Double] = Context(Parameters[String, Double](lexicographicOrdering))
    
Once an implicit context is defined, `DecisionDiagram(1d)` implicitly uses `context` to create the diagram. In particular, this means that type parameters transfer to the created DDs, i.e. `DecisionDiagram(1d)` will be a `DecisionDiagram[String, Double]`.
    
Scadd offers two separate implementations for DDs and their contexts. The above example uses `de.uniulm.dds.leanimpl._`, the alternative is `de.uniulm.dds.defaultimpl._`. Both offer the same functionality, but `leanimpl` uses less memory.

Variable ordering is given by a variable comparator `Comparator[Variable[V]]` that must be consistent with equals. Predefined orderings are `listbasedOrdering` and `lexicographicOrdering` defined in the `de.uniulm.dds.base` package object.

Caching
-------
Scadd uses [guava](https://code.google.com/p/guava-libraries/) for caching intermediate results of applying unary and binary operations and restricting. Cache sizes can be configured upon `Context` creation. Default values are supplied, these are very generous, however.

Adding scadd to your sbt project
--------------------------------
To use the library, add the following lines to your `.sbt` file:

    resolvers += "fmueller repository" at "http://companion.informatik.uni-ulm.de/~fmueller/mvn"
    
    libraryDependencies += "de.uni-ulm" % "scadd_2.11" % "1.12"

