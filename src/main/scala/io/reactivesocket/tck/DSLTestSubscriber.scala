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

import java.io.PrintWriter
import java.util.UUID

import io.reactivesocket.Payload
import org.reactivestreams.{Subscriber, Subscription}

import org.json4s._
import org.json4s.native.Serialization



class DSLTestSubscriber(writer : PrintWriter, initData: String, initMeta: String, kind: String,
  client: Option[DSLTestClient]) extends Subscriber[Payload] with Subscription {

  implicit val formats = Serialization.formats(NoTypeHints)

  private var id: UUID = null
  this.id = UUID.randomUUID

  private var clientID: Int =
  if (client.isDefined) client.get.getID
  else ClientIDGen.getNewID()

  // decide what type of subscriber to write down
  if (kind.equals("")) writer.write("") // write nothing
  else writer.write("c" + clientID + "%%" + "subscribe%%" + kind + "%%" + this.getID + "%%" + initData + "%%" +
    initMeta + "\n")

  def getID: String = return this.id.toString

  override def onSubscribe(s: Subscription) : Unit =  {}

  override def onNext(t: Payload) : Unit = {}

  override def onError(t: Throwable) : Unit = {}

  override def onComplete() : Unit = {}

  override def request(n: Long) : Unit = writer.write("c" + clientID + "%%" + "request%%" + n + "%%" + this.id + "\n")

  override def cancel() : Unit = writer.write("c" + clientID + "%%" + "cancel%%" + this.id + "\n")


  // assertion tests

  def assertNoErrors() : Unit = writer.write("c" + clientID + "%%" + "assert%%no_error%%" + this.id + "\n")

  def assertError() : Unit = writer.write("c" + clientID + "%%" + "assert%%error%%" + this.id + "\n")

  def assertReceived(lst: List[(String, String)]) : Unit =
    writer.write("c" + clientID + "%%" + "assert%%received%%" + this.id + "%%" + printList(lst) + "\n")

  def assertReceivedCount(n: Long) : Unit =
    writer.write("c" + clientID + "%%" + "assert%%received_n%%" + this.id + "%%" + n + "\n")

  def assertReceivedAtLeast(n: Long) : Unit =
    writer.write("c" + clientID + "%%" + "assert%%received_at_least%%" + this.id + "%%" + n + "\n")

  def assertCompleted() : Unit = writer.write("c" + clientID + "%%" + "assert%%completed%%" + this.id + "\n")

  def assertNotCompleted() : Unit = writer.write("c" + clientID + "%%" + "assert%%no_completed%%" + this.id + "\n")

  def assertCanceled() : Unit = writer.write("c" + clientID + "%%" + "assert%%canceled%%" + this.id + "\n")

  // await

  def awaitTerminal() : Unit = writer.write("c" + clientID + "%%" + "await%%terminal%%" + this.id + "\n")

  def awaitAtLeast(n: Long) =
    writer.write("c" + clientID + "%%" + "await%%atLeast%%" + this.id + "%%" + n + "%%" + 100 + "\n")

  def awaitNoAdditionalEvents(t: Long) =
    writer.write("c" + clientID + "%%" + "await%%no_events%%" + this.id + "%%" + t + "\n")

  def take(n: Long) : Unit = writer.write("c" + clientID + "%%" + "take%%" + n + "%%" + this.id + "\n")

  // internal functions

  private def printList(lst: List[(String, String)]) : String = lst.map(a => a._1 + "," + a._2).mkString("&&")
}
