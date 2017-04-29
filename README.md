# Reactive Socket TCK

The goal of this project is to provide a reliable polyglot testing suite to verify both existing and upcoming versions of servers and clients that use the Reactive Sockets protocol.

## Outline

The challenge of creating any test suite for network protocols is that we must have a wait for one side to behave according to how we expect, so we can check that the appropriate outputs are received given a certain input.
Another challenge is that the tests must be polyglot (support multiple languages), as this network protocol can, and will be, implemented in various languages.
We accomplish these challenges by defining a Domain Specific Language (DSL) using Scala, which generates an intermediate script that potential testers can then write drivers for that will drive their existing application.
We have a DSL for generating client tests as well as a DSL that can specify server behavior. The intermediate script generated is human readable and can be parsed easily line by line, but allows for powerful functionality.
This allows implementers of the Reactive Socket protocol to be able to write their own drivers without too much trouble.

## Running the TCK

This project generates script files, which are parsed and executed by implementors.
To generate the script files:

    sbt compile
    ./run.sh client clientscript.txt
    ./run.sh server serverscript.txt

This gives you the server and client scripts, the format of which is documented below. 
For examples of how to run them, see the TCK driver implementation in the reactivesocket-java project.

## Requester DSL

The DSLs are designed to be human readable as well, and should require very little documentation to understand. Here is an example of the client side DSL
```scala
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

## QuickStart Instructions
There are two sets of premade tests in this project. They are in the files `ReactiveSocketCoreTests.scala` and `SampleTests.scala`.
To generate the script files for the `SampleTests.scala` set of tests, first compile the project with `sbt assembly` and then to generate
the script run `./run.sh clienttest` and `./run.sh servertest`. This should generate the script files in the current directory.
These files will be needed in order to run the driver. The Java driver can be found [here](https://github.com/ReactiveSocket/reactivesocket-java/tree/master/reactivesocket-tck-drivers).

For a quickstart guide on how to write tests, one can basically copy and extend the existing tests in the project. The server side
knows how to behave in response to a request on the client side from the initial payload, so when writing tests, make sure that
for ever client test that is `using(a, b)` for some request type `r`, there is a server handler that is `handle(a, b)` for that
same request type `r`.

## Responder DSL
The responder DSL example is the dual to the above requester DSL.
```scala
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
When you want to generate the script, compile again with `sbt assembly` and then use the run script `./run.sh object-name`, where `object-name` is the name of the Scala object containing the tests.

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
The tests we write will have the following general format
```scala
@Test(pass = fail)
def testName() : Unit = {
  // test logic
}
```
Note that that `(pass = fail)` part of the test header is optional. This line is for asserting the outcome of the test,
as there are some situations that users would want to write tests that will fail. The default behavior is `(pass = true)`
so for most tests that assert correct behavior, this part of the annotation is not needed.

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
says that if our channel handler gets an initial payload of (a, b), we use this request handler to handle it. The server side DSL
allows the user to specify that the channel test SHOULD fail (if we're testing negative behavior such as a timeout), and this
can be done by using `requestChannel handle(a, b) shouldFail() asFollows(() => { ...`.
Then, in the `asFollows` clause, we define the behavior of the channel using both requester and responder elements.

`val s = channelSubscriber` : this creates a subscriber with the exact same functionality we defined earlier; and uses
the same syntax.

`respond(<marble>)` : this tells the driver to asynchronously respond with the given marble. However, keep in mind that it still may
not have responded with the marble of previous `respond` calls earlier on in the test. This is because the channel still must
follow flow control (`request(n)`). Therefore, the driver should asynchronously stage this marble to be sent, and it should
be sent once all the marbles before this one have been sent as well. The `respond` command must be nonblocking, as in the main test thread
should not be waiting for the entire marble diagram to be processed. However, `await` calls should be blocking the main
test thread just like before.

`createEchoChannel using(a, b)`
Drivers can also choose to support echo tests for channel. On the client side, this echo channel sends an initial payload `(a, b)` to the server,
and then starts its echo behavior. It should respect flow control. On the server side, the syntax is `requestEchoChannel handle(a, b)`, which
waits for a channel request with initial payload `(a, b)`, and then does echo behavior. This should also respect flow control.

## Driver

Writing the driver requires an understanding of the script that is generated by the DSLs. We will go over each of the possible
lines that can be generated, and how interpret it.

### Client Script

`subscribe%%<kind>%%<id>%%<initdata>%%<initmetadata>` : The driver should create a subscriber of type `kind` (either `rr` for request response,
`rs` for requeststream, `sub` for subscription, `fnf` for fire and forget) and register it under the `id`. We would recommend using
a hash table to keep track of the subscribers.

`request%%<n>%%<id>` : The driver should tell the subscriber with `id` to request `n`.

`cancel%%<id>` : The driver should tell the subscriber with `id` to cancel its subscription.

`await%%terminal%%<id>` : The driver should hang the test thread running the current test and await for a terminal event sent to
the subscriber with `id`.

`await%%atLeast%%<id>%%<n>` : The driver should hang the test thread and await for the subscriber with `id` to have IN TOTAL `n` 
items send to it by `onNext`.

`await%%no_events%%<id>%%<time>` : The driver should wait for `time` and assert that it received no additional events during that time, such
as `onNext`, `onComplete`, or `cancel`.

`take%%<n>%%<id>` : The driver should hang the test thread until it has received at least `n` items IN TOTAL, and then immediately cancel.

`assert%%no_error%%<id>` : The driver should assert that the test subscriber has not received any `onError`.

`assert%%error%%<id>` : The driver should assert that the test subscriber has received an `onError`.

`assert%%received%%<id>%%a,b&&c,d&&e,f...` : The driver should assert that the test subscriber has received the values `(a,b),
(c,d), (e,f), ...` throughout its lifetime in that order. Each tuple represents data and metadata.

`assert%%received_n%%<id>%%<n>` : The driver should assert that the test subscriber with `id` has received exactly `n` values.

`assert%%received_at_least%%<id>%%<n>` : Same as above, but this one asserts at least `n` values.

`assert%%completed%%<id>` : This asserts that the test subscriber with `id` has received an `onComplete`.

`assert%%no_completed%%<id>` : This asserts the opposite of the above.

`assert%%canceled%%<id>` : This asserts that the test subscriber has canceled its request.

### Server Script

`<type>%%<initdata>%%<initmeta>%%<marble>&&[map]` : The driver should create a handler for the type of interaction specified in
`type` (either `rr`, `rs`, `sub`, `fnf` like before). The handler should activate the response in `marble` upon receiving the
initial payload `(a, b)`. We would recommend using hash tables for this. The optional `map` argument maps the characters in the
marble diagram to actual payloads. The map is a simple JSON string `{"a":{"hello":"goodbye"}, "b":{...}...}`. In this case, we are
mapping `a` to a payload of `(hello, goodbye)`, and so on.

### Channel Script

The channel script is a bit more self contained. Enclosing the channel behavior, we have
```
channel%%a%%b%%{
...
}
```
This should tell the driver that enclosed is everything with the channel behavior. On the client side, the client should
send a channel request with the initial payload `(a, b)`, and on the server side, if the channel handler receives a request
with initial payload `(a, b)`, it should execute the behavior enclosed. Now, we look at the lines that are inside. On the server side however,
this channel script can also look like
```
channel%%a%%b%%fail%%{
...
}
```
This just tells the server that this channel test is expected to fail.

`respond%%<marble>` : This should tell the driver to asynchronously stage the marble string to be sent.

Everything else is the same as the regular client script we've seen above, except in this case the ID doesn't mean much
as the channel only has one subscriber.

### Driver Tips

Here are some tips on how to structure drivers from the viewpoint of building the Java driver.

#### Test Subscriber

Having a Test Subscriber is essential to creating the driver. A Test Subscriber is one that implements the Subscriber
interface in Reactive Streams, but also has additional functionality to keep track of values it has received and to perform
the awaits and assertions that are mentioned above and contained in the TCK. Building the Test Subscriber may require
some basic async structures such as CountDownLatch, but should be fairly straightforward to build.

#### Client Driver

The structure of the Java client driver is to have one central "driver" class that takes in a method pointer that allows it
to construct ReactiveSockets. Then, it does a quick initial parse through the file and separates the tests out into their
own collections. Then, it goes through each of the connections and calls a "parse" method that goes through each line of the
test and enacts the necessary behavior, most of which involve calling the Test Subscriber to do something. It may be useful
to have each test in a separate thread so that the failure of one test doesn't affect the others.

#### Server Driver

The structure of the Java server driver is to also have a central driver class. In the Java implementation, the ReactiveSocket
server uses a structure called a RequestHandler that determines behavior upon triggering one of the request types of
ReactiveSocket. The server driver parses through the file to build up the request handler; note that it does it in a way
such that the behavior will be determined at runtime, such as using hashmaps to determine behavior based on the initial payload.

#### Channel Handler

Most of the driver is straightforward, but the part that handles channel behavior is somewhat more complicated. On the client side,
upon the main driver reaching the channel block, it should immediately collect all lines inside the block and execute the channel
test inside. Since the channel test syntax is not quite the same as the main test syntax, it would be useful to have a separate
class/function to parse through the channel tests. Since the IO should not block the main test thread and that flow control should be respected, it is imperative that
the implementer be able to implement a basic marble parser with a backpressure buffer. If one side has more items to emit than the
other side requests, the former should wait until the latter has made a request, and then immediately try to fulfill that request.
Conversely, if one side has requested more items than the other side has to emit, once the latter side has more items to emit,
it should immediately emit them to the former side. Thankfully, this can be done without any fancy Rx code, and the general structure
may look like the following
```scala
class ParseMarble {

  def synchronous add(marble : String) : Unit = {
    // remove '-' characters and add to some global queue or list
    // if additional marble stuff to send, unblock parselatch
  }

  def synchronous request(n : Long) : Unit = {
    // update total request
    // if greater than 0, unblock sendlatch
  }

  def parse() : Unit = {
    while (true) {
        if (no more data to send) {
          //parselatch await
        }
        // send non-emmitable data like onComplete and onError
        if (cannot emit more items because not enough request) {
          //sendlatch await
        }
    }
  }

}
```

If one decides to go this route in implementing the driver, the `add()` method should be called using a thread that isn't waited on
in order to make the IO async from the rest of the test thread, and each
thread should make sure the previous thread that is calling add has finished before calling add itself so that marble
order does not get mixed up.

## Future Work

Of the bugs that the TCK has found so far, all were edge cases that would have been hard to identify during "normal" usage of ReactiveSockets
because they include events in strange orders that might rarely happen during normal usage. One way to be able to more quickly find these
bugs would be to implement fuzzing at the application level. In this case, we would generate events in a random order and assert on the result.
In order to do the assertions correctly however, this would require us to generate some sort of state graph and do a random traversal on it,
and keep track of the state we end on, so we can assert that the behavior indeed matches what we expect on that state.
