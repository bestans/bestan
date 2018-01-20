package bestan.common.net.netty.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;



public class NettyClientChannelHandleManager extends ChannelInboundHandlerAdapter {
	private NettyClient client;
//	private final static Logger logger = LogManager.getLogger(NettyClientChannelHandleManager.class);
	public NettyClientChannelHandleManager(NettyClient client) {
		this.client = client;
	}
	
	@Override 
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
//		logger.debug("channelRegistered");
	}
	     
	@Override 
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
//		logger.debug("channelUnregistered");
		this.client.setChannel(null);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
//		logger.debug("channel Active OK");
		this.client.setChannel(ctx);
		this.client.sendRegister();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
//		logger.debug("channelInactive");
		this.client.setChannel(null);
	}

}