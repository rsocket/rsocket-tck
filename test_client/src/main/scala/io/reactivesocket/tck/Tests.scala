package io.reactivesocket.tck

object Tests {
  def runTests(cls : Any) : Unit = {
    
    val methods = cls.getClass.getDeclaredMethods.reverse
    for (method <- methods) {
      if (method.getDeclaredAnnotations.length > 0 && method.getDeclaredAnnotations()(0).isInstanceOf[Test]) {
        method.invoke(cls)
      }
    }
  }
}
