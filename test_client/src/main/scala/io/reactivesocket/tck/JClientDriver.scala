package io.reactivesocket.tck

import java.io.{BufferedReader, FileReader}
import java.util.concurrent.{CountDownLatch, TimeUnit}

import scala.collection.mutable
import io.reactivesocket.{DefaultReactiveSocket, Payload, ReactiveSocket}
import org.reactivestreams.{Publisher, Subscriber, Subscription}

import scala.collection.JavaConverters._

class ClientDriver(path: String) {

  val ANSI_RESET: String = "\u001B[0m"
  val ANSI_BLACK: String = "\u001B[30m"
  val ANSI_RED: String = "\u001B[31m"
  val ANSI_GREEN: String = "\u001B[32m"
  val ANSI_YELLOW: String = "\u001B[33m"
  val ANSI_BLUE: String = "\u001B[34m"
  val ANSI_PURPLE: String = "\u001B[35m"
  val ANSI_CYAN: String = "\u001B[36m"
  val ANSI_WHITE: String = "\u001B[37m"

  private val reader: BufferedReader = new BufferedReader(new FileReader(path))
  private val payloadSubscribers = new mutable.HashMap[String, TestSubscriber[Payload]]
  private val fnfSubscribers = new mutable.HashMap[String, TestSubscriber[Void]]
  private val idToType = new mutable.HashMap[String, String]

  def runTests() : Unit = {
    var tests : List[List[String]] = List()
    var test : List[String] = List()
    var line = reader.readLine
    while (line != null) {
      line match {
          // test separator
        case "!" => {
          tests = tests :+ test
          test = List()
        }
        case _ => {
          test = test :+ line
        }
      }
      line = reader.readLine
    }
    tests = tests :+ test
    tests = tests.tail // the first list is always empty
    for (test <- tests) {
      val thread = new TestThread(test)
      thread.start()
      thread.join()
    }
  }

  def parse(test: List[String]) : Option[Boolean] = {
    var id : List[String] = List()
    val iter = test.iterator
    while (iter.hasNext) {
      val line = iter.next
      val args : Array[String] = line.split("%%")

      // the over arching cases, each case will have their own sub cases
      args(0) match {
        // in the case of subscribe, we want to create the publisher containing the initial payload and fire it
        // right away. The publisher is pretty trivial, and we'd only want to store the subscriber
        case "subscribe" => {
          handle_subscribe(args)
          id = id :+ args(2)
        }

        case "channel" => handle_channel(args, iter)

        case "await" => {
          args(1) match {
            case "terminal" => handle_await_terminal(args)
            case "atLeast" => println("awaiting"); handle_await_at_least(args)
            case "no_events" => handle_await_no_events(args)
          }
        }

        // assert will have an insane number of cases
        case "assert" => {
          args(1) match {
            case "no_error" => handle_no_error(args)
            case "error" => handle_error(args)
            case "received" => handle_received(args)
            case "received_n" => handle_received_n(args)
            case "received_at_least" => handle_received_at_least(args)
            case "completed" => handle_completed(args)
            case "no_completed" => handle_no_completed(args)
            case "canceled" => handle_canceled(args)
          }
        }

        // this tells the subscriber to request some number, and then canceled the subscription
        case "take" => handle_take(args)
        case "request" => handle_request(args)
        case "cancel" => handle_cancel(args)
        case "EOF" =>
      }
    }

    // makes sure that each subscriber passed
    if (id.length > 0) return Some(id.foldRight(true) {(a, b) =>
      var x = false;
      if (payloadSubscribers.get(a).isDefined) x = payloadSubscribers.get(a).get.hasPassed
      else x = fnfSubscribers.get(a).get.hasPassed
      x && b
    })
    else return None

  }


