/*
 * Copyright 2016 Facebook, Inc.
 * <p>
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  <p>
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  <p>
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 *  an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations under the License.
 */

package io.reactivesocket.tck

import java.io.{File, PrintWriter}

import scala.collection.immutable.Queue

class ResponderDSL {

  import org.json4s._
  import org.json4s.native.Serialization
  import org.json4s.native.Serialization._
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
      this
    }
  }

  object requestStream extends HandlerImpl {
    override def handle(data: String, meta: String) : Handler = {
      writer.write("rs%%" + data + "%%" + meta + "%%")
      this
    }
  }

  object requestSubscription extends HandlerImpl {
    override def handle(data: String, meta: String) : Handler = {
      writer.write("sub%%" + data + "%%" + meta + "%%")
      this
    }
  }

  object requestChannel extends ChannelHandler {
    override def handle(data: String, meta: String) : ChannelHandler = {
      writer.write("channel%%" + data + "%%" + meta + "%%")
      this
    }
    override def asFollows(f: () => Unit) = {
      writer.write("{\n")
      f()
      writer.write("}\n")
    }
  }

  object requestEchoChannel {
    def handle(data: String, meta: String) : Unit = {
      writer.write("echochannel%%" + data + "%%" + meta + "\n")
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
      this
    }
    override def str : String = s
  }

  object complete extends Marble {
    def apply : Marble = return this
    override def str : String = "|"
  }

  object error extends Marble {
    def apply : Marble = return this
    override def str : String = "#"
  }

  object emit extends Marble {
    var ch : Queue[Char] = Queue.empty
    def apply(c: Char) : Marble = {
      ch = ch.enqueue(c)
      this
    }
    override def str : String = {
      val temp : (Char, Queue[Char]) = ch.dequeue
      val toReturn : Char = temp._1
      ch = temp._2
      toReturn.toString
    }
  }

  object sub extends Marble {
    def apply : Marble = return this
    override def str : String = "^"
  }

  object group extends Marble {
    var s : String = ""
    def apply(mar : Marble *) : Marble = {
      s += "("
      for (m <- mar) {
        s += m.str
      }
      s += ")"
      this
    }
    override def str : String = s
  }


  def end() : Unit = {
    println("ended")
    writer.close()
  }


  def channelSubscriber() : DSLTestSubscriber = {
    // we create a trivial subscriber because we don't need a "real" one, because we will already pass in a test
    // subscriber in the driver, as one should have already been created to get the initial payload from the client
    new DSLTestSubscriber(writer, "", "", "");
  }

  def respond(marble : String) : Unit = {
    writer.write("respond%%" + marble + "\n")
  }

}
