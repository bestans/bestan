package bestan.common.net;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author yeyouhuan
 *
 */
public class NetEncodeHandler extends MessageToByteEncoder<MessagePack> {
	private IProtocol baseProtocol;

	public NetEncodeHandler(IProtocol baseProtocol) {
		this.baseProtocol = baseProtocol;
	}
	
	@Override
	protected void encode(ChannelHandlerContext ctx, MessagePack msg, ByteBuf out) throws Exception {
		var data = baseProtocol.encode(msg).toByteArray();
		out.writeInt(data.length);
		out.writeBytes(data);
	}
}
