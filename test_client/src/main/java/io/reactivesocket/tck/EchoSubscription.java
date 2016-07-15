package io.reactivesocket.tck;

import io.reactivesocket.Payload;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import scala.Tuple2;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class EchoSubscription implements Subscription {

    private Queue<Tuple<String, String>> q;
    private long numSent = 0;
    private long numRequested = 0;
    private Subscriber<? super Payload> sub;
    private boolean cancelled = false;

    public EchoSubscription(Subscriber<? super Payload> sub) {
        q = new ConcurrentLinkedQueue<>();
        this.sub = sub;
    }

    public void add(Tuple<String, String> payload) {
        q.add(payload);
        if (numSent < numRequested) request(0);
    }

    public void add(Tuple2<String, String> payload) {
        q.add(new Tuple<>(payload._1, payload._2));
        if (numSent < numRequested) request(0);
    }

    @Override
    public synchronized void request(long n) {
        numRequested += n;
        while (numSent < numRequested && !q.isEmpty() && !cancelled) {
            Tuple<String, String> tup = q.poll();
            sub.onNext(new PayloadImpl(tup.getK(), tup.getV()));
            numSent++;
        }
    }

    @Override
    public void cancel() {
        cancelled = true;
    }
}
