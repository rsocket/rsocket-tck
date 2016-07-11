package io.reactivesocket.tck

import java.io.{File, PrintWriter}

/**
  * Created by mjzhu on 7/5/16.
  */
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

}

object clienttest extends ClientDSL {
  def main(args: Array[String]) {
    //begintest(test0)
    begintest(test1)
    begintest(test2)
    begintest(test3)
    end
  }

  def test0() : Unit = {
    val s1 = requestChannel(Map("x" -> ("hello", "goodbye")), Map(
      ("a", "b") -> "--x--x--|",
      ("c", "d") -> "----#"
    )) // sets up the channel handler,

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
}
