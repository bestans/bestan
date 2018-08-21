package bestan.common.net.server;

import bestan.common.log.Glog;
import bestan.common.net.IProtocol;
import bestan.common.thread.BExecutor;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class NetServerHandler extends SimpleChannelInboundHandler<IProtocol> {
	private BExecutor workExecutor;
	private BaseNetServerManager serverManager;
	private NetServerConfig config;
	
	public NetServerHandler(BaseNetServerManager serverManager) {
		config = serverManager.getConfig();
		this.serverManager = serverManager;
		this.workExecutor = config.workdExecutor;
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, IProtocol protocol) throws Exception {
		workExecutor.execute(protocol);
	}
	
	@Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    	Glog.error("{} NetServerHandler exceptionCaught.error={}", config.serverName, cause);
        ctx.close();
    }
	
	@Override 
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		serverManager.channelRegistered(ctx);
	}
	     
	@Override 
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		serverManager.channelUnregistered(ctx);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		serverManager.channelActive(ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		serverManager.channelInactive(ctx);
	}
}
