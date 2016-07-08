package com.facebook.lithium

object RequestResponseTest extends MarbleDSL {
  def main(args: Array[String]) {
    requestResponse handle("a", "b") using(Map("x" -> ("hello", "goodbye")), pause(3), emit('x'),
      pause(4), pause(5), complete)
    requestResponse handle("c", "d") using(Map("x" -> ("ding", "dong")), pause(10), emit('x'),
      pause(10), complete)
    requestResponse handle("e", "f") using(pause(10), error)
    requestResponse handle("g", "h") using("-")
    end
  }
}
