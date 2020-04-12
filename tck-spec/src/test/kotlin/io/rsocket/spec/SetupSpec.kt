package io.rsocket.spec

import io.rsocket.expect.frame.*
import io.rsocket.frame.*
import io.rsocket.frame.shared.*
import org.spekframework.spek2.*
import org.spekframework.spek2.style.specification.*
import kotlin.time.*

object SetupSpec : Spek({
    //TODO add some real client/server to test against
    //val context by memoized { RealContext }
    describe("Setup", config.skipIf("setup")) {

        describe("reject connection", config.skipIf("setup.reject")) {
            val transport by memoized { MockErrorTransport }

            case("with ERROR[INVALID_SETUP] if SETUP stream id > 0", config.skipIf("setup.reject.streamIdPositive")) {
                val setup = SetupFrame(
                    header = FrameHeader(
                        streamId = 1, //wrong id for setup frame
                        flags = SetupFlags(
                            resume = false,
                            lease = false,
                            metadata = false
                        )
                    ),
                    version = Version(1, 0),
                    keepAlive = KeepAlive(
                        interval = 500.milliseconds,
                        maxLifetime = 5.seconds
                    ),
                    resumeToken = null,
                    metadataMimeType = MimeType("application/binary"),
                    dataMimeType = MimeType("application/binary"),
                    payload = Payload(
                        metadata = null,
                        data = bufferOf(1, 2, 3)
                    )
                )
                send(setup)
                val error = receive().asError() //here validated that frame type is [FrameType.ERROR]
                error expect ErrorFrame(
                    header = FrameHeader(
                        streamId = 0,
                        flags = UntypedFlags.Empty
                    ),
                    code = DefinedErrorCode.InvalidSetup,
                    data = TextBuffer("failed?") //TODO not need to check?
                )
            }

            //ignored while with length = false
            withLengthCase("with ERROR[INVALID_SETUP] if SETUP stream id > 0", config.skipIf("setup.reject.streamIdPositive")) {
                val setup = SetupFrame(
                    header = FrameHeader(
                        streamId = 1, //wrong id for setup frame
                        flags = SetupFlags(
                            resume = false,
                            lease = false,
                            metadata = false
                        )
                    ),
                    version = Version(1, 0),
                    keepAlive = KeepAlive(
                        interval = 500.milliseconds,
                        maxLifetime = 5.seconds
                    ),
                    resumeToken = null,
                    metadataMimeType = MimeType("application/binary"),
                    dataMimeType = MimeType("application/binary"),
                    payload = Payload(
                        metadata = null,
                        data = bufferOf(1, 2, 3)
                    )
                )
                send(setup)
                val error = receive().frame.asError() //here validated that frame type is [FrameType.ERROR]
                error expect ErrorFrame(
                    header = FrameHeader(
                        streamId = 0,
                        flags = UntypedFlags.Empty
                    ),
                    code = DefinedErrorCode.InvalidSetup,
                    data = TextBuffer("failed?") //TODO not need to check?
                )
            }
        }

        describe("accept connection", config.skipIf("setup.accept")) {
            val transport by memoized { MockDelayTransport }

            case("no ERROR frame for 1 second", config.skipIf("setup.accept.streamId0")) {
                val setup = SetupFrame(
                    header = FrameHeader(
                        streamId = 1, //wrong id for setup frame
                        flags = SetupFlags(
                            resume = false,
                            lease = false,
                            metadata = false
                        )
                    ),
                    version = Version(1, 0),
                    keepAlive = KeepAlive(
                        interval = 500.milliseconds,
                        maxLifetime = 5.seconds
                    ),
                    resumeToken = null,
                    metadataMimeType = MimeType("application/binary"),
                    dataMimeType = MimeType("application/binary"),
                    payload = Payload(
                        metadata = null,
                        data = bufferOf(1, 2, 3)
                    )
                )
                send(setup)
                receiveOrNull(1.seconds)
            }

        }
    }
})
