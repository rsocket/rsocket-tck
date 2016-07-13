package io.reactivesocket.tck

private class ParseChannelThread(pc: ParseChannel) extends Runnable {
  val t = new Thread(this)

  def run : Unit = pc.parse
  def start : Unit = this.t.start
  def join : Unit = t.join()
}

/**
  * This class repeatedly calls parse(), so the sending of data is asynchronous from the requesting/asserting of data
  */
class ParseThread(parseMarble: ParseMarble) extends Runnable {
  private val t = new Thread(this)

  def start() : Unit = t.start()
  override def run() : Unit = parseMarble.parse()
}

/**
  * This thread is exclusively spawned to asynchronously call request(n)
  * request(n) is a synchronized method, so this thread will wait to be able to call it
  * This is done so IO does not block the main test thread
 *
  * @param n
  */
class RequestThread(n: Long, parseMarble: ParseMarble) extends Runnable {
  private val t = new Thread(this)

  def start() : Unit = t.start()
  override def run() : Unit = parseMarble.request(n)
}

/**
  * This is like the request thread, but used to add marble strings
 *
  * @param marble
  */
class AddThread(marble: String, parseMarble: ParseMarble) extends Runnable {
  private val t = new Thread(this)

  def start() : Unit = t.start()
  override def run() : Unit = parseMarble.add(marble)
}