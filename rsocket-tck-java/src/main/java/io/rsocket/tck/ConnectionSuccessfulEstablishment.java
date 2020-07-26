package io.rsocket.tck;

import io.cucumber.datatable.DataTable;
import io.cucumber.guice.ScenarioScoped;
import io.cucumber.java8.En;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.rsocket.RSocket;
import io.rsocket.core.RSocketConnector;
import io.rsocket.frame.FrameLengthCodec;
import io.rsocket.tck.frame.SetupFlags;
import io.rsocket.tck.frame.SetupFrame;
import io.rsocket.tck.frame.SetupFrameKt;
import io.rsocket.tck.frame.shared.FrameHeader;
import io.rsocket.tck.frame.shared.KeepAliveKt;
import io.rsocket.tck.frame.shared.MimeType;
import io.rsocket.tck.frame.shared.RawFrameKt;
import io.rsocket.tck.frame.shared.VersionKt;
import io.rsocket.transport.netty.client.TcpClientTransport;
import reactor.netty.Connection;
import reactor.netty.DisposableServer;
import reactor.netty.tcp.TcpServer;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.assertj.core.api.Assertions.assertThat;

@ScenarioScoped
public class ConnectionSuccessfulEstablishment implements En {
    public static final int STREAM_ID = 0;
    public static final int FRAME_TYPE = 1;
    public static final int FLAGS = 2;
    public static final int MAJOR_VERSION = 3;
    public static final int MINOR_VERSION = 4;
    public static final int KEEP_ALIVE= 5;
    public static final int MAX_LIFE_TIME = 6;
    public static final int MIME_TYPE_METADATA_LENGTH = 7;
    public static final int MIME_TYPE_META_DATA = 8;
    public static final int MIME_TYPE_DATA_LENGTH = 9;
    public static final int MIME_TYPE_DATA = 10;

    private static final int PORT = 8000;
    private static final String HOST = "localhost";

    private RSocket clientConnection;
    private DisposableServer reactorServer;
    private Map<String, String> clientData;
    private SetupFrame expectedFrame;
    private final BlockingQueue<ByteBuf> queue = new LinkedBlockingQueue<>();
    private Connection connection;
    private List<List<String>> testData;

    public ConnectionSuccessfulEstablishment() {
        Given("^server listening to a port$",
                () -> reactorServer = TcpServer.create()
                        .host(HOST)
                        .port(PORT)
                        .doOnConnection(connection -> {
                            connection.addHandlerLast(new LengthCodec());
                            connection.inbound().receive().map(FrameLengthCodec::frame).subscribe(frame -> queue.offer(frame.retain()));
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
        Then("^server requires the following SETUP frame content$", (DataTable data) -> {
            testData = data.cells();
            String text = clientData.get("mime-type-data");
            expectedFrame = SetupFrameKt.create(
                    new FrameHeader<>(
                            Integer.parseInt(testData.get(STREAM_ID).get(1)),
                            new SetupFlags(false, false, false)
                    ),
                    VersionKt.Version(
                            Integer.parseInt(testData.get(MAJOR_VERSION).get(1)),
                            Integer.parseInt(testData.get(MINOR_VERSION).get(1))
                    ),
                    KeepAliveKt.KeepAlive(
                            Integer.parseInt(clientData.get("keep-alive")),
                            Integer.parseInt(clientData.get("max-life-time"))
                    ),
                    new MimeType(text, (byte) ByteBufUtil.utf8Bytes(text))
            );

            assertThat(connection.isDisposed()).isFalse();
            assertThat(clientConnection).isNotNull();
            assertThat(clientConnection.isDisposed()).isFalse();

            ByteBuf frame = queue.poll();
            assertThat(frame).isNotNull();
            assertBinaryContent(frame.slice());

            SetupFrame actualFrame = SetupFrameKt.asSetup(RawFrameKt.frame(frame));

            assertThat(actualFrame.getHeader().getStreamId()).isEqualTo(expectedFrame.getHeader().getStreamId());
            assertThat(actualFrame.getHeader().getFlags()).isEqualTo(expectedFrame.getHeader().getFlags());
            assertThat(actualFrame.getVersion()).isEqualTo(expectedFrame.getVersion());
            assertThat(actualFrame.getKeepAlive()).isEqualTo(expectedFrame.getKeepAlive());
            assertThat(actualFrame.getDataMimeType()).isEqualTo(expectedFrame.getDataMimeType());
        });

        And("^connection is not closed after$", () -> assertThat(connection.isDisposed()).isFalse());

        After(
                _scenario -> {
                    reactorServer.dispose();
                    clientConnection.dispose();
                }
        );
    }

    private void assertBinaryContent(ByteBuf frame) {
        int streamId = frame.readInt();
        assertThat(toBinaryString(streamId, 32)).isEqualTo(testData.get(STREAM_ID).get(2));
        short frameTypeAndFlags = frame.readShort();
        assertThat(toBinaryString(frameTypeAndFlags, 16)).isEqualTo(testData.get(FRAME_TYPE).get(2) + testData.get(FLAGS).get(2).substring(2));
        short majorVersion = frame.readShort();
        assertThat(toBinaryString(majorVersion, 16)).isEqualTo(testData.get(MAJOR_VERSION).get(2));
        short minorVersion = frame.readShort();
        assertThat(toBinaryString(minorVersion, 16)).isEqualTo(testData.get(MINOR_VERSION).get(2));
        int keepAlive = frame.readInt();
        assertThat(toBinaryString(keepAlive, 32)).isEqualTo(testData.get(KEEP_ALIVE).get(2));
        int maxLifeTime = frame.readInt();
        assertThat(toBinaryString(maxLifeTime, 32)).isEqualTo(testData.get(MAX_LIFE_TIME).get(2));
        // TODO FIX mime type check
//        short mimeTypeMetadataLength = frame.readUnsignedByte();
//        assertThat(toBinaryString(mimeTypeMetadataLength, 8)).isEqualTo(testData.get(MIME_TYPE_METADATA_LENGTH).get(2));
//        ByteBuf mimeTypeMetadata = frame.readBytes(mimeTypeMetadataLength);
//        byte mimeTypeDataLength = frame.readByte();
//        assertThat(toBinaryString(mimeTypeDataLength, 8)).isEqualTo(testData.get(MIME_TYPE_DATA_LENGTH).get(2));
//        ByteBuf mimeTypeData = frame.readBytes(mimeTypeDataLength);
    }

    static String toBinaryString(long val, int symbols) {
        String str = Long.toBinaryString(val);
        int diff = symbols - str.length();
        String result;
        if (diff > 0) {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < diff; i++) {
                stringBuilder
                        .append("0");
            }
            result = stringBuilder
                    .append(str)
                    .toString();
        } else {
            result = str;
        }

        StringBuilder builder = new StringBuilder();
        for (int i = result.length() - 1, j = 0; i >= 0; i--, j++) {
            if (j != 0 && j % 4 == 0) {
                builder.insert(0, '_');
            }
            builder.insert(0, result.charAt(i));
        }

        return builder.insert(0, "0b").toString();
    };
}
