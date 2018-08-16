package bestan.common.net.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author yeyouhuan
 *
 */
public class NetClientChannelHandleManager extends ChannelInboundHandlerAdapter {
	private BaseNetClientManager client;
	public NetClientChannelHandleManager(BaseNetClientManager client) {
		this.client = client;
	}
	
	@Override 
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		super.channelRegistered(ctx);
	}

	@Override 
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		this.client.setChannel(null);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		this.client.setChannel(ctx);
		//this.client.sendRegister();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		client.setChannel(null);
	}
}
