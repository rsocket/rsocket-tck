package io.reactivesocket.tck

import java.io.{File, PrintWriter}

import scala.collection.immutable.Queue



class MarbleDSL {

  import org.json4s._
  import org.json4s.native.Serialization._
  import org.json4s.native.Serialization
  implicit val formats = Serialization.formats(NoTypeHints)

  val writer: PrintWriter = new PrintWriter(new File(this.getClass.getSimpleName + ".txt"))

  trait Handler {
    def handle(data: String, meta: String) : Handler
    def using(marble: Marble *)
    def using(values: Map[String, (String, String)], marble: Marble *)
    def using(e : MyError)
    def using(s : String) // for people who want to write the actual ascii
    def using(values: Map[String, (String, String)], s : String)
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

  object requestChannel extends HandlerImpl {
    override def handle(data: String, meta: String) : Handler = {
      writer.write("channel%%" + data + "%%" + meta + "%%")
      return this
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

}

object servertest extends MarbleDSL {
  def main(args: Array[String]) {
    requestChannel handle("a", "b") using("---a---b---c---d")
    requestChannel handle("c", "d") using("--e--f--g--")
    end
  }
}