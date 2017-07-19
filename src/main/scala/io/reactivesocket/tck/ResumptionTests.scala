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

object resumptionclienttest extends RequesterDSL {
  def main(args: Array[String]) {
    RequesterReflection.runTests(this, this.writer)
  }


  // example for testing stream
  @Test
  def streamResumptionTest1() : Unit = {
    val s1 = requestStream("a", "b")
    s1 request 3
    s1 awaitAtLeast(3)
    s1 assertReceived(List(("a", "b"), ("c", "d"), ("e", "f")))
    s1 disconnect()
    s1 request 3
    s1 resume()
    s1 awaitTerminal()
    s1 assertCompleted()
    s1 assertNoErrors()
    s1 assertReceivedCount 6
  }

  @Test
  def streamResumptionTest2() : Unit = {
    val s1 = requestStream("a", "b")
    s1 request 3
    s1 awaitAtLeast(3)
    s1 assertReceived(List(("a", "b"), ("c", "d"), ("e", "f")))
    s1 disconnect()
    s1 request 3
    s1 awaitNoAdditionalEvents 100
    s1 assertReceivedCount 3
  }

  @Test
  def streamResumptionTest3() : Unit = {
    val s1 = requestStream("a", "b")
    s1 request 3
    s1 awaitAtLeast(3)
    s1 assertReceived(List(("a", "b"), ("c", "d"), ("e", "f")))
    s1 disconnect()
    s1 request 1
    s1 resume()
    s1 request 2
    s1 awaitTerminal()
    s1 assertReceivedCount 6
  }

  @Test
  def streamResumptionTest4() : Unit = {
    val s1 = requestStream("a", "b")
    s1 request 3
    s1 awaitAtLeast(3)
    s1 assertReceived(List(("a", "b"), ("c", "d"), ("e", "f")))
    s1 disconnect()
    s1 resume()
    s1 request 3
    s1 awaitTerminal()
    s1 assertCompleted()
    s1 assertNoErrors()
    s1 assertReceivedCount 6
  }

}

object resumptionservertest extends ResponderDSL {
  def main(args: Array[String]) {
    ResponderReflection.runTests(this, this.writer)
  }

  @Test
  def handleRequestStream() : Unit = {
    requestStream handle("a", "b") using(Map("a" -> ("a", "b"), "b" -> ("c", "d"), "c" -> ("e", "f")),
      "---a-----b-----c-----d--e--f---|")
  }

}
