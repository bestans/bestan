package bestan.common.net;

import java.util.List;

import com.google.protobuf.Message;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * @author yeyouhuan
 *
 */
public class NetDecodeHandler extends ByteToMessageDecoder {
	private Message baseMessage;

	public NetDecodeHandler(Message baseMessage) {
		this.baseMessage = baseMessage;
	}
	
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		if(!in.isReadable() || in.readableBytes() < 4) {
			return;
		}
		while (in.readableBytes() >= 4) {
	        int dataLength = in.readInt();
	        if (in.readableBytes() < dataLength) {
	            in.resetReaderIndex();
	            return;
	        }
	        byte[] data = new byte[dataLength];
	        in.readBytes(data);
	        var message = baseMessage.newBuilderForType().mergeFrom(data).build();
	        out.add(new CommonProtocol(ctx, message));
		}
	}

}
