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


class DSLTestSubscriber(writer : PrintWriter, initData: String, initMeta: String, kind: String)
  extends Subscriber[Payload] with Subscription {

  implicit val formats = Serialization.formats(NoTypeHints)

  private var id: UUID = null
  this.id = UUID.randomUUID

  // decide what type of subscriber to write down
  if (kind.equals("")) writer.write("") // write nothing
  else writer.write("subscribe%%" + kind + "%%" + this.getID + "%%" + initData + "%%" + initMeta + "\n")

  def getID: String = return this.id.toString

  override def onSubscribe(s: Subscription) : Unit =  {}

  override def onNext(t: Payload) : Unit = {}

  override def onError(t: Throwable) : Unit = {}

  override def onComplete() : Unit = {}

  override def request(n: Long) : Unit = writer.write("request%%" + n + "%%" + this.id + "\n")

  override def cancel() : Unit = writer.write("cancel%%" + this.id + "\n")


  // assertion tests

  def assertNoErrors() : Unit = writer.write("assert%%no_error%%" + this.id + "\n")

  def assertError() : Unit = writer.write("assert%%error%%" + this.id + "\n")

  def assertReceived(lst: List[(String, String)]) : Unit =
    writer.write("assert%%received%%" + this.id + "%%" + printList(lst) + "\n")

  def assertReceivedCount(n: Long) : Unit = writer.write("assert%%received_n%%" + this.id + "%%" + n + "\n")

  def assertReceivedAtLeast(n: Long) : Unit = writer.write("assert%%received_at_least%%" + this.id + "%%" + n + "\n")

  def assertNoValues() : Unit = writer.write("assert%%no_values%%" + this.id + "\n")

  def assertCompleted() : Unit = writer.write("assert%%completed%%" + this.id + "\n")

  def assertNotCompleted() : Unit = writer.write("assert%%no_completed%%" + this.id + "\n")

  def assertCanceled() : Unit = writer.write("assert%%canceled%%" + this.id + "\n")

  // await

  def awaitTerminal() : Unit = writer.write("await%%terminal%%" + this.id + "\n")

  def awaitOnNext() : Unit = writer.write("await%%onNext%%" + this.id + "\n")

  def awaitAtLeast(n: Long, t : Long) = writer.write("await%%atLeast%%" + this.id + "%%" + n + "%%" + t + "\n")

  def awaitNoAdditionalEvents(t: Long) = writer.write("await%%no_events%%" + this.id + "%%" + t + "\n")

  def take(n: Long) : Unit = writer.write("take%%" + n + "%%" + this.id + "\n")

  // internal functions

  private def printList(lst: List[(String, String)]) : String = lst.map(a => a._1 + "," + a._2).mkString("&&")

  private def writechannel(marble: Map[(String, String), String]) : String = {
    marble.keys.map(a => a._1 + "@" + a._2 + ":" + marble.get(a).get).mkString(",")
  }
}