package bestan.common.net.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelHandler.Sharable;

/**
 * this class is control All Online Connection includes AllClient`s and IWeb
 */
@Sharable
public class NettyChannelHandleManager extends ChannelInboundHandlerAdapter {
	private static NettyChannelHandleManager instance;
	
	protected NettyChannelHandleManager() {
	}
	
	
	public static NettyChannelHandleManager getInstance() {
		if(null == instance) {
			instance = new NettyChannelHandleManager();
		}
		
		return instance;
	}
	
	@Override 
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
//		logger.debug("channelRegistered");
	}
	     
	@Override 
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
//		logger.debug("channelUnregistered");
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
//		logger.debug("channelActive");
		NettyServer.ALL_CHANNELS.add(ctx.channel());
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
//		logger.debug("channelInactive");
		NettyServer.ALL_CHANNELS.remove(ctx.channel());
	}

}
