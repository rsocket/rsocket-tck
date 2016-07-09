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
        String target = "tcp://localhost:4567/rs";
        URI uri = new URI(target);

        DuplexConnection duplexConnection = buildConnection(uri);

        ReactiveSocket client = DefaultReactiveSocket
                .fromClientConnection(duplexConnection, ConnectionSetupPayload.create("UTF-8", "UTF-8"),
                        t -> t.printStackTrace());

        client.startAndWait();


        // we pass in our reactive socket here to the test suite
        String file = "clienttest$.txt";
        if (args.length > 0) {
            file = args[0];
        }

        ClientDriver jd = new ClientDriver(client, file);
        jd.runTests();

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

}
