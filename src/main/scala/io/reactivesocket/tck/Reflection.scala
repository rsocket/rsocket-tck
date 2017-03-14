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
import java.nio.file.Files
import java.nio.file.Paths

object RequesterReflection extends RequesterDSL {
  def runTests(cls : Any, writer: PrintWriter) : Unit = {
    this.writer = writer;

    val methods = cls.getClass.getDeclaredMethods
    for (method <- methods) {
      if (method.getDeclaredAnnotations.length > 0 && method.getDeclaredAnnotations()(0).isInstanceOf[Test]) {
        val test : Test = method.getDeclaredAnnotations()(0).asInstanceOf[Test]
        begintest()
        nametest(method.getName)
        method.invoke(cls)
      }
    }
    end
    Files.deleteIfExists(Paths.get("RequesterReflection.txt"))
  }
}

object ResponderReflection extends ResponderDSL {
  def runTests(cls : Any, writer: PrintWriter) : Unit = {
    this.writer = writer;

    val methods = cls.getClass.getDeclaredMethods
    for (method <- methods) {
      if (method.getDeclaredAnnotations.length > 0 && method.getDeclaredAnnotations()(0).isInstanceOf[Test]) {
        method.invoke(cls)
      }
    }
    end
    Files.deleteIfExists(Paths.get("ResponderReflection.txt"))
  }
}
