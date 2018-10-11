package bestan.common.net.chunk;

import com.google.protobuf.Message;

import bestan.common.protobuf.Proto.ChunkedData;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.stream.ChunkedInput;

/**
 * 以message形式分块传输数据
 * 
 * @author yeyouhuan
 *
 */
public class MessageChunkedInput implements ChunkedInput<Message> {

    private final ChunkedBuffer input;
    private final Message lastMessage;
    private boolean sendLastChunk;

    public MessageChunkedInput(byte[] in) {
    	this(new ChunkedBuffer(in));
    }

    public MessageChunkedInput(byte[] in, boolean sendLastChunk) {
    	this(new ChunkedBuffer(in), sendLastChunk);
    }
    
    /**
     * Creates a new instance using the specified input.
     * @param input {@link ChunkedInput} containing data to write
     */
    public MessageChunkedInput(ChunkedBuffer input) {
    	this(input, false);
    }

    /**
     * Creates a new instance using the specified input. {@code lastHttpContent} will be written as the terminating
     * chunk.
     * @param input {@link ChunkedInput} containing data to write
     * @param lastHttpContent {@link LastHttpContent} that will be written as the terminating chunk. Use this for
     *            training headers.
     */
    public MessageChunkedInput(ChunkedBuffer input, boolean sendLastChunk) {
        this.input = input;
        this.sendLastChunk = sendLastChunk;
        this.lastMessage = getLastMessage(sendLastChunk);
    }

    private static Message getLastMessage(boolean sentLastChunk) {
    	if (!sentLastChunk) { 
    		return null;
    	}
    	
        var data = ChunkedData.newBuilder();
        data.setEnd(true);
        return data.build();
    }
    
    @Override
    public boolean isEndOfInput() throws Exception {
        if (input.isEndOfInput()) {
            // Only end of input after last HTTP chunk has been sent
            return sendLastChunk;
        } else {
            return false;
        }
    }

    @Override
    public void close() throws Exception {
        input.close();
    }

    @Deprecated
    @Override
    public Message readChunk(ChannelHandlerContext ctx) throws Exception {
        return readChunk(ctx.alloc());
    }

    @Override
    public Message readChunk(ByteBufAllocator allocator) throws Exception {
        if (input.isEndOfInput()) {
            if (sendLastChunk) {
                return null;
            } else {
                // Send last chunk for this input
                sendLastChunk = true;
                return lastMessage;
            }
        } else {
            var buf = input.readChunk(allocator);
            if (buf == null) {
                return null;
            }
            var data = ChunkedData.newBuilder();
            data.setChunk(buf);
            return data.build();
        }
    }

    @Override
    public long length() {
        return input.length();
    }

    @Override
    public long progress() {
        return input.progress();
    }
}
