package bestan.common.net.client;

import com.google.protobuf.Message;

import bestan.common.net.IProtocol;
import bestan.common.thread.BExecutor;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class NetClientHandler extends SimpleChannelInboundHandler<Message> {
	private BaseNetClientManager client;
	private BExecutor workExecutor;
	private IProtocol baseProtocol;
	
	public NetClientHandler(BaseNetClientManager client) {
		this.client = client;
		workExecutor = client.getConfig().workdExecutor;
		baseProtocol = client.getConfig().baseProtocol;
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Message message) throws Exception {
		workExecutor.execute(baseProtocol.makeProtocol(ctx, message));
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
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		client.setChannel(null);
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		// TODO Auto-generated method stub
		client.exceptionCaught(ctx, cause);
		ctx.close();
	}
}
