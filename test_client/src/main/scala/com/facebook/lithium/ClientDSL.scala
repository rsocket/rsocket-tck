package com.facebook.lithium

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

  def requestChannel(marble: String) : DSLTestSubscriber = {
    return new DSLTestSubscriber(writer, marble)
  }

  def requestChannel(argMap: Map[String, (String, String)], marble: String) : DSLTestSubscriber = {
    return new DSLTestSubscriber(writer, argMap, marble)
  }

  def end() : Unit = {
    println("ended")
    writer.write("EOF\n")
    writer.close()
  }

}

object clienttest extends ClientDSL {
  def main(args: Array[String]) {
    val s1 = requestChannel(Map("a" -> ("hello", "goodbye")), "---a---b----c--d--e--")
    end()
  }
}
