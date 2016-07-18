
/*
 * Copyright 2016 Facebook, Inc.
 * <p>
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  <p>
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  <p>
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 *  an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations under the License.
 */

package io.reactivesocket.tck;

import io.netty.channel.nio.NioEventLoopGroup;
import io.reactivesocket.*;
import io.reactivesocket.netty.tcp.client.ClientTcpDuplexConnection;
import io.reactivesocket.netty.websocket.client.ClientWebSocketDuplexConnection;
import org.reactivestreams.Publisher;
import rx.RxReactiveStreams;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import static java.net.InetSocketAddress.createUnresolved;

// this client should parse the test cases we wrote and use them to
public class JavaTCPClient {

    public static void main(String[] args) throws MalformedURLException, URISyntaxException {
        // we pass in our reactive socket here to the test suite
        String file = "clienttest$.txt";
        if (args.length > 0) {
            file = args[0];
        }
        try {
            JavaClientDriver jd = new JavaClientDriver(file);
            jd.runTests();
        } catch (Exception e) {
            System.out.println("parse exception");
        }
    }

    private static DuplexConnection buildConnection(URI uri) {
        Publisher<? extends DuplexConnection> connection;
        if (uri.getScheme().equals("tcp")) {
            connection =
                    ClientTcpDuplexConnection.create(createUnresolved(uri.getHost(), uri.getPort()),
                            new NioEventLoopGroup());
        } else if (uri.getScheme().equals("ws")) {
            connection = ClientWebSocketDuplexConnection.create(uri, new NioEventLoopGroup());
        } else {
            throw new UnsupportedOperationException("uri unsupported: " + uri);
        }
        return RxReactiveStreams.toObservable(connection).toBlocking().single();
    }

    public static ReactiveSocket createClient() {
        try {
            String target = "tcp://localhost:4567/rs";
            URI uri = new URI(target);

            DuplexConnection duplexConnection = buildConnection(uri);

            ReactiveSocket client = DefaultReactiveSocket
                    .fromClientConnection(duplexConnection, ConnectionSetupPayload.create("UTF-8", "UTF-8"),
                            t -> t.printStackTrace());

            client.startAndWait();
            return client;
        } catch (Exception e) {
            System.out.println("Something went wrong");
        }
        return null;
    }

}
