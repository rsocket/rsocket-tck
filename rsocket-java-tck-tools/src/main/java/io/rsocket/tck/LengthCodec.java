package io.rsocket.tck;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class LengthCodec extends LengthFieldBasedFrameDecoder {
    private static final int FRAME_LENGTH_MASK = 0xFFFFFF;
    private static final int FRAME_LENGTH_SIZE = 3;

    public LengthCodec() {
        super(FRAME_LENGTH_MASK, 0, FRAME_LENGTH_SIZE, 0, 0);
    }

    public Object decode(ByteBuf input) throws Exception {
        return decode(null,input);
    }
}
