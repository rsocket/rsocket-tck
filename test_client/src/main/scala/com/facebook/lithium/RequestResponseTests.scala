package com.facebook.lithium

object RequestResponseTests extends ClientDSL {
  def main(args: Array[String]) {
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

    /*val s4 = requestResponse("g", "h") // bug
    s4 cancel()
    s4 assertCanceled()
    s4 assertNoErrors()
    s4 assertNotCompleted()
    s4 assertReceivedCount 0*/

    val s4 = requestResponse("g", "h")
    s4 request 1
    s4 cancel()
    s4 assertCanceled()
    s4 assertNoErrors()
    s4 assertNotCompleted()
    s4 assertReceivedCount 0

    end
  }
}
