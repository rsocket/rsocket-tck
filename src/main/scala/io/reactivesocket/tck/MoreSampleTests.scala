package io.reactivesocket.tck

object client extends RequesterDSL {

  def main(args: Array[String]): Unit = {
    RequesterTests.runTests(this, this.writer)
  }

  // RequestResponse tests

  @Test
  def requestResponsePass() : Unit = {
    val s = requestResponse("a", "b")
    s request 1
    s awaitAtLeast 1
    s assertNoErrors()
    s assertCompleted()
    s assertReceived List(("a", "a"))
  }

  @Test
  def requestResponseError() : Unit = {
    val s = requestResponse("c", "d")
    s request 1
    s awaitTerminal()
    s assertReceivedCount 0
    s assertNotCompleted()
    s assertError()
  }

  @Test(pass = false)
  def requestResponseTimeoutFail() : Unit = {
    val s = requestResponse("e", "f")
    s request 1
    s awaitTerminal()
  }

  @Test
  def requestResponseCancel() : Unit = {
    val s = requestResponse("g", "h")
    s cancel()
    s assertCanceled()
    s assertNoErrors()
    s assertNotCompleted()
    s assertReceivedCount(0)
  }

  @Test
  def requestResponseInterleave() : Unit = {
    val s = requestResponse("i", "j")
    val s2 = requestResponse("k", "l")
    s request 1
    val s3 = requestResponse("m", "n")
    s3 request 1
    s awaitAtLeast 1
    s2 request 1
    s2 awaitAtLeast 1
    s3 awaitAtLeast 1
    s assertReceived List(("homer", "simpson"))
    s2 assertReceived List(("bart", "simpson"))
    s3 assertReceived List(("seymour", "skinner"))
  }

  // request stream tests

  @Test
  def requestStreamEmpty() : Unit = {
    val s = requestStream("a", "b")
    s request 1
    s awaitTerminal()
    s assertCompleted()
    s assertReceivedCount 0
    s assertNoErrors()
  }

  @Test
  def requestStreamSingle() : Unit = {
    val s = requestStream("c", "d")
    s request 1
    s awaitTerminal()
    s assertReceivedCount 1
    s assertNoErrors()
    s assertCompleted()
    s assertReceived List(("jimbo", "jones"))
  }

  @Test
  def requestStreamMultivalue() : Unit = {
    val s = requestStream("e", "f")
    s request 3
    s awaitAtLeast 3
    s awaitTerminal()
    s assertReceivedCount 3
    s assertCompleted()
    s assertNoErrors()
    s assertReceived List(("a", "a"), ("b", "b"), ("c", "c"))
  }

  @Test
  def requestStreamInfinite() : Unit = {
    val s = requestStream("g", "h")
    s request 3
    s awaitAtLeast 3
    s request 10
    s awaitAtLeast 10
    s assertNotCompleted()
    s assertNoErrors()
    s assertReceivedCount 13
  }

  @Test
  def requestStreamError() : Unit = {
    val s = requestStream("i", "j")
    s request 1
    s awaitTerminal()
    s assertNotCompleted()
    s assertError()
    s assertReceivedCount 0
  }

  @Test
  def requestStreamValueThenError() : Unit = {
    val s = requestStream("k", "l")
    s request 10
    s awaitAtLeast 1
    s assertReceivedAtLeast 1
    s awaitTerminal()
    s assertError()
    s assertNotCompleted()
  }

  @Test
  def requestStreamFlowControl() : Unit = {
    val s = requestStream("g", "h")
    s request 4
    s awaitAtLeast 4
    s awaitNoAdditionalEvents 2000
    s assertReceivedCount 4
    s assertNotCompleted()
  }

  @Test
  def requestStreamFlowControl2() : Unit = {
    val s = requestStream("m", "n")
    s request 10
    s awaitTerminal()
    s awaitNoAdditionalEvents 2000
    s assertReceivedAtLeast 4
    s assertNoErrors()
  }

  @Test
  def requestStreamCancel() : Unit = {
    val s = requestStream("m", "n")
    s request 1
    s cancel()
    s awaitNoAdditionalEvents 1000
    s assertCanceled()
    s assertNoErrors()
    s assertNotCompleted()
  }

  @Test
  def requestStreamInterleave() : Unit = {
    val s = requestStream("o", "p")
    val s2 = requestStream("q", "r")
    s request 1
    s2 request 2
    s request 1
    s awaitAtLeast 2
    s2 awaitAtLeast 2
    s2 request 2
    s assertReceivedCount 2
    s2 awaitAtLeast 4
    s2 assertReceivedCount 4
    s awaitTerminal()
    s2 awaitTerminal()
    s assertCompleted()
    s2 assertError()
  }

  // Request Subscription tests

