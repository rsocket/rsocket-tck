package io.reactivesocket.tck

import java.util.List
import java.util.concurrent.TimeUnit

import io.reactivesocket._

import scala.collection.JavaConversions._

/**
  * This class is exclusively used to parse channel commands on both the client and the server
  * @param commands a list of commands inside each channel "block"
  * @param sub the testsubscriber that receives and asserts data from the other side
  * @param parseMarble the parseMarble object that will send data to the other side
  */
class ParseChannel(commands: List[String], sub: TestSubscriber[Payload], parseMarble: ParseMarble) {

  val ANSI_RESET: String = "\u001B[0m"
  val ANSI_RED: String = "\u001B[31m"
  val ANSI_GREEN: String = "\u001B[32m"

  // spawns a thread that runs the parse method
  val parseThread = new ParseThread(parseMarble);
  parseThread.start()

  // parse through command list in order
  def parse() : Unit = {
    for (line <- commands) {
      val args: Array[String] = line.split("%%")
      args(0) match {

        case "respond" => handleResponse(args)

        case "await" => {
          args(1) match {
            case "terminal" => sub.awaitTerminalEvent
            case "atLeast" => println("await");sub.awaitAtLeast(args(3).toLong, args(4).toLong, TimeUnit.MILLISECONDS)
            case "no_events" => sub.awaitNoEvents(args(3).toLong)
          }
        }


        case "assert" => {
          args(1) match {
            case "no_error" => sub.assertNoErrors
            case "error" => sub.assertError(new Throwable)
            case "received" => handleReceived(args)
            case "received_n" => sub.assertValueCount(args(3).toInt)
            case "received_at_least" => sub.assertReceivedAtLeast(args(3).toInt)
            case "completed" => sub.assertComplete
            case "no_completed" => sub.assertNotComplete
            case "canceled" => sub.isCancelled
          }
        }

        case "take" => handleRequest(args)
        case "request" => sub.request(args(1).toLong); println("requesting " + args(1).toLong)
        case "cancel" => sub.cancel

      }
    }
    if (sub.hasPassed) println(ANSI_GREEN + "CHANNEL PASSED" + ANSI_RESET)
    else println(ANSI_RED + "CHANNEL FAILED" + ANSI_RESET)
  }

  private def handleResponse(args: Array[String]) = {
    println("responding")
    val addThread = new AddThread(args(1), parseMarble)
    addThread.start()
  }

  private def handleRequest(args: Array[String]) = {
    val requestThread = new RequestThread(args(1).toLong, parseMarble)
    requestThread.start()
  }

  // should only be called by payload subscribers
  private def handleReceived(args: Array[String]) : Unit = {
    val values = args(3).split("&&")
    if (values.length == 1) {
      val temp = values(0).split(",")
      sub.assertValue((temp(0), temp(1)))
    } else if (values.length > 1) {
      val list: Array[(String, String)] = values.map(a => a.split(",")).map(b => (b(0), b(1)))
      sub.assertValues(list)
    }
  }

}
