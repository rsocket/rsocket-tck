package io.reactivesocket.tck

import java.io.PrintWriter
import java.util.UUID

import io.reactivesocket.Payload
import org.reactivestreams.{Subscriber, Subscription}

import org.json4s._
import org.json4s.native.Serialization._
import org.json4s.native.Serialization


class DSLTestSubscriber(writer : PrintWriter, argMap: Map[String, (String, String)], initData: String,
                        initMeta: String, kind: String, marble: Map[(String, String), String])
  extends Subscriber[Payload] with Subscription {

  implicit val formats = Serialization.formats(NoTypeHints)

  // constructor for requestresponse, requeststream, firenforget, subscription
  def this(writer : PrintWriter, initData: String, initMeta: String, kind: String) = {
    this(writer, null, initData, initMeta, kind, Map())
  }


  //TODO: will have to extend channel stuff based on what we decide for specifications
  // constructor for channel without argmap
  def this(writer: PrintWriter, marble: Map[(String, String), String]) {
    this(writer, null, "", "", "channel", marble)
  }

  // constructor for channel with argmap
  def this(writer: PrintWriter, argMap: Map[String, (String, String)], marble: Map[(String, String), String]) = {
    this(writer, argMap, "", "", "channel", marble)
  }

  private var id: UUID = null
  this.id = UUID.randomUUID

  // decide what type of subscriber to write down
  if (kind.equals("channel") && argMap == null) writer.write("subscribe%%channel%%" + this.getID + "%%"
    + writechannel(marble) + "\n")
  else if(argMap != null) writer.write("subscribe%%channel%%" + this.getID + "%%" + writechannel(marble) + "&&" + write(argMap)
    + "\n")
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

  def assertNotTerminated() : Unit = writer.write("assert%%no_terminal%%" + this.id + "\n")

  def assertSubscribed() : Unit = writer.write("assert%%subscribed%%" + this.id + "\n")

  def assertNotSubscribed() : Unit = writer.write("assert%%no_subscribed%%" + this.id + "\n")

  def assertCanceled() : Unit = writer.write("assert%%canceled%%" + this.id + "\n")

  // await

  def awaitTerminal() : Unit = writer.write("await%%terminal%%" + this.id + "\n")

  def awaitOnNext() : Unit = writer.write("await%%onNext%%" + this.id + "\n")

  def awaitAtMost(n: Long, t : Long) = writer.write("await%%atMost%%" + this.id + "%%" + n + "%%" + t + "\n")

  def take(n: Long) : Unit = writer.write("take%%" + n + "%%" + this.id + "\n")

  // internal functions

  private def printList(lst: List[(String, String)]) : String = lst.map(a => a._1 + "," + a._2).mkString("&&")

  private def writechannel(marble: Map[(String, String), String]) : String = {
    "+" + marble.keys.map(a => a._1 + "@" + a._2 + ":" + marble.get(a).get).mkString(",")
  }
}