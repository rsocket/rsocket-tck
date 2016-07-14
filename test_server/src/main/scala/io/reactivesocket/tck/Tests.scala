package io.reactivesocket.tck

import java.io.PrintWriter

object Tests extends MarbleDSL {
  def runTests(cls : Any, writer: PrintWriter) : Unit = {
    this.writer = writer;

    val methods = cls.getClass.getDeclaredMethods
    for (method <- methods) {
      if (method.getDeclaredAnnotations.length > 0 && method.getDeclaredAnnotations()(0).isInstanceOf[Test]) {
        method.invoke(cls)
      }
    }
    end
  }
}
