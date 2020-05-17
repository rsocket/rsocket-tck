package io.rsocket.tck.frame

import io.rsocket.frame.shared.*

/**
 * Types of Frame that can be sent.
 *
 * @see [Frame
 * Types](https://github.com/rsocket/rsocket/blob/master/Protocol.md.frame-types)
 */
/**
 * Returns the encoded type.
 *
 * @return the encoded type
 */
enum class FrameType(val encodedType: Int, flags: Int = Flags.EMPTY) {

    /** Reserved.  */
    RESERVED(0x00),

    // CONNECTION

    /**
     * Sent by client to initiate protocol processing.
     *
     * @see [Setup
     * Frame](https://github.com/rsocket/rsocket/blob/master/Protocol.md.setup-frame-0x01)
     */
    SETUP(0x01, Flags.CAN_HAVE_DATA or Flags.CAN_HAVE_METADATA),

    /**
     * Sent by Responder to grant the ability to send requests.
     *
     * @see [Lease
     * Frame](https://github.com/rsocket/rsocket/blob/master/Protocol.md.lease-frame-0x02)
     */
    LEASE(0x02, Flags.CAN_HAVE_METADATA),

    /**
     * Connection keepalive.
     *
     * @see [Keepalive
     * Frame](https://github.com/rsocket/rsocket/blob/master/Protocol.md.frame-keepalive)
     */
    KEEPALIVE(0x03, Flags.CAN_HAVE_DATA),

    // START REQUEST

    /**
     * Request single response.
     *
     * @see [Request
     * Response Frame](https://github.com/rsocket/rsocket/blob/master/Protocol.md.frame-request-response)
     */
    REQUEST_RESPONSE(
        0x04,
        Flags.CAN_HAVE_DATA or Flags.CAN_HAVE_METADATA or Flags.IS_FRAGMENTABLE or Flags.IS_REQUEST_TYPE
    ),

    /**
     * A single one-way message.
     *
     * @see [Request
     * Fire-and-Forget Frame](https://github.com/rsocket/rsocket/blob/master/Protocol.md.frame-fnf)
     */
    REQUEST_FNF(0x05, Flags.CAN_HAVE_DATA or Flags.CAN_HAVE_METADATA or Flags.IS_FRAGMENTABLE or Flags.IS_REQUEST_TYPE),

    /**
     * Request a completable stream.
     *
     * @see [Request
     * Stream Frame](https://github.com/rsocket/rsocket/blob/master/Protocol.md.frame-request-stream)
     */
    REQUEST_STREAM(
        0x06,
        Flags.CAN_HAVE_METADATA or Flags.CAN_HAVE_DATA or Flags.HAS_INITIAL_REQUEST_N or Flags.IS_FRAGMENTABLE or Flags.IS_REQUEST_TYPE
    ),

    /**
     * Request a completable stream in both directions.
     *
     * @see [Request
     * Channel Frame](https://github.com/rsocket/rsocket/blob/master/Protocol.md.frame-request-channel)
     */
    REQUEST_CHANNEL(
        0x07,
        Flags.CAN_HAVE_METADATA or Flags.CAN_HAVE_DATA or Flags.HAS_INITIAL_REQUEST_N or Flags.IS_FRAGMENTABLE or Flags.IS_REQUEST_TYPE
    ),

    // DURING REQUEST

    /**
     * Request N more items with Reactive Streams semantics.
     *
     * @see [RequestN
     * Frame](https://github.com/rsocket/rsocket/blob/master/Protocol.md.frame-request-n)
     */
    REQUEST_N(0x08),

    /**
     * Cancel outstanding request.
     *
     * @see [Cancel
     * Frame](https://github.com/rsocket/rsocket/blob/master/Protocol.md.frame-cancel)
     */
    CANCEL(0x09),

    // RESPONSE

