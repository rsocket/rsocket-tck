package com.facebook.lithium

object RequestSubscriptionTests extends ClientDSL {
  def main(args: Array[String]) {
    val s1 = requestSubscription("a", "b")
    s1 request 5
    s1 awaitAtMost (5, 1000)
    s1 assertNotCompleted()
    s1 assertNoErrors()
    val s2 = requestStream("a", "b")
    s2 request 1
    s2 awaitAtMost (1, 1000)
    s2 assertReceived List(("a", "b"))
    s1 assertReceivedCount 5
    s1 request 100
    s2 assertNoErrors()
    s2 assertNotCompleted()
    s2 request 1
    s2 awaitAtMost (1, 1000)
    s2 assertReceived List(("a", "b"), ("c", "d"))
    s1 take 7 // 7 total
    s1 assertReceivedAtLeast 7
    s1 assertNotCompleted()
    s1 assertCanceled()
    s1 assertNoErrors()
    end
  }
}
