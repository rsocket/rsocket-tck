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
  object clienttest extends RequesterDSL {
  def main(args: Array[String]) {
    RequesterTests.runTests(this, this.writer)
  }

  @Test
  def echoTest() : Unit = {
    requestChannel using("e", "f") asFollows(() => {
      respond("a")
      val cs = channelSubscriber()
      cs request(1)
      cs awaitAtLeast (1)
      cs request(10)
      respond("abcdefghijkmlnopqrstuvwxyz")
      cs awaitAtLeast (10)
      cs request(20)

    })
  }

  // example for testing channel
  @Test
  def channelTest() : Unit = {
    requestChannel using("a", "b") asFollows(() => { // onChannelRequest
      respond("-a-")
      val s1 = channelSubscriber
      s1 request 1
      respond("-b-c-d-e-f-")
      s1 awaitAtLeast(1)
      s1 assertReceivedAtLeast 1
      s1 assertReceived List(("x", "x"))
      s1 request 2
      respond("-g-h-i-j-k-")
      s1 awaitAtLeast(4)
      s1 request 4
      s1 awaitAtLeast(7)
      respond("|")
      s1 awaitTerminal()
      s1 assertCompleted()
    })
  }

  @Test
  def requestresponsePass() : Unit = {
    val s1 = requestResponse("a", "b")
    s1 request 1
    s1 awaitTerminal()
    s1 assertCompleted()
  }
  ....
}
```

Notice that the test for channel incorporates both server and client behavior. You are able to send data and assert data received. The IO is non-blocking, while the await blocks the thread running the main
tests. So for example, calling `respond(...)` and then `request ...` will not block the request if the client can't respond, but calling `await ...` will block anything after it, but not respond requests
that have already be started.

## Responder DSL
The responder DSL example is the dual to the above requester DSL.
```
object servertest extends ResponderDSL {
  def main(args: Array[String]) {
    ResponderTests.runTests(this, this.writer)
  }

  @Test
  def handleRequestResponse() : Unit = {
    requestResponse handle("a", "b") using(Map("x" -> ("hello", "goodbye")), pause(3), emit('x'),
      pause(4), pause(5), complete)

    requestResponse handle("c", "d") using(Map("x" -> ("ding", "dong")), pause(10), emit('x'),
      pause(10), complete)

    requestResponse handle("e", "f") using(pause(10), error)

    requestResponse handle("g", "h") using("-")
  }

  @Test
  def handleRequestStream() : Unit = {
    requestStream handle("a", "b") using(Map("a" -> ("a", "b"), "b" -> ("c", "d"), "c" -> ("e", "f")),
      "---a-----b-----c-----d--e--f---|")
    requestStream handle("c", "d") using(Map("a" -> ("a", "b"), "b" -> ("c", "d"), "c" -> ("e", "f")),
      "---a-----b-----c-----d--e--f---|")
  }

  @Test
  def handleRequestChannel() : Unit = {
    requestChannel handle("a", "b") asFollows(() => {
      val s1 = channelSubscriber()
      respond("---x---")
      s1 request 1
      s1 awaitAtLeast(2)
      s1 assertReceivedCount 2
      s1 assertReceived List(("a", "b"), ("a", "a"))
      s1 request 5
      s1 awaitAtLeast(7)
      respond("a---b---c")
      s1 request 5
      s1 awaitAtLeast(12) // there's an implicit request 1 in the beginning
      respond("d--e---f-")
      respond("|")
      s1 awaitTerminal()
      s1 assertCompleted()
    })
  }
....
}
    
