package io.reactivesocket.tck

class Scratch {


  /*// requester

  val s1 = requestResponse("hello", "metadata")
  await s1 // wait until either onComplete or onError
  assert s1.onComplete
  assert s1.received "hello world" // the received value

  val s2 = requestResponse("helloError", "metadata")
  await s2 // wait until either onComplete or onError
    assert s2.onError
  assert error "failure!"


  val s2b = requestResponse("hang")
  s2b.cancel
  assert s2b cancelled
  assert !s2b.onError
  assert !s2b.onComplete
  assert didn't receive anythign   ...


  val s3 = requestSubscription("subscribeX");
  s3.take 3;
  assert s3.cancelled;
  assert !s3.onCompleted;
  assert !s3.onError;
  assert s3.received ("1", "2", "3");


  val s4 = requestStream("streamOf20")
  s4.requestN(5)
  await s4.atLeast(5) // how to do this well?
  assert s4.received (1, 2, 3, 4, 5) // should ONLY be those 5
  s4.request(50)
  await s4 // await terminal event
  assert s4   ... // no errors, received onComplete ... assert received all 20






  // responder
  requestResponse.handle("hello").with("hello world|")
  requestResponse.handle("helloError").with(new RuntimeException("failure!"))
  requestResponse.handle("hang").with("") // hangs, no onComplete, no onError


  // use ascii marble diagram to represent onNext over time ... each space == 100ms
  requestSubscription.handle("subscribeX").with(100, " 1    2  3     4   x"); // first arg is time between events


  requestStream.handle("streamOf20").with(10, "1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20")*/
}
