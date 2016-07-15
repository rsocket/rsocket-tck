package io.reactivesocket.tck

import java.io.{File, PrintWriter}

import scala.collection.immutable.Queue

class MarbleDSL {

  import org.json4s._
  import org.json4s.native.Serialization._
  import org.json4s.native.Serialization
  implicit val formats = Serialization.formats(NoTypeHints)

  var writer: PrintWriter = new PrintWriter(new File(this.getClass.getSimpleName + ".txt"))

  trait Handler {
    def handle(data: String, meta: String) : Handler
    def using(marble: Marble *)
    def using(values: Map[String, (String, String)], marble: Marble *)
    def using(e : MyError)
    def using(s : String) // for people who want to write the actual ascii
    def using(values: Map[String, (String, String)], s : String)
  }


  trait ChannelHandler {
    def handle(data: String, meta: String) : ChannelHandler
    def asFollows(f: () => Unit): Unit
  }

  abstract class MyError

  object fail extends MyError

  abstract class HandlerImpl extends Handler {

    override def using(marble: Marble *) : Unit = {
      var str : String = ""
      for (m <- marble) {
        str += m.str
      }
      writer.write(str + "\n")
    }

    // allows user to define a map of char to (data, metadata) they want to send
    override def using(values: Map[String, (String, String)], marble: Marble *) = {
      var str : String = ""
      for (m <- marble) {
        str += m.str
      }
      str += "&&" + write(values)
      writer.write(str + "\n")
    }

    override def using(e : MyError) : Unit = {
      writer.write("error\n")
    }

    override def using(s: String) : Unit = {
      writer.write(s + "\n")
    }

    override def using(values: Map[String, (String, String)], s: String) : Unit = {
      writer.write(s + "&&" + write(values) + "\n")
    }
  }

  object requestResponse extends HandlerImpl {
    override def handle(data: String, meta: String) : Handler = {
      writer.write("rr%%" + data + "%%" + meta + "%%")
      return this
    }
  }

  object requestStream extends HandlerImpl {
    override def handle(data: String, meta: String) : Handler = {
      writer.write("rs%%" + data + "%%" + meta + "%%")
      return this
    }
  }

  object requestSubscription extends HandlerImpl {
    override def handle(data: String, meta: String) : Handler = {
      writer.write("sub%%" + data + "%%" + meta + "%%")
      return this
    }
  }

  object requestChannel extends ChannelHandler {
    override def handle(data: String, meta: String) : ChannelHandler = {
      writer.write("channel%%" + data + "%%" + meta + "%%")
      return this
    }
    override def asFollows(f: () => Unit) = {
      writer.write("{\n")
      f()
      writer.write("}\n")
    }
  }


  // Marble DSL


  abstract class Marble {
    def str : String
  }

  object pause extends Marble {
    var s : String = ""
    def apply(n : Int) : Marble = {
      for (i <- 0 until n) {
        s += "-"
      }
      return this
    }
    override def str : String = return s
  }

  object complete extends Marble {
    def apply : Marble = return this
    override def str : String = return "|"
  }

  object error extends Marble {
    def apply : Marble = return this
    override def str : String = return "#"
  }

  object emit extends Marble {
    var ch : Queue[Char] = Queue.empty
    def apply(c: Char) : Marble = {
      ch = ch.enqueue(c)
      return this
    }
    override def str : String = {
      val temp : (Char, Queue[Char]) = ch.dequeue
      val toReturn : Char = temp._1
      ch = temp._2
      return toReturn.toString
    }
  }

  object sub extends Marble {
    def apply : Marble = return this
    override def str : String = return "^"
  }

  object group extends Marble {
    var s : String = ""
    def apply(mar : Marble *) : Marble = {
      s += "("
      for (m <- mar) {
        s += m.str
      }
      s += ")"
      return this
    }
    override def str : String = return s
  }


  def end() : Unit = {
    println("ended")
    writer.close()
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


object servertest extends MarbleDSL {
  def main(args: Array[String]) {
    Tests.runTests(this, this.writer)
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
  def handleRequestSubscription() : Unit = {
    requestSubscription handle("a", "b") using("abcdefghijklmnop")
  }

  @Test
  def handleRequestChannel() : Unit = {
    requestChannel handle("a", "b") asFollows(() => {
      val s1 = channelSubscriber()
      respond("---x---")
      s1 request 1
      s1 awaitAtLeast(2, 1000)
      s1 assertReceivedCount 2
      s1 assertReceived List(("a", "b"), ("a", "a"))
      s1 request 5
      s1 awaitAtLeast(7, 1000)
      respond("a---b---c")
      s1 request 5
      s1 awaitAtLeast(12, 1000) // there's an implicit request 1 in the beginning
      respond("d--e---f-")
      respond("|")
      s1 awaitTerminal()
      s1 assertCompleted()
    })
  }

  @Test
  def handleRequestChannel2() : Unit = {
    requestChannel handle("c", "d") asFollows(() => {
      val s1 = channelSubscriber()
      respond("---x---")
      s1 request 1
      s1 awaitAtLeast(2, 1000)
      s1 assertReceivedCount 2
      s1 assertReceived List(("c", "d"), ("a", "a"))
      s1 request 5
      s1 awaitAtLeast(7, 1000)
      respond("a---b---c")
      s1 request 5
      s1 awaitAtLeast(12, 1000) // there's an implicit request 1 in the beginning
      respond("d--e---f-")
      respond("|")
      s1 awaitTerminal()
      s1 assertCompleted()
      s1 awaitNoAdditionalEvents 1000
    })
  }

}