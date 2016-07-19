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

object servertest extends MarbleDSL {
  def main(args: Array[String]) {
    Tests.runTests(this, this.writer)
  }

  @Test
  def handleEcho() : Unit = {
    // not really a test... more like set up a behavior
    requestEchoChannel handle("e", "f")
  }

  @Test
  def handleRequestResponse() : Unit = {
    requestResponse handle("a", "b") using(Map("x" -> ("hello", "goodbye")), pause(3), emit('x'),
      pause(4), pause(5), complete)

    requestResponse handle("c", "d") using(Map("x" -> ("ding", "dong")), pause(10), emit('x'),
      pause(10), complete)

    requestResponse handle("e", "f") using(pause(10), error)

    requestResponse handle("g", "h") using("-")
  }

  @Test
  def handleRequestStream() : Unit = {
    requestStream handle("a", "b") using(Map("a" -> ("a", "b"), "b" -> ("c", "d"), "c" -> ("e", "f")),
      "---a-----b-----c-----d--e--f---|")
    requestStream handle("c", "d") using(Map("a" -> ("a", "b"), "b" -> ("c", "d"), "c" -> ("e", "f")),
      "---a-----b-----c-----d--e--f---|")
  }

  @Test
  def handleRequestSubscription() : Unit = {
    requestSubscription handle("a", "b") using("abcdefghijklmnop")
  }

  @Test
  def handleRequestChannel() : Unit = {
    requestChannel handle("a", "b") asFollows(() => {
      val s1 = channelSubscriber()
      respond("---x---")
      s1 request 1
      s1 awaitAtLeast(2, 1000)
      s1 assertReceivedCount 2
      s1 assertReceived List(("a", "b"), ("a", "a"))
      s1 request 5
      s1 awaitAtLeast(7, 1000)
      respond("a---b---c")
      s1 request 5
      s1 awaitAtLeast(12, 1000) // there's an implicit request 1 in the beginning
      respond("d--e---f-")
      respond("|")
      s1 awaitTerminal()
      s1 assertCompleted()
    })
  }

  @Test
  def handleRequestChannel2() : Unit = {
    requestChannel handle("c", "d") asFollows(() => {
      val s1 = channelSubscriber()
      respond("---x---")
      s1 request 1
      s1 awaitAtLeast(2, 1000)
      s1 assertReceivedCount 2
      s1 assertReceived List(("c", "d"), ("a", "a"))
      s1 request 5
      s1 awaitAtLeast(7, 1000)
      respond("a---b---c")
      s1 request 5
      s1 awaitAtLeast(12, 1000) // there's an implicit request 1 in the beginning
      respond("d--e---f-")
      respond("|")
      s1 awaitTerminal()
      s1 assertCompleted()
      s1 awaitNoAdditionalEvents 1000
    })
  }

}