  @Test
  def requestSubscriptionEmpty() : Unit = {
    val s = requestSubscription("a", "b")
    s request 1
    s assertNoErrors()
    s assertNotCompleted()
    s awaitNoAdditionalEvents 1000
    s assertReceivedCount 0
  }

  @Test
  def requestSubscriptionSingle() : Unit = {
    val s = requestSubscription("c", "d")
    s request 10
    s awaitAtLeast 1
    s awaitNoAdditionalEvents 1000
    s assertReceivedCount 1
    s assertReceived List(("jimbo", "jones"))
    s assertNotCompleted()
    s assertNoErrors()
  }

  @Test
  def requestSubscriptionMulti() : Unit = {
    val s = requestSubscription("e", "f")
    s request 10
    s awaitAtLeast 3
    s awaitNoAdditionalEvents 1000
    s assertReceivedCount 3
    s assertNotCompleted()
    s assertNoErrors()
    s assertReceived List(("a", "a"), ("b", "b"), ("c", "c"))
  }

  @Test
  def requestSubscriptionError() : Unit = {
    val s = requestSubscription("g", "h")
    s request 100
    s.awaitTerminal // do this change
    s assertNotCompleted()
    s assertError()
    s assertReceivedCount 0
  }

  @Test
  def requestSubscriptionValueThenError() : Unit = {
    val s = requestSubscription("i", "j")
    s request 10
    s awaitTerminal()
    s assertReceivedCount 1
    s assertError()
    s assertNotCompleted()
  }

  @Test
  def requestSubscriptionFlowControl() : Unit = {
    val s = requestSubscription("k", "l")
    s request 2
    s awaitAtLeast 2
    s awaitNoAdditionalEvents 1000
    s assertReceivedCount 2
    s assertNotCompleted()
    s assertNoErrors()
    s request 2
    s awaitAtLeast 4
    s awaitNoAdditionalEvents 1000
    s assertReceivedCount 4
  }

  @Test
  def requestSubscriptionFlowControl2() : Unit = {
    val s = requestSubscription("m", "n")
    s request 10
    s awaitAtLeast 4
    s awaitNoAdditionalEvents 1000
    s assertReceivedCount 4
  }

  @Test
  def requestSubscriptionCancel() : Unit = {
    val s = requestSubscription("m", "n")
    s cancel()
    s awaitNoAdditionalEvents 1000
    s assertCanceled()
    s assertNotCompleted()
    s assertNoErrors()
    s assertReceivedCount 0
    val s2 = requestSubscription("m", "n")
    s2 request 1
    s2 cancel()
    s2 awaitNoAdditionalEvents 1000
    s2 assertCanceled()
    s2 assertNotCompleted()
    s2 assertNoErrors()
  }

  @Test
  def requestSubscriptionCancel2() : Unit = {
    val s = requestSubscription("m", "n")
    s request 1
    s awaitAtLeast 1
    s cancel()
    s assertCanceled()
    s assertNotCompleted()
    s assertNoErrors()
    s awaitNoAdditionalEvents 1000
  }

  @Test
  def requestSubscriptionInterleave() : Unit = {
    val s = requestSubscription("o", "p")
    val s2 = requestSubscription("q", "r")
    s2 request 1
    s request 1
    s2 cancel()
    s awaitAtLeast 1
    s assertNoErrors()
    val s3 = requestSubscription("s", "t")
    s2 assertReceivedAtLeast 0
    s3 request 2
    s3 awaitTerminal()
    s3 assertError()
    s3 awaitNoAdditionalEvents 1000
    s3 assertNotCompleted()
    s3 assertReceivedCount 2
    s assertNotCompleted()
  }

  // fire and forget tests

  @Test
  def fireAndForget() : Unit = {
    val s = firenForget("a", "b")
    s request 1 // need to send a request in order to send the fire and forget
    s awaitTerminal()
    s assertNoErrors()
    s assertCompleted()
  }

  @Test
  def fireAndForget2() : Unit = {
    val s = firenForget("c", "d")
    s request 1
    s awaitTerminal()
    s assertNoErrors()
    s assertCompleted()
  }

  // channel tests

  @Test
  def requestChannelSingleVsSingle() : Unit = {
    requestChannel using("a", "b") asFollows(() => {
      val s = channelSubscriber()
      respond("a")
      s request 1
      s awaitAtLeast 1
      s assertReceivedCount 1
      respond("|")
      s awaitTerminal()
      s assertCompleted()
      s assertNoErrors()
    })
  }

  @Test
  def requestChannelSingleVsMulti() : Unit = {
    requestChannel using("c", "d") asFollows(() => {
      val s = channelSubscriber()
      respond("a-b-c-|")
      s request 1
      s awaitAtLeast 1
      s assertReceivedCount 1
      s awaitTerminal()
      s assertCompleted()
      s assertNoErrors()
    })
  }

