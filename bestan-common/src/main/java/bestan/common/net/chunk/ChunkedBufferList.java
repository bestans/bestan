package bestan.common.net.chunk;

import java.util.List;

import com.google.protobuf.ByteString;

import bestan.common.util.PairData;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.stream.ChunkedInput;

/**
 * @author yeyouhuan
 *
 */
public class ChunkedBufferList implements ChunkedInput<PairData<ByteString, Boolean>> {

    static final int DEFAULT_CHUNK_SIZE = 8192;

    private List<byte[]> in;
    private final int chunkSize;
    private long offset;
    private boolean closed;
    private long totalSize;
    /**
     * 传输到第几块数据了
     */
    private int sectionIndex = 0;
    /**
     * 当前数据偏移
     */
    private int sectionOffset = 0;

    /**
     * Creates a new instance that fetches data from the specified stream.
     */
    public ChunkedBufferList(List<byte[]> in) {
        this(in, DEFAULT_CHUNK_SIZE);
    }

    /**
     * Creates a new instance that fetches data from the specified stream.
     *
     * @param chunkSize the number of bytes to fetch on each
     *                  {@link #readChunk(ChannelHandlerContext)} call
     */
    public ChunkedBufferList(List<byte[]> in, int chunkSize) {
        if (in == null || in.size() <= 0) {
            throw new NullPointerException("in");
        }
        if (chunkSize <= 0) {
            throw new IllegalArgumentException(
                    "chunkSize: " + chunkSize +
                    " (expected: a positive integer)");
        }
        this.in = in;
        this.chunkSize = chunkSize;
        for (var it : in) {
        	totalSize += it.length;
        }
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

        return offset >= totalSize;
    }

    @Override
    public void close() throws Exception {
        closed = true;
    }

    @Deprecated
    @Override
    public PairData<ByteString, Boolean> readChunk(ChannelHandlerContext ctx) throws Exception {
        return readChunk(ctx.alloc());
    }

    @Override
    public PairData<ByteString, Boolean> readChunk(ByteBufAllocator allocator) throws Exception {
        if (isEndOfInput()) {
            return null;
        }
        var data = in.get(sectionIndex);
        
        final int availableBytes = (int)(data.length - sectionOffset);
        if (availableBytes <= 0) {
        	return null;
        }
        final int chunkSize = Math.min(this.chunkSize, availableBytes);
        // transfer to buffer
    	var buffer = ByteString.copyFrom(data, sectionOffset, chunkSize);
    	sectionOffset += chunkSize;
        offset += chunkSize;
        Boolean sectionEnd = false;
        if (sectionOffset >= data.length) {
        	//下一份数据
        	sectionIndex++;
        	sectionOffset = 0;
        	sectionEnd = true;
        }
        return PairData.newPair(buffer, sectionEnd);
    }

    @Override
    public long length() {
        return totalSize;
    }

    @Override
    public long progress() {
        return offset;
    }
}