  private def handle_subscribe(args : Array[String]) : Unit = {
    args(1) match {
      case "rr" => { // all three of these can use the same subscriber
        val sub : TestSubscriber[Payload] = new TestSubscriber[Payload](0 : Long)
        payloadSubscribers.put(args(2), sub)
        idToType.put(args(2), args(1)) // keeps track of the type of subscriber this id is referring to
        val client = JavaTCPClient.createClient(); // create a fresh client
        val pub : Publisher[Payload] = client.requestResponse(new PayloadImpl(args(3), args(4)))
        pub.subscribe(sub) // this code is very eager
      }
      case "rs" => {
        val sub : TestSubscriber[Payload] = new TestSubscriber[Payload](0 : Long)
        payloadSubscribers.put(args(2), sub)
        idToType.put(args(2), args(1)) // keeps track of the type of subscriber this id is referring to
        val client = JavaTCPClient.createClient();
        val pub : Publisher[Payload] = client.requestStream(new PayloadImpl(args(3), args(4)))
        pub.subscribe(sub) // this code is very eager
        println("subscribed stream")
      }
      case "sub" => {
        val sub : TestSubscriber[Payload] = new TestSubscriber[Payload](0 : Long)
        payloadSubscribers.put(args(2), sub)
        idToType.put(args(2), args(1)) // keeps track of the type of subscriber this id is referring to
        val client = JavaTCPClient.createClient();
        val pub : Publisher[Payload] = client.requestSubscription(new PayloadImpl(args(3), args(4)))
        pub.subscribe(sub) // this code is very eager
      }
      case "fnf" => {
        val sub : TestSubscriber[Void] = new TestSubscriber[Void]
        fnfSubscribers.put(args(2), sub)
        idToType.put(args(2), args(1)) // keeps track of the type of subscriber this id is referring to
        val client = JavaTCPClient.createClient();
        val pub : Publisher[Void] = client.fireAndForget(new PayloadImpl(args(3), args(4)))
        pub.subscribe(sub)
      }
    }
  }

  private def handle_channel(args: Array[String], iter: Iterator[String]) : Unit = {
    var commands: List[String] = List()
    var line = iter.next()
    // this gets the commands that will run this channel
    while (!line.equals("}")) {
      commands = commands :+ line
      line = iter.next()
    }
    // this is the initial payload
    val initpayload = new PayloadImpl(args(1), args(2))

    // this is the subscriber that will request data from the SERVER, like all the other test subscribers here
    // the client will do asserts on this subscriber
    val testsub : TestSubscriber[Payload] = new TestSubscriber[Payload](1 : Long)
    var superpc : ParseChannel = null
    val c = new CountDownLatch(1)
    // this creates the publisher that the SERVER will subscribe to with their subscriber
    // We want to give the subscriber a subscription that the CLIENT defines, that will send data to the server
    val client = JavaTCPClient.createClient(); // we create a fresh client to use
    val pub: Publisher[Payload] = client.requestChannel(new Publisher[Payload] {
      override def subscribe(s: Subscriber[_ >: Payload]): Unit = {
        val pm : ParseMarble = new ParseMarble(s)
        val ts : TestSubscription = new TestSubscription(pm, initpayload, s)
        s.onSubscribe(ts)
        val pc : ParseChannel = new ParseChannel(commands.asJava, testsub, pm)
        val pct = new ParseChannelThread(pc)
        pct.start
        pct.join
      }
    })
    pub.subscribe(testsub)
  }

  private def handle_await_terminal(args : Array[String]) : Unit = {
    val id = args(2)
    if (!idToType.get(id).isDefined) {
      println("could not find subscriber with given id")
    } else {
      if (idToType.get(id).get.equals("fnf")) {
        // retrieve the subscriber from the fnf map
        val sub = fnfSubscribers.get(id).get
        sub.awaitTerminalEvent // wait for a terminal event
      } else {
        // retrieve the subscriber from the payload map
        val sub = payloadSubscribers.get(id).get
        sub.awaitTerminalEvent
      }
    }
  }

  private def handle_await_at_least(args: Array[String]) : Unit = {
    val id = args(2)
    val sub = payloadSubscribers.get(id).get
    sub.awaitAtLeast(args(3).toLong, args(4).toLong, TimeUnit.MILLISECONDS)
  }

  private def handle_await_no_events(args: Array[String]) : Unit = {
    val id = args(2)
    val sub = payloadSubscribers.get(id).get
    sub.awaitNoEvents(args(3).toLong)
  }

  private def handle_no_error(args: Array[String]) : Unit = {
    val id = args(2)
    if (idToType.get(id).get.equals("fnf")) {
      // retrieve the subscriber from the fnf map
      val sub = fnfSubscribers.get(id).get
      sub.assertNoErrors
    } else {
      // retrieve the subscriber from the payload map
      val sub = payloadSubscribers.get(id).get
      sub.assertNoErrors
    }
  }

