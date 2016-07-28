package io.reactivesocket.tck

object client extends RequesterDSL {

  def main(args: Array[String]): Unit = {
    RequesterTests.runTests(this, this.writer)
  }

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
  def requestResponseTimeoutFail() : Unit = {
    val s = requestResponse("e", "f")
    s request 1
    s awaitTerminal()
  }

}

object server extends ResponderDSL {
  def main(args: Array[String]): Unit = {
    ResponderTests.runTests(this, this.writer)
  }

  @Test
  def handleRequestResponse() : Unit = {
    requestResponse handle("a", "b") using("a|")
    requestResponse handle("c", "d") using("#")
    requestResponse handle("e", "f") using ("-")
  }

}