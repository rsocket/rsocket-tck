package io.reactivesocket.tck;

import io.reactivesocket.Payload;
import io.reactivesocket.RequestHandler;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import scala.Array;
import scala.Tuple2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class JServerDriver {

    private String path;

    // these map initial payload -> marble, which dictates the behavior of the server
    private Map<Tuple<String, String>, String> requestResponseMarbles;
    private Map<Tuple<String, String>, String> requestStreamMarbles;
    private Map<Tuple<String, String>, String> requestSubscriptionMarbles;
    // channel doesn't have an initial payload, but maybe the first payload sent can be viewed as the "initial"
    private Map<Tuple<String, String>, List<String>> requestChannelCommands;
    // first try to implement single channel subscriber
    private BufferedReader reader;
    // will implement channel later

    public JServerDriver(String path) {
        this.path = path;
        requestResponseMarbles = new HashMap<>();
        requestStreamMarbles = new HashMap<>();
        requestSubscriptionMarbles = new HashMap<>();
        requestChannelCommands = new HashMap<>();
        //channelSubscribers = new HashMap<>();
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
                        break;
                    case "channel":
                        handle_channel(args, reader);
                    default:
                        break;
                }

                line = reader.readLine();
            }


        } catch (Exception e) {
            e.printStackTrace();
            //System.out.println("reader exception");
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
                new ParseThread(pm).start();
                s.onSubscribe(new TestSubscription(pm));
            }
        }).withRequestStream(payload -> s -> {
            Tuple<String, String> initialPayload = new Tuple<>(PayloadImpl.byteToString(payload.getData()),
                    PayloadImpl.byteToString(payload.getMetadata()));
            String marble = requestStreamMarbles.get(initialPayload);
            System.out.println("Stream " + initialPayload.getK() + " " + initialPayload.getV());
            if (marble != null) {
                ParseMarble pm = new ParseMarble(marble, s);
                new ParseThread(pm).start();
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
            try {
                System.out.println("Channel");
                TestSubscriber<Payload> sub = new TestSubscriber<>();
                payloadPublisher.subscribe(sub);
                // want to get equivalent of "initial payload"
                //sub.request(1); // first request of server is implicit, so don't need to call request(1) here
                sub.awaitAtLeast(1, 1000, TimeUnit.MILLISECONDS);
                Tuple<String, String> initpayload = new Tuple<>(sub.getElement(0)._1, sub.getElement(0)._2);
                System.out.println(initpayload.getK() + " " + initpayload.getV());
                ParseMarble pm = new ParseMarble(s);
                s.onSubscribe(new TestSubscription(pm));
                // need special functionality for parseMarble to incrementally build marble
                ParseChannel pc = new ParseChannel(requestChannelCommands.get(initpayload), sub, pm);
                new ParseChannelThread(pc).start();
            } catch (Exception e) {
                System.out.println("Interrupted");
            }
        }).build();
    }

    /**
     * This handles the creation of a channel handler, it basically groups together all the lines of the channel
     * script and put it in a map for later access
     * @param args
     * @param reader
     * @throws IOException
     */
    private void handle_channel(String[] args, BufferedReader reader) throws IOException {
        Tuple<String, String> initialPayload = new Tuple<>(args[1], args[2]);
        String line = reader.readLine();
        List<String> commands = new ArrayList<>();
        while (!line.equals("}")) {
            commands.add(line);
            line = reader.readLine();
        }
        requestChannelCommands.put(initialPayload, commands);
    }

    /**
     * A trivial subscription used to interface with the ParseMarble object
     */
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
            pm.request(n);
        }
    }

}
