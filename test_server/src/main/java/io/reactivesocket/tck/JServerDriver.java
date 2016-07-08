package io.reactivesocket.tck;

import io.reactivesocket.Payload;
import io.reactivesocket.RequestHandler;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;



public class JServerDriver {

    private String path;

    // these map initial payload -> marble, which dictates the behavior of the server
    private Map<Tuple<String, String>, String> requestResponseMarbles;
    private Map<Tuple<String, String>, String> requestStreamMarbles;
    private Map<Tuple<String, String>, String> requestSubscriptionMarbles;
    // channel doesn't have an initial payload, but maybe the first payload sent can be viewed as the "initial"
    private Map<Tuple<String, String>, String> requestChannelMarbles;
    private BufferedReader reader;
    // will implement channel later

    public JServerDriver(String path) {
        this.path = path;
        requestResponseMarbles = new HashMap<>();
        requestStreamMarbles = new HashMap<>();
        requestSubscriptionMarbles = new HashMap<>();
        requestChannelMarbles = new HashMap<>();
        try {
            reader = new BufferedReader(new FileReader(path));
        } catch (Exception e) {
            System.out.println("File not found");
        }
    }

    // this parses the text file and primes the marble diagram
    public RequestHandler parse() {
        try {
            String line = reader.readLine();
            while (line != null) {
                String[] args = line.split("%%");
                switch (args[0]) {
                    case "rr":
                        // put the request response marble in the hash table
                        requestResponseMarbles.put(new Tuple<>(args[1], args[2]), args[3]);
                        break;
                    case "rs":
                        requestStreamMarbles.put(new Tuple<>(args[1], args[2]), args[3]);
                        break;
                    case "sub":
                        requestSubscriptionMarbles.put(new Tuple<>(args[1], args[2]), args[3]);
                    case "channel":
                        requestChannelMarbles.put(new Tuple<>(args[1], args[2]), args[3]);
                    default:
                        break;
                }

                line = reader.readLine();
            }

        } catch (Exception e) {
            System.out.println("reader exception");
        }

        return new RequestHandler.Builder().withFireAndForget(payload -> s -> {
            Tuple<String, String> initialPayload = new Tuple<>(PayloadImpl.byteToString(payload.getData()),
                    PayloadImpl.byteToString(payload.getMetadata()));
            System.out.println("firenforget " + initialPayload.getK() + " " + initialPayload.getV());
        }).withRequestResponse(payload -> s -> {
            Tuple<String, String> initialPayload = new Tuple<>(PayloadImpl.byteToString(payload.getData()),
                    PayloadImpl.byteToString(payload.getMetadata()));
            String marble = requestResponseMarbles.get(initialPayload);
            System.out.println("requestresponse " + initialPayload.getK() + " " + initialPayload.getV());
            if (marble != null) {
                ParseMarble pm = new ParseMarble(marble, s);
                s.onSubscribe(new TestSubscription(pm));
            }
        }).withRequestStream(payload -> s -> {
            Tuple<String, String> initialPayload = new Tuple<>(PayloadImpl.byteToString(payload.getData()),
                    PayloadImpl.byteToString(payload.getMetadata()));
            String marble = requestStreamMarbles.get(initialPayload);
            System.out.println("Stream " + initialPayload.getK() + " " + initialPayload.getV());
            if (marble != null) {
                ParseMarble pm = new ParseMarble(marble, s);
                s.onSubscribe(new TestSubscription(pm));
            }
        }).withRequestSubscription(payload -> s -> {
            Tuple<String, String> initialPayload = new Tuple<>(PayloadImpl.byteToString(payload.getData()),
                    PayloadImpl.byteToString(payload.getMetadata()));
            String marble = requestSubscriptionMarbles.get(initialPayload);
            System.out.println("Subscription " + initialPayload.getK() + " " + initialPayload.getV());
            if (marble != null) {
                ParseMarble pm = new ParseMarble(marble, s);
                s.onSubscribe(new TestSubscription(pm));
            }
        }).withRequestChannel(payloadPublisher -> s -> { // design flaw

        }).build();
    }

    private class TestSubscription implements Subscription {
        private ParseMarble pm;
        public TestSubscription(ParseMarble pm) {
            this.pm = pm;
        }

        @Override
        public void cancel() {
            pm.cancel();
        }

        @Override
        public void request(long n) {
            pm.parse(n);
        }
    }

}
