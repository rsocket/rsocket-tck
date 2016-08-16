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

class RequesterDSL {

  val filename = this.getClass.getSimpleName.reverse.substring(1).reverse + ".txt"

  if (!filename.equals("RequesterReflection.txt")) println("writing to " + filename)

  var writer: PrintWriter = new PrintWriter(new File(filename))

  def requestResponse(data: String, metadata: String) : DSLTestSubscriber =
    new DSLTestSubscriber(writer, data, metadata, "rr")

  def requestStream(data: String, metadata: String) : DSLTestSubscriber =
    new DSLTestSubscriber(writer, data, metadata, "rs")

  def firenForget(data: String, metadata: String) : DSLTestSubscriber =
    new DSLTestSubscriber(writer, data, metadata, "fnf")

  def requestSubscription(data: String, metadata: String) : DSLTestSubscriber =
    new DSLTestSubscriber(writer, data, metadata, "sub")

  def end() : Unit = {
    writer.write("EOF\n")
    writer.close()
  }

  def begintest() : Unit = {
    writer.write("!\n")
  }

  def nametest(name: String) : Unit = writer.write("name%%" + name + "\n")


  trait ChannelHandler {
    def using(data: String, meta: String) : ChannelHandler
    def asFollows(f: () => Unit): Unit
  }

  object requestChannel extends ChannelHandler {
    override def using(data: String, meta: String) : ChannelHandler = {
      writer.write("channel%%" + data + "%%" + meta + "%%")
      this
    }
    override def asFollows(f: () => Unit) = {
      writer.write("{\n")
      f()
      writer.write("}\n")
    }
  }

  object createEchoChannel {
    def using(data: String, meta: String) : Unit = writer.write("echochannel%%" + data + "%%" + meta + "\n")
  }

  def channelSubscriber() : DSLTestSubscriber = {
    // we create a trivial subscriber because we don't need a "real" one, because we will already pass in a test
    // subscriber in the driver, as one should have already been created to get the initial payload from the client
    return new DSLTestSubscriber(writer, "", "", "");
  }

  def respond(marble : String) : Unit = {
    writer.write("respond%%" + marble + "\n")
  }

  def pass() : Unit = writer.write("pass\n")

  def fail() : Unit = writer.write("fail\n")

}