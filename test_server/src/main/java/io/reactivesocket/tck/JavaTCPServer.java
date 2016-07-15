package io.reactivesocket.tck;

import io.reactivesocket.transport.tcp.server.TcpReactiveSocketServer;


public class JavaTCPServer {

    public static void main(String[] args) {

        String file = "servertest$.txt";

        if (args.length > 0) {
            file = args[0];
        }

        JServerDriver jsd =
                new JServerDriver(file);

        TcpReactiveSocketServer.create(4567)
                .start((setupPayload, reactiveSocket) -> {
                    // create request handler
                    return jsd.parse();
                }).awaitShutdown();


    }

}