  private def handle_error(args: Array[String]) : Unit = {
    val id = args(2)
    if (idToType.get(id).get.equals("fnf")) {
      // retrieve the subscriber from the fnf map
      val sub = fnfSubscribers.get(id).get
      sub.assertError(new Throwable)
    } else {
      // retrieve the subscriber from the payload map
      val sub = payloadSubscribers.get(id).get
      sub.assertError(new Throwable)
    }
  }

  private def handle_completed(args: Array[String]) : Unit = {
    val id = args(2)
    if (idToType.get(id).get.equals("fnf")) {
      // retrieve the subscriber from the fnf map
      val sub = fnfSubscribers.get(id).get
      sub.assertComplete
    } else {
      // retrieve the subscriber from the payload map
      val sub = payloadSubscribers.get(id).get
      sub.assertComplete
    }
  }

  private def handle_no_completed(args: Array[String]) : Unit = {
    val id = args(2)
    if (idToType.get(id).get.equals("fnf")) {
      // retrieve the subscriber from the fnf map
      val sub = fnfSubscribers.get(id).get
      sub.assertNotComplete
    } else {
      // retrieve the subscriber from the payload map
      val sub = payloadSubscribers.get(id).get
      sub.assertNotComplete
    }
  }

  private def handle_request(args: Array[String]) : Unit = {
    val num = args(1).toLong
    val id = args(2)
    if (idToType.get(id).get.equals("fnf")) {
      // retrieve the subscriber from the fnf map
      val sub = fnfSubscribers.get(id).get
      sub.request(num)
    } else {
      // retrieve the subscriber from the payload map
      val sub = payloadSubscribers.get(id).get
      sub.request(num)
    }
  }

  private def handle_take(args: Array[String]) : Unit = {
    val id = args(2)
    val num = args(1).toLong
    val sub = payloadSubscribers.get(id).get
    sub.take(num);
  }

  // should only be called by payload subscribers
  private def handle_received(args: Array[String]) : Unit = {
    val id = args(2)
    val sub = payloadSubscribers.get(id).get
    val values = args(3).split("&&")
    if (values.length == 1) {
      val temp = values(0).split(",")
      sub.assertValue((temp(0), temp(1)))
    } else if (values.length > 1) {
      val list: Array[(String, String)] = values.map(a => a.split(",")).map(b => (b(0), b(1)))
      sub.assertValues(list)
    }
  }

  private def handle_received_n(args : Array[String]) : Unit = {
    val id = args(2)
    val sub = payloadSubscribers.get(id).get
    sub.assertValueCount(args(3).toInt)
  }

  private def handle_received_at_least(args : Array[String]) : Unit = {
    val id = args(2)
    val sub = payloadSubscribers.get(id).get
    sub.assertReceivedAtLeast(args(3).toInt)
  }

  private def handle_cancel(args: Array[String]) : Unit = {
    val id = args(1)
    val sub = payloadSubscribers.get(id).get
    sub.cancel()
  }

  private def handle_canceled(args: Array[String]) : Unit = {
    val id = args(2)
    val sub = payloadSubscribers.get(id).get
    sub.isCancelled
  }

  /**
    * A class that calls parse given a single test
    */
  private class TestThread(test: List[String]) extends Runnable {
    private val t : Thread = new Thread(this)
    override def run() : Unit = {
      var name : String = ""
      if (test.head.startsWith("name")) {
        name = test.head.split("%%")(1)
        println("Starting test " + name)
        val finish = parse(test.tail)
        if (finish.isEmpty) return
        if (parse(test.tail).get) println(ANSI_GREEN + name + " passed" + ANSI_RESET)
        else println(ANSI_RED + name + " failed" + ANSI_RESET)
      } else {
        println("Starting test")
        val finish = parse(test)
        if (finish.isEmpty) return
        if (parse(test).get) println(ANSI_GREEN + "Test passed" + ANSI_RESET)
        else println(ANSI_RED + "Test failed" + ANSI_RESET)
      }

    }

    def start() : Unit = t.start
    def join() : Unit = t.join()
  }

}

private class TestSubscription(pm: ParseMarble, initpayload: Payload, sub: Subscriber[_ >: Payload]) extends Subscription {
  var firstRequest = true

  def cancel {
    pm.cancel
  }

  def request(n: Long) {
    var m = n;
    if (firstRequest) {
      sub.onNext(initpayload)
      firstRequest = false
      m = m - 1
    }
    pm.request(m)
  }
}


