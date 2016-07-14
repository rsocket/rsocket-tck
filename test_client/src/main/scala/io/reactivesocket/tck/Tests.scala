package io.reactivesocket.tck

import java.io.PrintWriter
import java.nio.file.Files
import java.nio.file.Paths

object Tests extends ClientDSL {
  def runTests(cls : Any, writer: PrintWriter) : Unit = {
    this.writer = writer;

    val methods = cls.getClass.getDeclaredMethods
    for (method <- methods) {
      if (method.getDeclaredAnnotations.length > 0 && method.getDeclaredAnnotations()(0).isInstanceOf[Test]) {
        begintest()
        nametest(method.getName)
        method.invoke(cls)
      }
    }
    end
    Files.deleteIfExists(Paths.get("Tests$.txt"))
  }
}
