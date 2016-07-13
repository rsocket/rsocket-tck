package io.reactivesocket.tck

object RequestStreamTests extends ClientDSL {
  def main(args: Array[String]) {
    val s1 = requestStream("a", "b")
    s1 request 3
    val s2 = requestStream("c", "d")
    s1 awaitAtLeast(3, 70)
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
    end
  }
}
