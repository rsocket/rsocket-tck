package io.reactivesocket.tck

object client extends RequesterDSL {

  def main(args: Array[String]): Unit = {
    RequesterReflection.runTests(this, this.writer)
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
  def requestChannelMultiVsSingle() : Unit = {
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

  @Test
  def requestChannelMultiVsMulti() : Unit = {
    requestChannel using("k", "l") asFollows(() => {
      val s = channelSubscriber()
      respond("a-b-c-|")
      s request 3
      s awaitAtLeast 3
      s awaitTerminal()
      s assertReceivedAtLeast 3
      s assertCompleted()
      s assertNoErrors()
    })
  }

  @Test(pass = false)
  def requestChannelMultiVsNoResponse() : Unit = {
    requestChannel using("m", "n") asFollows(() => {
      val s = channelSubscriber()
      respond("a-b-c-d-|")
      s request 10
      s awaitAtLeast 10
    })
  }

  @Test
  def requestChannelMultiVsError() : Unit = {
    requestChannel using("o", "p") asFollows(() => {
      val s = channelSubscriber()
      respond("a-b-c-d-e-|")
      s request 1
      s awaitTerminal()
      s assertNotCompleted()
      s assertError()
    })
  }

  @Test
  def requestChannelNoResponseVsSingle() : Unit = {
    requestChannel using("q", "r") asFollows(() => {
      val s = channelSubscriber()
      s request 1
      s awaitTerminal()
      s assertReceivedCount 1
      s assertCompleted()
      s assertNoErrors()
    })
  }

  @Test
  def requestChannelNoResponseVsMulti() : Unit = {
    requestChannel using("s", "t") asFollows(() => {
      val s = channelSubscriber()
      s request 3
      s awaitAtLeast 3
      s awaitTerminal()
      s assertCompleted()
      s assertNoErrors()
    })
  }

  @Test
  def requestChannelNoResponseVsError() : Unit = {
    requestChannel using("u", "v") asFollows(() => {
      val s = channelSubscriber()
      s request 2
      s awaitTerminal()
      s assertNotCompleted()
      s assertError()
    })
  }

  @Test(pass = false)
  def requestChannelNoResponseVsNoResponse() : Unit = {
    requestChannel using("w", "x") asFollows(() => {
      val s = channelSubscriber()
      s request 1
      s awaitAtLeast 1
    })
  }

  @Test
  def requestChannelErrorVsSingle() : Unit = {
    requestChannel using("y", "z") asFollows(() => {
      val s = channelSubscriber()
      s request 1
      s awaitTerminal()
      s assertReceivedCount 1
      s assertCompleted()
      s assertNoErrors()
      respond("a-#")
    })
  }

  @Test
  def requestChannelErrorVsMulti() : Unit = {
    requestChannel using("aa", "bb") asFollows(() => {
      val s = channelSubscriber()
      s request 3
      s awaitTerminal()
      s assertReceivedCount 3
      s assertCompleted()
      s assertNoErrors()
      respond("#")
    })
  }

  @Test
  def requestChannelErrorVsError() : Unit = {
    requestChannel using("cc", "dd") asFollows(() => {
      val s = channelSubscriber()
      s request 1
      respond("#")
      s awaitTerminal()
      s assertError()
      s assertNotCompleted()
      s assertReceivedCount 0
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

  @Test
  def requestChannelCancel() : Unit = {
    requestChannel using("ee", "ff") asFollows(() => {
      val s = channelSubscriber()
      s request 1
      respond("a-b-c-d-e")
      s awaitAtLeast 1
      s cancel()
      s assertCanceled()
    })
  }

  @Test
  def requestChannelCancel2() : Unit = {
    requestChannel using("gg", "hh") asFollows(() => {
      val s = channelSubscriber()
      s request 1
      respond("a-b-c-|")
      s awaitAtLeast 1
      s cancel()
      s assertCanceled()
    })
  }

  @Test
  def requestChannelCancel3() : Unit = {
    requestChannel using("ii", "jj") asFollows(() => {
      val s = channelSubscriber()
      s request 3
      respond("a-b-c-d-e")
      s awaitTerminal()
      s assertCompleted()
      s assertNoErrors()
    })
  }

  @Test
  def requestResponseMultipleSuccession() = {
    val s1 = requestResponse("aa", "bb")
    val s2 = requestResponse("aa", "bb")
    val s3 = requestResponse("aa", "bb")
    val s4 = requestResponse("aa", "bb")
    s1 request 1
    s2 request 1
    s3 request 1
    s4 request 1
    s1 awaitAtLeast 1
    s2 awaitAtLeast 1
    s3 awaitAtLeast 1
    s4 awaitAtLeast 1
  }

  @Test
  def requestStreamMultipleSuccession() = {
    val s1 = requestStream("aa", "bb")
    val s2 = requestStream("aa", "bb")
    val s3 = requestStream("aa", "bb")
    val s4 = requestStream("aa", "bb")
    s1 request 1
    s2 request 1
    s3 request 1
    s4 request 1
    s1 awaitAtLeast 1
    s2 awaitAtLeast 1
    s3 awaitAtLeast 1
    s4 awaitAtLeast 1
  }

  @Test
  def requestChannelMultipleSuccession() = {
    requestChannel using("xx", "yy") asFollows(() => {
      val s = channelSubscriber()
      s request 1
      s awaitAtLeast 1
    })
    requestChannel using("xx", "yy") asFollows(() => {
      val s = channelSubscriber()
      s request 1
      s awaitAtLeast 1
    })
    requestChannel using("xx", "yy") asFollows(() => {
      val s = channelSubscriber()
      s request 1
      s awaitAtLeast 1
    })
    requestChannel using("xx", "yy") asFollows(() => {
      val s = channelSubscriber()
      s request 1
      s awaitAtLeast 1
    })
  }

  @Test
  def requestResponseRequestAfterCancel(): Unit = {
    val s = requestResponse("request", "cancel")
    s cancel()
    s request 1
    s awaitNoAdditionalEvents 2000
    s assertReceivedCount 0
  }

  @Test
  def requestResponseMultipleCancel() : Unit = {
    val s1 = requestResponse("aa", "bb")
    val s2 = requestResponse("aa", "bb")
    val s3 = requestResponse("aa", "bb")
    val s4 = requestResponse("aa", "bb")
    s1 request 1
    s2 request 1
    s3 request 1
    s4 request 1
    s1 cancel()
    s2 cancel()
    s3 cancel()
    s4 cancel()
  }

  @Test
  def requestStreamMultipleCancel() : Unit = {
    val s1 = requestStream("aa", "bb")
    val s2 = requestStream("aa", "bb")
    val s3 = requestStream("aa", "bb")
    val s4 = requestStream("aa", "bb")
    s1 request 1
    s2 request 1
    s3 request 1
    s4 request 1
    s1 cancel()
    s2 cancel()
    s3 cancel()
    s4 cancel()
  }

  @Test
  def requestStreamAfterCancel() : Unit = {
    val s = requestStream("after", "cancel")
    s request 1
    s awaitAtLeast 1
    s assertReceivedCount 1
    s cancel()
    s assertCanceled()
    s request 1
    s awaitNoAdditionalEvents 2000
    s assertReceivedCount 1
  }

  @Test(pass = false)
  def requestChannelAfterCancel() : Unit = {
    requestChannel using("after", "cancel") asFollows(() => {
      val s = channelSubscriber()
      s request 1
      respond("a-b-c-d-e")
      s awaitAtLeast 1
      s assertReceivedCount 1
      s cancel()
      s request 1
      s awaitAtLeast 1 // should timeout
    })
  }

  @Test
  def requestChannelAfterCancel2() : Unit = {
    requestChannel using("after", "cancel2") asFollows(() => {
      val s = channelSubscriber()
      s request 1
      s awaitAtLeast 1
    })
  }

}

object server extends ResponderDSL {
  def main(args: Array[String]): Unit = {
    ResponderReflection.runTests(this, this.writer)
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
    requestResponse handle("aa", "bb") using("a-|")
    requestResponse handle("request", "cancel") using("a-|")
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
    requestStream handle("aa", "bb") using("a-b-c-d-|")
    requestStream handle("after", "cancel") using("a-b-c-d-e-|")
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

  @Test
  def requestChannelMultiVsMulti() : Unit = {
    requestChannel handle("k", "l") asFollows(() => {
      val s = channelSubscriber()
      respond("x-y-z-|")
      s request 3
      s awaitTerminal()
      s assertCompleted()
      s assertReceivedCount 4
      s assertNoErrors()
    })
  }

  @Test
  def requestChannelMultiVsNoResponse() : Unit = {
    requestChannel handle("m", "n") asFollows(() => {
      val s = channelSubscriber()
      s request 4
      s awaitTerminal()
      s assertReceivedCount 5
      s assertCompleted()
      s assertNoErrors()
    })
  }

  @Test
  def requestChannelMultiVsError() : Unit = {
    requestChannel handle("o", "p") asFollows(() => {
      val s = channelSubscriber()
      s request 5
      s awaitTerminal()
      s assertCompleted()
      s assertReceivedCount 6
      s assertNoErrors()
      respond("#")
    })
  }

  @Test
  def requestChannelNoResponseVsSingle() : Unit = {
    requestChannel handle("q", "r") shouldFail() asFollows(() => { // this marks the test as should fail
      val s = channelSubscriber()
      respond("a-|")
      s request 1
      s awaitAtLeast 2
    })
  }

  @Test
  def requestChannelNoResponseVsMulti() : Unit = {
    requestChannel handle("s", "t") shouldFail() asFollows(() => {
      val s = channelSubscriber()
      respond("a-b-c-|")
      s request 1
      s awaitAtLeast 2
    })
  }

  @Test
  def requestChannelNoResponseVsError() : Unit = {
    requestChannel handle("u", "v") shouldFail() asFollows(() => {
      val s = channelSubscriber()
      respond("a-b-#")
      s request 1
      s awaitAtLeast 2
    })
  }

  @Test
  def requestChannelNoResponseVsNoResponse() : Unit = {
    requestChannel handle("w", "x") shouldFail() asFollows(() => {
      val s = channelSubscriber()
      s request 1
      s awaitAtLeast 2
    })
  }

  @Test
  def requestChannelErrorVsSingle() : Unit = {
    requestChannel handle("y", "z") asFollows(() => {
      val s = channelSubscriber()
      s request 1
      respond("a-|")
      s awaitTerminal()
      s assertError()
      s assertNotCompleted()
    })
  }

  @Test
  def requestChannelErrorVsMulti() : Unit = {
    requestChannel handle("aa", "bb") asFollows(() => {
      val s = channelSubscriber()
      s request 1
      respond("a-b-c-|")
      s awaitTerminal()
      s assertError()
      s assertNotCompleted()
    })
  }

  @Test
  def requestChannelErrorVsError() : Unit = {
    requestChannel handle("cc", "dd") asFollows(() => {
      val s = channelSubscriber()
      s request 1
      respond("#")
      s awaitTerminal()
      s assertError()
      s assertNotCompleted()
    })
  }

  @Test
  def requestChannelCancel() : Unit = {
    requestChannel handle("ee", "ff") asFollows(() => {
      val s = channelSubscriber()
      s request 1
      respond("a-b-c-d-e")
      s awaitAtLeast 1
      s cancel()
      s assertCanceled()
    })
  }

  @Test
  def requestChannelCancel2() : Unit = {
    requestChannel handle("gg", "hh") asFollows(() => {
      val s = channelSubscriber()
      s request 3
      respond("a-b-c-|")
      s awaitAtLeast 4
      s awaitTerminal()
      s assertCompleted()
      s assertNoErrors()
    })
  }

  @Test
  def requestChannelCancel3() : Unit = {
    requestChannel handle("ii", "jj") asFollows(() => {
      val s = channelSubscriber()
      s request 3
      respond("a-b-c-|")
      s cancel()
      s assertCanceled()
    })
  }

  @Test
  def requestChannelMultipleSuccession() : Unit = {
    requestChannel handle("xx", "yy") asFollows(() => {
      respond("a-b-c-|")
    })
  }

  @Test
  def requestChannelAfterCancel() : Unit = {
    requestChannel handle("after", "cancel") asFollows(() => {
      val s = channelSubscriber()
      s request 1
      respond("a-b-c-d-e-f")
      s awaitAtLeast 2
      s assertReceivedCount 2
    })
  }

  @Test
  def requestChannelAfterCancelServer() : Unit = {
    requestChannel handle("after", "cancel2") shouldFail() asFollows(() => {
      val s = channelSubscriber()
      s cancel()
      s request 1
      respond("a-b-c-d")
      s awaitAtLeast 2 // should timeout
    })
  }

}