```
Here, we write that we want to create requestResponse handlers that handle some initial payload. The optional map argument allows testers to map data and metadata to characters. Under the hood, this is using almost exactly
the same syntax as the marble diagrams in the rxjs project [here](https://github.com/ReactiveX/rxjs/blob/master/doc/writing-marble-tests.md#marble-syntax). Users can also directly write the marble diagram into the test cases
instead of using the programatic notion we have here, so one can write something like `requestResponse handle("x", "y") using ("---a---|")`.

Notice that the syntax for this channel handler is exactly the same as the client's except for a single difference. In the client, the `handle` call is instead a `using` call. Using tells the client
to send an initial payload, while handle tells the server to expect an initial payload. Because of this, we already have an element received before we start our tests, which means we must await
at least one additional element than we request.

## Run Instructions
This project is managed with sbt. Simply navigate to the root directory with build.sbt and run `sbt assembly`.
You can generate a script by first creating an object that extends either `RequesterDSL` or `ResponderDSL` (depending on if you want to generate a requester script or a responder script). Then, follow the examples above to start writing your scripts.
When you want to generate the script, compile again with `sbt assembly` and then use the run script `./run object-name`, where `object-name` is the name of the Scala object containing the tests.

## Documentation

### DSL Documentation
The examples of the DSL above are fairly self explanatory, but we will provide full coverage of the DSL usage here.
For those unfamiliar with Scala, it is useful to know that an `object` is a single instance of a class; think of it like
a static class in Java. `object` classes can have `main` methods just like Java, which is executable. `object`s, like classes,
can extend other classes as well. Here, we are extending the DSL classes so we can use their functions.


To write tests in the DSL, we must create an `object`, and the object must have a `main` method with either `ResponderTests.runTests(this, this.writer)`
or `RequesterTests.runTests(this, this.writer)`, depending on which type of test you are writing. This is necessary for the
way in which we use reflection to run each of the test functions.

Individual tests must go into their own functions, but the test inside each function can support the creation of multiple
types of subscribers (so they can be arbitrarily complex). Each function also MUST have the annotation `@Test` on top,
or else it will not be registered as a test function. The examples at the top show the general structure of the test.
However, there are actually two DSLs, a requester and responder one that differ quite a bit. We will go through them here.

#### Requester DSL
`val s = request<Response/Stream/Subscription>("a", "b")` : this is the line that should tell the driver to create a subscriber
with initial payload with data = "a" and metadata = "b". We can also create a fire-and-forget subscriber as well. If we use
an IDE like Intellij to write these tests, the autocomplete feature should be very helpful. Also, we say "tell the driver" for now,
but we will go into detail later on exactly how a driver would be able to parse this command.

##### Request Commands

`s request <n>` : we notice that every line of the DSL defines an action on a subscriber we created earlier. This is intentionally
very similar to OO programing, and should be very easy to pick up. This particular line tells the driver to request <n>
with the subscriber we defined earlier.

`s take <n>` : instead of `request <n>`, but in this case, we are saying "wait until we have received IN TOTAL <n> values,
and then cancel". A take should always block and should cancel immediately after unblocking. This is a mix of a request and
await command.

##### Await Commands

`s awaitAtLeast <n>` : this should block the test thread until the subscriber has received IN TOTAL <n> values, and then
continue on with the remainder of the test.

`s awaitTerminal` : this should block the test thread until the subscriber has received a terminal event, which is either
an onComplete or an onError call.

`s awaitNoAdditionalEvents <t>` : this should block the test thread for <t> milliseconds, and fails if it received any additional
events (onNext, terminal event, cancel). This is a mix of an await and assert command.

##### Assert Commands
`s assertNoErrors` : this should tell the driver to assert that the subscriber has not received any onError. The test should
fail if this assertion fails.

`s assertError` : asserts that this subscriber DID receive an onError.

`s assertReceived List(("a", "b"), ("c", "d"), ...)` : asserts that this subscriber has received, in its lifetime, the
following values in order.

`s assertReceivedCount <n>` : asserts that this subscriber has received, in its lifetime, exactly <n> values.

`s assertReceivedAtLeast <n>` : asserts that this subscriber has received, in its lifetime, at least <n> values.

`s assertNoValues` : asserts that this subscriber has never received any values in its lifetime

`s assertCompleted` : asserts that this subscriber has received an onComplete

`s assertNotCompleted` : asserts that this subscriber has not received an onComplete

`s assertCanceled` : asserts that this subscriber has been canceled. This would only be useful in channel, subscription
and stream tests.

#### Responder DSL
The responder DSL takes advantage of the marble syntax defined in rxjs as we mentioned earlier, so feel free to take
a look at that repository for a more detailed explanation of the marble syntax. We are using only a subset of the marble syntax.

##### Marble Syntax
The marble diagram is a single continuous string. You can also do it programatically in all tests except for channel tests.
The idea of the marble diagram is that it is an easy to parse way to determine server behavior.

`-` or `pause(n)`: this tells the server to pause for some small unit of time. However, because we don't care about time in
ReactiveSocket, this effectively does nothing except make the marble diagram easier to understand.

`<character>` : a character in the marble diagram tells the server to send something at that point. This can either be
the character itself as both the data and metadata, or the user can define a mapping of characters to payloads. We will go into
that later on.

`#` : tells the server to respond with an onError

`|` : tells the server to respond with an onComplete

##### Handler Syntax

An implementation of ReactiveSocket should allow the responder to set up handlers that intercept queries from the requester.
We can define behavior for the handlers using the marble syntax along with some simple DSL syntax. Not counting channel, there are 4 different
types of handlers: requestResponse, requestStream, requestSubscription, requestFireAndForget.

We can define the behavior for each of these channels the same way. Using streams as an example, we can do

`requestStream handle(a, b) using(Map("x" -> ("hi", "bye")), "---x--y--z--|")` : this tells the server driver to create a
requestStream handler that, given the initial payload with data `a` and metadata `b`, do the behavior defined in the marble
diagram, and map the value `'x'` to a Payload("hi", "bye"). The unmapped values, such as `'y'`, can just turn into a
Payload("y", "y"), but it is up to the driver to decide. The following code is also equivalent. `requestStream handle(a, b)
using(Map("x" -> ("hi", "bye"), pause(3), emit('x'), pause(2), emit('y'), pause(2), emit('z'), pause(2), complete)`.

## Channel DSL

As you might imagine, channel tests will have to encompass both requester and responder commands. Channel tests will look like a
mix of the two.

On the requester side, we define a request to start a channel connection using `requestChannel using(a, b)`. This sets
the initial payload as (a, b). On the responder side, we define a channel handler using requestChannel handle(a, b). This
says that if our channel handler gets an initial payload of (a, b), we use this request handler to handle it.
Then, in the `asFollows` clause, we then define the behavior of the channel using both requester and responder elements.

`val s = channelSubscriber` : this creates a subscriber with the exact same functionality we defined earlier; and uses
the same syntax

`respond(<marble>)` : this tells the driver to asynchronously respond with the given marble. However, keep in mind that it still may
not have responded with the marble of previous `respond` calls earlier on in the test. This is because the channel still must
follow flow control (`request(n)`). Therefore, the driver should asynchronously stage this marble to be sent, and it should
be sent once all the marbles before this one have been sent as well. The `respond` command must be nonblocking, as in the main test thread
should not be waiting for the entire marble diagram to be processed. However, `await` calls should be blocking the main
test thread just like before.

## Driver Tips

Here are some tips on creating and structuring the driver for imperative programming languages.



