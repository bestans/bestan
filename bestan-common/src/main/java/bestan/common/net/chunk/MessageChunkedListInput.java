package bestan.common.net.chunk;

import java.util.List;

import bestan.common.net.IProtocol;
import bestan.common.net.MessagePack;
import bestan.common.protobuf.Proto.ChunkedData;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.stream.ChunkedInput;

/**
 * @author yeyouhuan
 *
 */
public class MessageChunkedListInput implements ChunkedInput<MessagePack>  {


    private final ChunkedBufferList input;
    private final MessagePack lastMessage;
    private boolean sendLastChunk;
    private IProtocol protocol;

    public MessageChunkedListInput(IProtocol protocol, List<byte[]> in) {
    	this(protocol, new ChunkedBufferList(in));
    }

    public MessageChunkedListInput(IProtocol protocol, List<byte[]> in, boolean sendLastChunk) {
    	this(new ChunkedBufferList(in), protocol, sendLastChunk);
    }
    
    /**
     * Creates a new instance using the specified input.
     * @param input {@link ChunkedInput} containing data to write
     */
    public MessageChunkedListInput(IProtocol protocol, ChunkedBufferList input) {
    	this(input, protocol, false);
    }

    /**
     * Creates a new instance using the specified input. {@code lastHttpContent} will be written as the terminating
     * chunk.
     * @param input {@link ChunkedInput} containing data to write
     * @param lastHttpContent {@link LastHttpContent} that will be written as the terminating chunk. Use this for
     *            training headers.
     */
    public MessageChunkedListInput(ChunkedBufferList input, IProtocol protocol, boolean sendLastChunk) {
        this.input = input;
        this.sendLastChunk = sendLastChunk;
        this.lastMessage = getLastMessage(protocol, sendLastChunk);
        this.protocol = protocol;
    }

    private static MessagePack getLastMessage(IProtocol protocol, boolean sentLastChunk) {
    	if (!sentLastChunk) { 
    		return null;
    	}
    	
        var data = ChunkedData.newBuilder();
        data.setEnd(true);
        return protocol.packMessage(data.build());
    }
    
    @Override
    public boolean isEndOfInput() throws Exception {
        if (input.isEndOfInput()) {
            // Only end of input after last HTTP chunk has been sent
            return !sendLastChunk;
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
    public MessagePack readChunk(ChannelHandlerContext ctx) throws Exception {
        return readChunk(ctx.alloc());
    }

    @Override
    public MessagePack readChunk(ByteBufAllocator allocator) throws Exception {
        if (input.isEndOfInput()) {
            if (!sendLastChunk) {
                return null;
            } else {
                // Send last chunk for this input
                sendLastChunk = false;
                return lastMessage;
            }
        } else {
            var tempChunk = input.readChunk(allocator);
            if (tempChunk == null) {
                return null;
            }
            var data = ChunkedData.newBuilder();
            data.setChunk(tempChunk.first);
            data.setSectionEnd(tempChunk.second);
            return protocol.packMessage(data.build());
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
