package bestan.common.net.client;

import bestan.common.net.IProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

/**
 * @author yeyouhuan
 *
 */
public class NetClientEncodeHandler extends ChannelOutboundHandlerAdapter {
	private BaseNetClientManager client;
	
	public NetClientEncodeHandler(BaseNetClientManager client) {
		this.client = client;
	}
	
	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		if (msg instanceof IProtocol) {
			var data = ((IProtocol)msg).encode();
			ByteBuf encoded = ctx.alloc().buffer(data.length);
			encoded.writeBytes(data);
			ctx.writeAndFlush(encoded);
		} else {
			throw new RuntimeException(client.getConfig().clientName + " NetClientEncodeHandler:invalid message");
		}
	}
}