    /**
     * Payload on a stream. For example, response to a request, or message on a channel.
     *
     * @see [Payload
     * Frame](https://github.com/rsocket/rsocket/blob/master/Protocol.md.frame-payload)
     */
    PAYLOAD(0x0A, Flags.CAN_HAVE_DATA or Flags.CAN_HAVE_METADATA or Flags.IS_FRAGMENTABLE),

    /**
     * Error at connection or application level.
     *
     * @see [Error
     * Frame](https://github.com/rsocket/rsocket/blob/master/Protocol.md.frame-error)
     */
    ERROR(0x0B, Flags.CAN_HAVE_DATA),

    // METADATA

    /**
     * Asynchronous Metadata frame.
     *
     * @see [Metadata
     * Push Frame](https://github.com/rsocket/rsocket/blob/master/Protocol.md.frame-metadata-push)
     */
    METADATA_PUSH(0x0C, Flags.CAN_HAVE_METADATA),

    // RESUMPTION

    /**
     * Replaces SETUP for Resuming Operation (optional).
     *
     * @see [Resume
     * Frame](https://github.com/rsocket/rsocket/blob/master/Protocol.md.frame-resume)
     */
    RESUME(0x0D),

    /**
     * Sent in response to a RESUME if resuming operation possible (optional).
     *
     * @see [Resume OK
     * Frame](https://github.com/rsocket/rsocket/blob/master/Protocol.md.frame-resume-ok)
     */
    RESUME_OK(0x0E),

    /**
     * Used To Extend more frame types as well as extensions.
     *
     * @see [Extension
     * Frame](https://github.com/rsocket/rsocket/blob/master/Protocol.md.frame-ext)
     */
    EXT(0x3F, Flags.CAN_HAVE_DATA or Flags.CAN_HAVE_METADATA);

    /**
     * Whether the frame type starts with an initial `requestN`.
     *
     * @return wether the frame type starts with an initial `requestN`s
     */
    val hasInitialRequestN: Boolean = flags check Flags.HAS_INITIAL_REQUEST_N

    /**
     * Whether the frame type is a request type.
     *
     * @return whether the frame type is a request type
     */
    val isRequestType: Boolean = flags check Flags.IS_REQUEST_TYPE

    /**
     * Whether the frame type is fragmentable.
     *
     * @return whether the frame type is fragmentable
     */
    val isFragmentable: Boolean = flags check Flags.IS_FRAGMENTABLE

    /**
     * Whether the frame type can have metadata
     *
     * @return whether the frame type can have metadata
     */
    val canHaveMetadata: Boolean = flags check Flags.CAN_HAVE_METADATA

    /**
     * Whether the frame type can have data.
     *
     * @return whether the frame type can have data
     */
    val canHaveData: Boolean = flags check Flags.CAN_HAVE_DATA

    private object Flags {
        const val EMPTY = 0
        const val HAS_INITIAL_REQUEST_N = 1
        const val IS_REQUEST_TYPE = 2
        const val IS_FRAGMENTABLE = 4
        const val CAN_HAVE_METADATA = 8
        const val CAN_HAVE_DATA = 16
    }

    companion object {
        const val SIZE: Int = 6

        private val FRAME_TYPES_BY_ENCODED_TYPE: Array<FrameType?>

        init {
            val maximumEncodedType = values().map { it.encodedType }.max() ?: 0
            FRAME_TYPES_BY_ENCODED_TYPE = arrayOfNulls(maximumEncodedType + 1)
            values().forEach { FRAME_TYPES_BY_ENCODED_TYPE[it.encodedType] = it }
        }

        /**
         * Returns the `FrameType` that matches the specified `encodedType`.
         *
         * @param encodedType the encoded type
         * @return the `FrameType` that matches the specified `encodedType`
         */
        fun fromEncodedType(encodedType: Int): FrameType =
            FRAME_TYPES_BY_ENCODED_TYPE[encodedType]
                ?: throw IllegalArgumentException("Frame type $encodedType is unknown")
    }
}