  @Test(pass = false)
  def requestChannelSingleVsNoResponse() : Unit = {
    requestChannel using("e", "f") asFollows(() => {
      val s = channelSubscriber()
      s request 1
      respond("a-|")
      s awaitAtLeast 1
    })
  }

  @Test
  def requestChannelSingleVsError() : Unit = {
    requestChannel using("g", "h") asFollows(() => {
      val s = channelSubscriber()
      s request 1
      respond("a-|")
      s awaitTerminal()
      s assertError()
    })
  }

  @Test
  def requestChannelInterleaveRequestResponse() : Unit = {
    requestChannel using("i", "j") asFollows(() => {
      val s = channelSubscriber()
      respond("a")
      s request 1
      respond("b")
      s awaitAtLeast 1
      s request 2
      respond("c|")
      s awaitAtLeast 3
      s awaitTerminal()
      s assertCompleted()
      s assertNoErrors()
    })
  }


}

object server extends ResponderDSL {
  def main(args: Array[String]): Unit = {
    ResponderTests.runTests(this, this.writer)
  }

  @Test
  def handleRequestResponse() : Unit = {
    requestResponse handle("a", "b") using("a-|")
    requestResponse handle("c", "d") using("#")
    requestResponse handle("e", "f") using ("-")
    requestResponse handle("g", "h") using ("-")
    requestResponse handle("i", "j") using (Map("x" -> ("homer", "simpson")), "x-|")
    requestResponse handle("k", "l") using (Map("y" -> ("bart", "simpson")), "y-|")
    requestResponse handle("m", "n") using (Map("z" -> ("seymour", "skinner")), "z-|")
  }

  @Test
  def handleRequestStream() : Unit = {
    requestStream handle("a", "b") using("-|")
    requestStream handle("c", "d") using(Map("x" -> ("jimbo", "jones")), "x-|")
    requestStream handle("e", "f") using("a-b-c-|")
    requestStream handle("g", "h") using("a-b-c-d-e-f-g-h-i-j-k-l-m-n-o-p-q-r-s-t-u-v-w-x-y-z-|")
    requestStream handle("i", "j") using("#")
    requestStream handle("k", "l") using("a-#")
    requestStream handle("m", "n") using("a-b-c-d-|")
    requestStream handle("o", "p") using("a-b-|")
    requestStream handle("q", "r") using("a-b-c--d-#")
  }

  @Test
  def handleRequestSubscription() : Unit = {
    requestSubscription handle("a", "b") using("|")
    requestSubscription handle("c", "d") using(Map("x" -> ("jimbo", "jones")), "x-")
    requestSubscription handle("e", "f") using("a-b-c-")
    requestSubscription handle("g", "h") using("#")
    requestSubscription handle("i", "j") using("a-#")
    requestSubscription handle("k", "l") using("a-b-c-d-e-f-g-")
    requestSubscription handle("m", "n") using("a-b-c-d-")
    requestSubscription handle("o", "p") using("a-b-")
    requestSubscription handle("q", "r") using("a-b-c--d-#")
    requestSubscription handle("s", "t") using("a-b-#")
  }

  @Test
  def requestChannelSingleVsSingle() : Unit = {
    requestChannel handle("a", "b") asFollows(() => {
      val s = channelSubscriber()
      respond("a")
      s request 1
      s awaitAtLeast 2
      s assertReceivedCount 2
      respond("|")
      s awaitTerminal()
      s assertCompleted()
      s assertNoErrors()
    })
  }

  @Test
  def requestChannelSingleVsMulti() : Unit = {
    requestChannel handle("c", "d") asFollows(() => {
      val s = channelSubscriber()
      respond("a-|")
      s request 3
      s awaitTerminal()
      s assertReceivedCount 4
      s assertNoErrors()
      s assertCompleted()
    })
  }

  @Test
  def requestChannelSingleVsNoResponse() : Unit = {
    requestChannel handle("e", "f") asFollows(() => {
      val s = channelSubscriber()
      s request 1
      s awaitTerminal()
      s assertReceivedCount 2 // we could the (e, f) as well
      s assertCompleted()
      s assertNoErrors()
    })
  }

  @Test
  def requestChannelSingleVsError() : Unit = {
    requestChannel handle("g", "h") asFollows(() => {
      val s = channelSubscriber()
      s request 1
      s awaitTerminal()
      s assertCompleted()
      s assertNoErrors()
      respond("#")
    })
  }

  @Test
  def requestChannelInterleaveRequestResponse() : Unit = {
    requestChannel handle("i", "j") asFollows(() => {
      val s = channelSubscriber()
      s request 1
      respond("a")
      s awaitAtLeast 2
      s request 1
      respond("a-b")
      s awaitAtLeast 3
      s request 1
      s awaitAtLeast 4
      respond("|")
      s awaitTerminal()
      s assertCompleted()
      s assertNoErrors()
    })
  }


}