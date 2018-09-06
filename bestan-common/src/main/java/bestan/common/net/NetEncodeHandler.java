package bestan.common.net;

import com.google.protobuf.Message;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author yeyouhuan
 *
 */
public class NetEncodeHandler extends MessageToByteEncoder<Message> {
	private IProtocol baseProtocol;

	public NetEncodeHandler(IProtocol baseProtocol) {
		this.baseProtocol = baseProtocol;
	}
	
	@Override
	protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
		var data = baseProtocol.encode(msg).toByteArray();
		out.writeInt(data.length);
		out.writeBytes(data);
	}
}
