package io.reactivesocket.tck

import java.io.{BufferedReader, FileReader}
import java.util.concurrent.{CountDownLatch, TimeUnit}
import java.util.function.Consumer

import scala.collection.mutable
import io.reactivesocket.{DefaultReactiveSocket, Payload, ReactiveSocket}
import org.reactivestreams.{Publisher, Subscriber, Subscription}

import scala.collection.mutable.ListBuffer

class ClientDriver(client: ReactiveSocket, path: String) {

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
    println(tests)
    for (test <- tests) {
      val thread = new TestThread(test)
      thread.start()
      thread.join()
    }
  }

  def parse(test: List[String]) : Unit = {

    for (line <- test) {
      val args : Array[String] = line.split("%%")

      // the over arching cases, each case will have their own sub cases
      args(0) match {
        // in the case of subscribe, we want to create the publisher containing the initial payload and fire it
        // right away. The publisher is pretty trivial, and we'd only want to store the subscriber
        case "subscribe" => {
          handle_subscribe(args)
        }

        case "await" => {
          args(1) match {
            case "terminal" => {
              handle_await_terminal(args)
            }
            case "atMost" => {
              handle_await_at_most(args)
            }
            case "onNext" => {
              // we probably don't need this
            }
          }
        }

        // assert will have an insane number of cases
        case "assert" => {
          args(1) match {
            case "no_error" => {
              handle_no_error(args)
            }
            case "error" => {
              handle_error(args)
            }
            case "received" => {
              handle_received(args)
            }
            case "received_n" => {
              handle_received_n(args)
            }
            case "received_at_least" => {
              handle_received_at_least(args)
            }
            case "completed" => {
              handle_completed(args)
            }
            case "no_completed" => {
              handle_no_completed(args)
            }
            case "no_terminal" => {

            }
            case "subscribed" => {

            }
            case "no_subscribed" => {

            }
            case "canceled" => {
              handle_canceled(args)
            }
          }
        }

        // this tells the subscriber to request some number, and then canceled the subscription
        case "take" => {
          handle_take(args)
        }

        case "request" => {
          handle_request(args)
        }

        case "cancel" => {
          handle_cancel(args)
        }

        case "EOF" => {

        }
      }
    }

  }


  private def handle_subscribe(args : Array[String]) : Unit = {
    args(1) match {
      case "rr" => { // all three of these can use the same subscriber
        val sub : TestSubscriber[Payload] = new TestSubscriber[Payload](0 : Long)
        payloadSubscribers.put(args(2), sub)
        idToType.put(args(2), args(1)) // keeps track of the type of subscriber this id is referring to
        val pub : Publisher[Payload] = client.requestResponse(new PayloadImpl(args(3), args(4)))
        pub.subscribe(sub) // this code is very eager
      }
      case "rs" => {
        val sub : TestSubscriber[Payload] = new TestSubscriber[Payload](0 : Long)
        payloadSubscribers.put(args(2), sub)
        idToType.put(args(2), args(1)) // keeps track of the type of subscriber this id is referring to
        val pub : Publisher[Payload] = client.requestStream(new PayloadImpl(args(3), args(4)))
        pub.subscribe(sub) // this code is very eager
      }
      case "sub" => {
        val sub : TestSubscriber[Payload] = new TestSubscriber[Payload](0 : Long)
        payloadSubscribers.put(args(2), sub)
        idToType.put(args(2), args(1)) // keeps track of the type of subscriber this id is referring to
        val pub : Publisher[Payload] = client.requestSubscription(new PayloadImpl(args(3), args(4)))
        pub.subscribe(sub) // this code is very eager
      }
      case "fnf" => {
        val sub : TestSubscriber[Void] = new TestSubscriber[Void]
        fnfSubscribers.put(args(2), sub)
        idToType.put(args(2), args(1)) // keeps track of the type of subscriber this id is referring to
        val pub : Publisher[Void] = client.fireAndForget(new PayloadImpl(args(3), args(4)))
        pub.subscribe(sub)
      }
      case "channel" => {
        val sub : TestSubscriber[Payload] = new TestSubscriber[Payload](0 : Long);
        payloadSubscribers.put(args(2), sub)
        idToType.put(args(2), args(1)) // keeps track of the type of subscriber this id is referring to
        val pub : Publisher[Payload] = client.requestChannel(new Publisher[Payload] {
            override def subscribe(s: Subscriber[_ >: Payload]): Unit = {
              s.onSubscribe(new TestSubscription(new ParseMarble(args(3), s)))
            }
          })
        pub.subscribe(sub) // this code is very eager
      }
    }
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

  private def handle_await_at_most(args: Array[String]) : Unit = {
    val id = args(2)
    val sub = payloadSubscribers.get(id).get
    sub.awaitAtMost(args(3).toLong, args(4).toLong, TimeUnit.MILLISECONDS)
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
      // TODO: probably will have to modify testsubscriber.java
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
        parse(test.tail)
        println(name + " passed")
      } else {
        println("Starting test")
        parse(test)
        println("Test passed")
      }

    }

    def start() : Unit = t.start
    def join() : Unit = t.join()
  }

}

private class TestSubscription(var pm: ParseMarble) extends Subscription {
  def cancel {
    pm.cancel
  }

  def request(n: Long) {
    pm.parse(n)
  }
}

