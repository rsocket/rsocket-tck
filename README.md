# Reactive Socket TCK

The goal of this project is to provide a reliable polyglot testing suite to verify both existing and upcoming versions of servers and clients that use the Reactive Sockets protocol.

## Outline

The challenge of creating any test suite for network protocols is that we must have a wait for one side to behave according to how we expect, so we can check that the appropriate outputs are received given a certain input.
Another challenge is that the tests must be polyglot (support multiple languages), as this network protocol can, and will be, implemented in various languages.
We accomplish these challenges by defining a Domain Specific Language (DSL) using Scala, which generates an intermediate script that potential testers can then write drivers for that will drive their existing application.
We have a DSL for generating client tests as well as a DSL that can specify server behavior. The intermediate script generated is human readable and can be parsed easily line by line, but allows for powerful functionality.
This allows implementers of the Reactive Socket protocol to be able to write their own drivers without too much trouble.

## Requester DSL

The DSLs are designed to be human readable as well, and should require very little documentation to understand. Here is an example of the client side DSL
```
val s1 = requestResponse("a", "b")
    s1 request 1
    val s2 = requestResponse("c", "d")
    s1 awaitTerminal()
    s1 assertCompleted()
    s1 assertNoErrors()
    s2 request 1
    val s3 = requestResponse("e", "f")
    s2 awaitTerminal()
    s2 assertCompleted()
    s3 request 1
    s2 assertNoErrors()
    s1 assertReceived List(("hello", "goodbye"))
    s2 assertReceived List(("ding", "dong"))
    s3 awaitTerminal()
    s3 assertError()
```
In the above example, we create a requestResponse subscriber and subscribe it to a trivial publisher with initial payload data "a" and metadata "b". We then call request on it, and assert the behaviors we expect.
When we assert received, since it is a requestResponse, we only expect a list of 1 payload, and we assert both the data and metadata received. In this example, we are testing interleaving 3 requestResponse subscribers.

## Responder DSL
The responder DSL example is the dual to the above requester DSL.
```
requestResponse handle("a", "b") using(Map("x" -> ("hello", "goodbye")), pause(3), emit('x'),
      pause(4), pause(5), complete)
requestResponse handle("c", "d") using(Map("x" -> ("ding", "dong")), pause(10), emit('x'),
      pause(10), complete)
requestResponse handle("e", "f") using(pause(10), error)
end
```
Here, we write that we want to create requestResponse handlers that handle some initial payload. The optional map argument allows testers to map data and metadata to characters. Under the hood, this is using almost exactly
the same syntax as the marble diagrams in the rxjs project [here](https://github.com/ReactiveX/rxjs/blob/master/doc/writing-marble-tests.md#marble-syntax). Users can also directly write the marble diagram into the test cases
instead of using the programatic notion we have here, so one can write something like `requestResponse handle("x", "y") using ("---a---|")`.

## Run Instructions
This project is managed with sbt. Simply navigate to the root directory with build.sbt and run `sbt assembly`. You can then use `./run <scriptfile>` to run the server or client with a specific script file.

## TODO
We are still working on getting tests for channel set up, and will also reorganize the project in the future. Please feel free to contact us with any suggestions.
