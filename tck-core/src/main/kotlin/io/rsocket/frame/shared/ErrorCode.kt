package io.rsocket.frame.shared

/**
 * The types of [Error] that can be set.
 *
 * @see [Error
 * Codes](https://github.com/rsocket/rsocket/blob/master/Protocol.md.error-codes)
 */
interface ErrorCode {
    val value: Int
}

@Suppress("FunctionName")
fun ErrorCode(value: Int): ErrorCode = DefinedErrorCode[value] ?: CustomErrorCode(value)

enum class DefinedErrorCode(override val value: Int) : ErrorCode {

    /** Reserved.  */
    Reserved(0x00000000),

    // defined zero stream id error codes

    /**
     * The Setup frame is invalid for the server (it could be that the client is too recent for the
     * old server). Stream ID MUST be 0.
     */
    InvalidSetup(0x00000001),

    /**
     * Some (or all) of the parameters specified by the client are unsupported by the server. Stream
     * ID MUST be 0.
     */
    UnsupportedSetup(0x00000002),

    /**
     * The server rejected the setup, it can specify the reason in the payload. Stream ID MUST be 0.
     */
    RejectedSetup(0x00000003),

    /**
     * The server rejected the resume, it can specify the reason in the payload. Stream ID MUST be 0.
     */
    RejectedResume(0x00000004),


    /**
     * The connection is being terminated. Stream ID MUST be 0. Sender or Receiver of this frame MAY
     * close the connection immediately without waiting for outstanding streams to terminate.
     */
    ConnectionError(0x00000101),

    /**
     * The connection is being terminated. Stream ID MUST be 0. Sender or Receiver of this frame MUST
     * wait for outstanding streams to terminate before closing the connection. New requests MAY not
     * be accepted.
     */
    ConnectionClose(0x00000102),


    // defined non-zero stream id error codes

    /**
     * Application layer logic generating a Reactive Streams onError event. Stream ID MUST be &gt; 0.
     */
    ApplicationError(0x00000201),

    /**
     * Despite being a valid request, the Responder decided to reject it. The Responder guarantees
     * that it didn't process the request. The reason for the rejection is explained in the Error Data
     * section. Stream ID MUST be &gt; 0.
     */
    Rejected(0x00000202),

    /**
     * The Responder canceled the request but may have started processing it (similar to REJECTED but
     * doesn't guarantee lack of side-effects). Stream ID MUST be &gt; 0.
     */
    Canceled(0x00000203),

    /** The request is invalid. Stream ID MUST be &gt; 0.  */
    Invalid(0x00000204),


    /** Reserved for Extension Use.  */
    ReservedForExtension(0xFFFFFFFF.toInt());

    companion object {
        private val map = values().associateBy { it.value }
        operator fun get(value: Int): DefinedErrorCode? = map[value]
    }
}

data class CustomErrorCode(override val value: Int) : ErrorCode {
    companion object {

        /** Minimum allowed user defined error code value  */
        const val Min: Int = 0x00000301

        /**
         * Maximum allowed user defined error code value. Note, the value is above signed integer maximum,
         * so it will be negative after overflow.
         */
        const val Max: Int = 0xFFFFFFFE.toInt()

    }
}
