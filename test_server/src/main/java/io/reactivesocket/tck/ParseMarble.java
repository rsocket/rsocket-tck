package io.reactivesocket.tck;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivesocket.Payload;
import org.reactivestreams.Subscriber;

import java.text.StringCharacterIterator;
import java.util.*;

// Every time request n is called, there should be a call to parse n in this class, which will handle making sure
// that the request is respected
// If things are canceled, they should also be called here
public class ParseMarble {

    private String marble;
    private Subscriber<? super Payload> s;
    private boolean cancelled = false;
    private StringCharacterIterator iter;
    private Map<String, Map<String, String>> argMap;

    public ParseMarble(String marble, Subscriber<? super Payload> s) {
        this.marble = marble;
        this.s = s;
        iter = new StringCharacterIterator(marble);
        if (marble.contains("&&")) {
            String[] temp = marble.split("&&");
            ObjectMapper mapper = new ObjectMapper();
            try {
                argMap = mapper.readValue(temp[1], new TypeReference<Map<String, Map<String, String>>>() {
                });
            } catch (Exception e) {
                System.out.println("couldn't convert argmap");
            }
        }
    }

    // this parses the actual marble diagram and acts out the behavior
    // should be called upon triggering a handler
    public void parse(long n) {
        long numSent = 0;
        // if cancel has been called, don't do anything
        if (cancelled) return;

        String buffer = "";
        boolean grouped = false;

        while (iter.current() != StringCharacterIterator.DONE) {
            if (numSent >= n) return;
            char c = iter.current();
            switch (c) {
                case '-':
                    if (grouped) buffer += c;
                    else try {Thread.sleep(10);} catch (Exception e) {System.out.println("Interrupted");}
                    break;
                case '|':
                    if (grouped) buffer += c;
                    else s.onComplete();
                    break;
                case '#':
                    if (grouped) buffer += c;
                    else s.onError(new Throwable("error"));
                    break;
                case '(':
                    buffer = "";
                    grouped = true;
                    break;
                case ')':
                    parseMarble(buffer, s);
                    grouped = false;
                    buffer = "";
                    break;
                default:
                    if (argMap != null) {
                        // this is hacky, but we only expect one key and one value
                        Map<String, String> tempMap = argMap.get(c + "");
                        if (tempMap == null) {
                            s.onNext(new PayloadImpl(c + "", c + ""));
                            break;
                        }
                        List<String> key = new ArrayList<>(tempMap.keySet());
                        List<String> value = new ArrayList<>(tempMap.values());
                        s.onNext(new PayloadImpl(key.get(0), value.get(0)));
                    }
                    else s.onNext(new PayloadImpl(c + "", c + "")); // if value not mapped, just send as data and meta
                    numSent++;
                    break;
            }
            iter.next();
        }
    }

    private void parseMarble(String marble, Subscriber<? super Payload> s) {
        String buffer = "";
        boolean grouped = false;

        try {
            char[] commands = marble.toCharArray();
            Map<String, Map<String, String>> argMap = null;
            if (marble.contains("&&")) {
                String[] temp = marble.split("&&");
                commands = temp[0].toCharArray();
                ObjectMapper mapper = new ObjectMapper();
                argMap = mapper.readValue(temp[1], new TypeReference<Map<String, Map<String, String>>>(){});
            }
            for (char c : commands) {
                switch (c) {
                    case '-':
                        if (grouped) buffer += c;
                        else Thread.sleep(10);
                        break;
                    case '|':
                        if (grouped) buffer += c;
                        else s.onComplete();
                        break;
                    case '#':
                        if (grouped) buffer += c;
                        else s.onError(new Throwable("error"));
                        break;
                    case '(':
                        buffer = "";
                        grouped = true;
                        break;
                    case ')':
                        parseMarble(buffer, s); // recursively call parse marble to parse this grouping
                        grouped = false;
                        buffer = "";
                        break;
                    default:
                        if (argMap != null) {
                            // this is hacky, but we only expect one key and one value
                            Map<String, String> tempMap = argMap.get(c + "");
                            List<String> key = new ArrayList<>(tempMap.keySet());
                            List<String> value = new ArrayList<>(tempMap.values());
                            s.onNext(new PayloadImpl(key.get(0), value.get(0)));
                        }
                        else s.onNext(new PayloadImpl(c + "", c + ""));
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // cancel says that values will eventually stop being sent, which means we can wait till we've processed the initial
    // batch before sending
    public void cancel() {
        cancelled = true;
    }

}
