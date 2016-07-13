package io.reactivesocket.tck

import java.io.{File, PrintWriter}

class ClientDSL {

  val writer: PrintWriter = new PrintWriter(new File(this.getClass.getSimpleName + ".txt"))

  def requestResponse(data: String, metadata: String) : DSLTestSubscriber = {
    return new DSLTestSubscriber(writer, data, metadata, "rr");
  }

  def requestStream(data: String, metadata: String) : DSLTestSubscriber = {
    return new DSLTestSubscriber(writer, data, metadata, "rs");
  }

  def firenForget(data: String, metadata: String) : DSLTestSubscriber = {
    return new DSLTestSubscriber(writer, data, metadata, "fnf");
  }

  def requestSubscription(data: String, metadata: String) : DSLTestSubscriber = {
    return new DSLTestSubscriber(writer, data, metadata, "sub");
  }

  def requestChannel(marble: Map[(String, String), String]) : DSLTestSubscriber = {
    return new DSLTestSubscriber(writer, marble)
  }

  def requestChannel(argMap: Map[String, (String, String)], marble: Map[(String, String), String]) : DSLTestSubscriber = {
    return new DSLTestSubscriber(writer, argMap, marble)
  }

  def end() : Unit = {
    println("ended")
    writer.write("EOF\n")
    writer.close()
  }

  def begintest(test : () => Unit) : Unit = {
    writer.write("!\n")
    test()
  }

  def nametest(name: String) : Unit = writer.write("name%%" + name + "\n")


  trait ChannelHandler {
    def using(data: String, meta: String) : ChannelHandler
    def asFollows(f: () => Unit): Unit
  }

  object requestChannel extends ChannelHandler {
    override def using(data: String, meta: String) : ChannelHandler = {
      writer.write("channel%%" + data + "%%" + meta + "%%")
      return this
    }
    override def asFollows(f: () => Unit) = {
      writer.write("{\n")
      f()
      writer.write("}\n")
    }
  }

  def channelSubscriber() : DSLTestSubscriber = {
    // we create a trivial subscriber because we don't need a "real" one, because we will already pass in a test
    // subscriber in the driver, as one should have already been created to get the initial payload from the client
    return new DSLTestSubscriber(writer, "", "", "");
  }

  def respond(marble : String) : Unit = {
    writer.write("respond%%" + marble + "\n")
  }

}

object clienttest extends ClientDSL {
  def main(args: Array[String]) {
    //begintest(test0)
    begintest(test1)
    begintest(test2)
    begintest(test3)
    //begintest(test4)
    end
  }

  // example for testing channel
  def test0() : Unit = {
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

  def test1() : Unit = {
    nametest("test1")
    val s1 = requestResponse("a", "b")
    s1 request 1
    s1 awaitTerminal()
    s1 assertCompleted()
  }

  def test2() : Unit = {
    nametest("test2")
    val s1 = requestResponse("c", "d")
    s1 request 1
    s1 awaitTerminal()
    s1 assertReceived List(("ding", "dong"))
    s1 assertCompleted()
    s1 assertNotCompleted()
    s1 assertNoErrors()
  }

  def test3() : Unit = {
    nametest("test3")
    val s1 = requestResponse("e", "f")
    s1 request 1
    s1 awaitTerminal()
    s1 assertError()
    s1 assertNotCompleted()
  }

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
}
