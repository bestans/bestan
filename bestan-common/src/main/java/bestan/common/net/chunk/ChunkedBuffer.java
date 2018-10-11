package bestan.common.net.chunk;

import com.google.protobuf.ByteString;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.stream.ChunkedInput;

/**
 * @author yeyouhuan
 *
 */
public class ChunkedBuffer implements ChunkedInput<ByteString> {

    static final int DEFAULT_CHUNK_SIZE = 8192;

    private byte[] in;
    private final int chunkSize;
    private long offset;
    private boolean closed;

    /**
     * Creates a new instance that fetches data from the specified stream.
     */
    public ChunkedBuffer(byte[] in) {
        this(in, DEFAULT_CHUNK_SIZE);
    }

    /**
     * Creates a new instance that fetches data from the specified stream.
     *
     * @param chunkSize the number of bytes to fetch on each
     *                  {@link #readChunk(ChannelHandlerContext)} call
     */
    public ChunkedBuffer(byte[] in, int chunkSize) {
        if (in == null) {
            throw new NullPointerException("in");
        }
        if (chunkSize <= 0) {
            throw new IllegalArgumentException(
                    "chunkSize: " + chunkSize +
                    " (expected: a positive integer)");
        }
        this.in = in;
        this.chunkSize = chunkSize;
    }

    /**
     * Returns the number of transferred bytes.
     */
    public long transferredBytes() {
        return offset;
    }

    @Override
    public boolean isEndOfInput() throws Exception {
        if (closed) {
            return true;
        }

        return offset >= in.length;
    }

    @Override
    public void close() throws Exception {
        closed = true;
    }

    @Deprecated
    @Override
    public ByteString readChunk(ChannelHandlerContext ctx) throws Exception {
        return readChunk(ctx.alloc());
    }

    @Override
    public ByteString readChunk(ByteBufAllocator allocator) throws Exception {
        if (isEndOfInput()) {
            return null;
        }

        final int availableBytes = (int)(in.length - offset);
        if (availableBytes <= 0) {
        	return null;
        }
        final int chunkSize = Math.min(this.chunkSize, availableBytes);
        // transfer to buffer
    	var buffer = ByteString.copyFrom(in, (int)offset, chunkSize);
        offset += chunkSize;
        return buffer;
    }

    @Override
    public long length() {
        return in.length;
    }

    @Override
    public long progress() {
        return offset;
    }
}
