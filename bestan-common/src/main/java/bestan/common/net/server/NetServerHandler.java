package bestan.common.net.server;

import com.google.protobuf.Message;

import bestan.common.net.IProtocol;
import bestan.common.thread.BExecutor;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author yeyouhuan
 *
 */
public class NetServerHandler extends SimpleChannelInboundHandler<Message> {
	private BExecutor workExecutor;
	private BaseNetServerManager serverManager;
	private NetServerConfig config;
	private IProtocol baseProtocol;
	
	public NetServerHandler(BaseNetServerManager serverManager) {
		config = serverManager.getConfig();
		this.serverManager = serverManager;
		this.workExecutor = config.workdExecutor;
		this.baseProtocol = config.baseProtocol;
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Message message) throws Exception {
		workExecutor.execute(baseProtocol.makeProtocol(ctx, message));
	}
	
	@Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    	serverManager.exceptionCaught(ctx, cause);
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
