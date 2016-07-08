package io.reactivesocket.tck

object RequestSubscriptionTests extends MarbleDSL {
  def main(args: Array[String]) {
    requestStream handle("a", "b") using(Map("a" -> ("a", "b"), "b" -> ("c", "d"), "c" -> ("e", "f")),
      "---a-----b-----c-----d--e--f---|")
    requestStream handle("c", "e") using(Map("a" -> ("a", "b"), "b" -> ("c", "d"), "c" -> ("e", "f")),
      "---a-----b-----c-----d--e--f---|")
    requestSubscription handle("a", "b") using("abcdefghijklmnop")
    end
  }
}
