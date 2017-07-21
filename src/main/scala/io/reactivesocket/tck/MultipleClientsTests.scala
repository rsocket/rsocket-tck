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

object clientmultipleclientstest extends RequesterDSL {
  def main(args: Array[String]) {
    RequesterReflection.runTests(this, this.writer)
  }


  // example for testing stream
  @Test
  def requestStreamSequential() : Unit = {
    val c1 = client()
    val c2 = client()
    val s1 = requestStream("a", "b", Some(c1))
    s1 request 3
    s1 awaitAtLeast(3)
    s1 assertReceived(List(("a", "b"), ("c", "d"), ("e", "f")))
    s1 assertNoErrors()
    s1 assertReceivedCount 3
    val s2 = requestStream("c", "d", Some(c2))
    s2 request 3
    s2 awaitAtLeast(3)
    s2 assertReceived(List(("a", "b"), ("c", "d"), ("e", "f")))
    s2 assertNoErrors()
    s2 assertReceivedCount 3
  }

  @Test
  def requestStreamConcurrent() : Unit = {
    val c1 = client()
    val c2 = client()
    val s1 = requestStream("a", "b", Some(c1))
    s1 request 3
    val s2 = requestStream("c", "d", Some(c2))
    s2 request 3
    s1 awaitAtLeast(3)
    s2 awaitAtLeast(3)
    s1 assertNoErrors()
    s1 assertReceivedCount 3
    s2 assertNoErrors()
    s2 assertReceivedCount 3
    s1 request 3
    s2 request 3
    s1 awaitAtLeast(6)
    s2 awaitAtLeast(6)
    s1 assertReceivedCount 6
    s2 assertReceivedCount 6
    s1 awaitTerminal()
    s2 awaitTerminal()
    s1 assertCompleted()
    s2 assertCompleted()

  }

  @Test
  def requestResponseSequential() : Unit = {
    val c1 = client()
    val c2 = client()
    val s1 = requestResponse("i", "j", Some(c1))
    val s2 = requestResponse("k", "l", Some(c1))
    s1 request 1
    val s3 = requestResponse("m", "n", Some(c1))
    s2 request 1
    s3 request 1
    s1 awaitAtLeast 1
    s2 awaitAtLeast 1
    s3 awaitAtLeast 1
    s1 assertReceived List(("homer", "simpson"))
    s2 assertReceived List(("bart", "simpson"))
    s3 assertReceived List(("seymour", "skinner"))

    val s4 = requestResponse("m", "n", Some(c2))
    val s5 = requestResponse("k", "l", Some(c2))
    s4 request 1
    val s6 = requestResponse("i", "j", Some(c2))
    s5 request 1
    s6 request 1
    s4 awaitAtLeast 1
    s5 awaitAtLeast 1
    s6 awaitAtLeast 1
    s6 assertReceived List(("homer", "simpson"))
    s5 assertReceived List(("bart", "simpson"))
    s4 assertReceived List(("seymour", "skinner"))
  }

  @Test
  def requestResponseConcurrent() : Unit = {
    val c1 = client()
    val c2 = client()
    val s11 = requestResponse("i", "j", Some(c1))
    val s21 = requestResponse("m", "n", Some(c1))
    s11 request 1
    s21 request 1
    val s12 = requestResponse("k", "l", Some(c1))
    val s22 = requestResponse("k", "l", Some(c2))
    s12 request 1
    s22 request 1
    s12 awaitAtLeast 1
    s22 awaitAtLeast 1

    s11 assertReceived List(("homer", "simpson"))
    s21 assertReceived List(("seymour", "skinner"))
    s12 assertReceived List(("bart", "simpson"))
    s22 assertReceived List(("bart", "simpson"))
  }
}

object servermultipleclientstest extends ResponderDSL {
  def main(args: Array[String]) {
    ResponderReflection.runTests(this, this.writer)
  }

  @Test
  def handleRequestStream() : Unit = {
    requestStream handle("a", "b") using(Map("a" -> ("a", "b"), "b" -> ("c", "d"), "c" -> ("e", "f")),
      "---a-----b-----c-----d--e--f---|")
    requestStream handle("c", "d") using(Map("a" -> ("a", "b"), "b" -> ("c", "d"), "c" -> ("e", "f")),
      "---a-----b-----c-----d--e--f---|")
  }

  @Test
  def handleRequestResponse() : Unit = {
    requestResponse handle("i", "j") using (Map("x" -> ("homer", "simpson")), "x-|")
    requestResponse handle("k", "l") using (Map("y" -> ("bart", "simpson")), "y-|")
    requestResponse handle("m", "n") using (Map("z" -> ("seymour", "skinner")), "z-|")
  }
}
