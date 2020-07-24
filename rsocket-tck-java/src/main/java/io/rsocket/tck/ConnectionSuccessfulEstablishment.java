package io.rsocket.tck;

import io.cucumber.datatable.DataTable;
import io.cucumber.guice.ScenarioScoped;
import io.cucumber.java8.En;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.rsocket.core.RSocketConnector;
import io.rsocket.tck.frame.SetupFlags;
import io.rsocket.tck.frame.SetupFrame;
import io.rsocket.tck.frame.SetupFrameKt;
import io.rsocket.tck.frame.shared.*;
import io.rsocket.transport.netty.client.TcpClientTransport;
import reactor.netty.Connection;
import reactor.netty.DisposableServer;
import reactor.netty.tcp.TcpServer;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.assertj.core.api.Assertions.assertThat;

@ScenarioScoped
public class ConnectionSuccessfulEstablishment implements En {
    private static final int PORT = 8000;
    private static final String HOST = "localhost";

    private io.rsocket.RSocket clientConnection;
    private DisposableServer reactorServer;
    private Map<String, String> clientData;
    private SetupFrame expectedFrame;
    private final BlockingQueue<ByteBuf> queue = new LinkedBlockingQueue<>();
    private Connection connection;

    public ConnectionSuccessfulEstablishment() {
        Given("^server listening to a port$",
                () -> reactorServer = TcpServer.create()
                        .host(HOST)
                        .port(PORT)
                        .doOnConnection(connection -> {
                            connection.addHandlerLast(new LengthCodec());
                            connection.inbound().receive().subscribe(frame -> queue.offer(frame.retain()));
                            this.connection = connection;
                        }).bindNow());

        When("^client sends SETUP frame with$", (DataTable data) -> {
                Map<String, String> map = data.asMap(String.class, String.class);
            RSocketConnector clientConnector = io.rsocket.core.RSocketConnector.create()
                    .dataMimeType(map.get("mime-type-data"))
                .keepAlive(
                    Duration.ofMillis(Long.parseLong(map.get("keep-alive"))),
            Duration.ofMillis(Long.parseLong(map.get("max-life-time")))
                );

            clientConnection = clientConnector.connect(
                    TcpClientTransport.create(
                            HOST,
                            PORT
                    )
            ).block(Duration.ofSeconds(10L));
            clientData = map;
        });

        //TODO write binary  & hex parser
        // build ByteBuf and comper it with incoming from client
        And("^server requires the following SETUP frame content$", (DataTable data) -> {
            Map<String, String> map = data.asMap(String.class, String.class);
            String text = clientData.get("mime-type-data");
            expectedFrame = SetupFrameKt.create(
                    new FrameHeader<>(
                            Integer.parseInt(map.get("stream-id")),
                            new SetupFlags(false, false, false)
                    ),
                    VersionKt.Version(
                            Integer.parseInt(map.get("major-version")),
                            Integer.parseInt(map.get("minor-version"))
                    ),
                    KeepAliveKt.KeepAlive(
                            Integer.parseInt(clientData.get("keep-alive")),
                            Integer.parseInt(clientData.get("max-life-time"))
                    ),
                    new MimeType(text, (byte) ByteBufUtil.utf8Bytes(text))
            );
        });

        Then("^server does not close connection after$", () -> {
            assertThat(connection.isDisposed()).isFalse();
            assertThat(clientConnection).isNotNull();
            assertThat(clientConnection.isDisposed()).isFalse();

            ByteBuf frame = queue.poll();
            assertThat(frame).isNotNull();
            SetupFrame actualFrame = SetupFrameKt.asSetup(RawFrameKt.frame(frame));

            assertThat(actualFrame.getHeader().getStreamId()).isEqualTo(expectedFrame.getHeader().getStreamId());
            assertThat(actualFrame.getHeader().getFlags()).isEqualTo(expectedFrame.getHeader().getFlags());
            assertThat(actualFrame.getVersion()).isEqualTo(expectedFrame.getVersion());
            assertThat(actualFrame.getKeepAlive()).isEqualTo(expectedFrame.getKeepAlive());
            assertThat(actualFrame.getDataMimeType()).isEqualTo(expectedFrame.getDataMimeType());
        });

        After(
                _scenario -> {
                    reactorServer.dispose();
                    clientConnection.dispose();
                }
        );
    }
}
