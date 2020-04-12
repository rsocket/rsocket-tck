package io.rsocket

import io.rsocket.expect.frame.*
import io.rsocket.frame.*
import io.rsocket.frame.shared.*
import kotlin.time.*

//just to show power of assertions
fun main() {
    val f1 = SetupFrame(
        FrameHeader(
            0,
            SetupFlags(
                true,
                false,
                false
            )
        ),
        Version(2, 0),
        KeepAlive(
            500.milliseconds,
            5.seconds
        ),
        ResumeToken(
            bufferOf(1, 2, 3, 4, 5),
            4
        ),
        MimeType("application/json"),
        MimeType("application/binary"),
        Payload(null)
    )
    val f2 = SetupFrame(
        FrameHeader(
            0,
            SetupFlags(
                true,
                false,
                true
            )
        ),
        Version(3, 0),
        KeepAlive(
            500.milliseconds,
            6.seconds
        ),
        ResumeToken(
            bufferOf(1, 2, 3, 4, 6),
            5
        ),
        MimeType("application/binary"),
        MimeType("application/binary"),
        Payload(
            PayloadMetadata(
                bufferOf(1, 2, 3)
            )
        )
    )
    f1 expect f2
}

fun main2() {
    val f1 = ErrorFrame(
        FrameHeader(
            2,
            UntypedFlags(512)
        ),
        DefinedErrorCode.ApplicationError,
        TextBuffer("error1")
    )
    val f2 = ErrorFrame(
        FrameHeader(
            2,
            UntypedFlags(64)
        ),
        DefinedErrorCode.Canceled,
        TextBuffer("error2")
    )
    f1 expect f2
}
