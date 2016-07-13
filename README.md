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
  def main(args: Array[String]) {
    begintest(test0)
    begintest(test4)
    end
  }

  // example for testing channel
  def test0() : Unit = {
    // using determines the initial payload sent
    requestChannel using("a", "b") asFollows(() => {
      respond("-a-")
      val s1 = channelSubscriber
      s1 request 1
      respond("-b-c-d-e-f-")
      s1 awaitAtLeast(1, 2000)
      s1 assertReceivedAtLeast 1
      s1 assertReceived List(("x", "x"))
      s1 request 2
      respond("-g-h-i-j-k-")
      s1 awaitAtLeast(4, 2000)
      s1 request 4
      s1 awaitAtLeast(7, 1000)
      respond("|")
      s1 awaitTerminal()
      s1 assertCompleted()
    })
  }

....

  // example for testing stream
  def test4() : Unit = {
    nametest("test4")
    val s1 = requestStream("a", "b")
    s1 request 3
    val s2 = requestStream("c", "d")
    s1 awaitAtLeast(3, 2000)
    s2 request 1
    s1 assertReceived(List(("a", "b"), ("c", "d"), ("e", "f")))
    s1 request 3
    s1 awaitTerminal()
    s1 assertCompleted()
    s1 assertNoErrors()
    s1 assertReceivedCount 6
    s2 cancel()
    s2 assertCanceled()
    s2 assertNoErrors()
  }
```
In the above example, we create a requestResponse subscriber and subscribe it to a trivial publisher with initial payload data "a" and metadata "b". We then call request on it, and assert the behaviors we expect.
When we assert received, since it is a requestResponse, we only expect a list of 1 payload, and we assert both the data and metadata received. In this example, we are testing interleaving 3 requestResponse subscribers.

Notice that the test for channel incorporates both server and client behavior. You are able to send data and assert data received. The IO is non-blocking, while the await blocks the thread running the main
tests. So for example, calling `respond(...)` and then `request ...` will not block the request if the client can't respond, but calling `await ...` will block anything after it, but not respond requests
that have already be started.

#### We will update this to use reflection like JUnit tests

## Responder DSL
The responder DSL example is the dual to the above requester DSL.
```
    // this handles request response
    requestResponse handle("a", "b") using(Map("x" -> ("hello", "goodbye")), pause(3), emit('x'),
      pause(4), pause(5), complete)
    requestResponse handle("c", "d") using(Map("x" -> ("ding", "dong")), pause(10), emit('x'),
      pause(10), complete)
    requestResponse handle("e", "f") using(pause(10), error)
    requestResponse handle("g", "h") using("-")

    // this handles request streams
    requestStream handle("a", "b") using(Map("a" -> ("a", "b"), "b" -> ("c", "d"), "c" -> ("e", "f")),
      "---a-----b-----c-----d--e--f---|")
    requestStream handle("c", "e") using(Map("a" -> ("a", "b"), "b" -> ("c", "d"), "c" -> ("e", "f")),
      "---a-----b-----c-----d--e--f---|")

    // this handles request subscriptions, it's exactly the same as stream but no onComplete
    requestSubscription handle("a", "b") using("abcdefghijklmnop")

    // this handles channel with an initial payload of ("a", "b")
    requestChannel handle("a", "b") asFollows(() => {
      val s1 = channelSubscriber()
      respond("---x---")
      s1 request 1
      s1 awaitAtLeast(2, 1000)
      s1 assertReceivedCount 2
      s1 assertReceived List(("a", "b"), ("a", "a"))
      s1 request 5
      s1 awaitAtLeast(7, 1000)
      respond("a---b---c")
      s1 request 5
      s1 awaitAtLeast(12, 1000) // there's an implicit request 1 in the beginning
      respond("d--e---f-")
      respond("|")
      s1 awaitTerminal()
      s1 assertCompleted()
    })
    end
```
Here, we write that we want to create requestResponse handlers that handle some initial payload. The optional map argument allows testers to map data and metadata to characters. Under the hood, this is using almost exactly
the same syntax as the marble diagrams in the rxjs project [here](https://github.com/ReactiveX/rxjs/blob/master/doc/writing-marble-tests.md#marble-syntax). Users can also directly write the marble diagram into the test cases
instead of using the programatic notion we have here, so one can write something like `requestResponse handle("x", "y") using ("---a---|")`.

Notice that the syntax for this channel handler is exactly the same as the client's except for a single difference. In the client, the `handle` call is instead a `using` call. Using tells the client
to send an initial payload, while handle tells the server to expect an initial payload. Because of this, we already have an element received before we start our tests, which means we must await
at least one additional element than we request.

## Run Instructions
This project is managed with sbt. Simply navigate to the root directory with build.sbt and run `sbt assembly`. You can then use `./run <scriptfile>` to run the server or client with a specific script file.

## TODO
We need still need to add support for certain types of tests for channel, and find try to write tests so we can idenfity any problems with the TCK
