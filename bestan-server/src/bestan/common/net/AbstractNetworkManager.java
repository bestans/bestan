package bestan.common.net;

import bestan.common.net.netty.NettyServer;
import bestan.common.server.MainServer.E_SERVER_TYPE;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import com.google.protobuf.GeneratedMessage;

public abstract class AbstractNetworkManager {
	private NettyServer netttyServer = null;
	
	public boolean init(E_SERVER_TYPE eServerType) {
		netttyServer = new NettyServer(eServerType);
		return true;
	}
	
	public void start() throws Exception {
		netttyServer.start();
	}
	
	public void finish() throws Exception {
		this.netttyServer.stopServer();
	}
	
	public void sendMessage(GeneratedMessage msg, ChannelHandlerContext ctx) {
		if(null != msg && null != ctx) {
			ctx.writeAndFlush(msg);
		}
	}
	
	public void sendMessage(GeneratedMessage msg, Channel channel) {
		if(null != msg && null != channel && channel.isActive()) {
			channel.writeAndFlush(msg);
		}
	}
	
	public void sendMessageNotFlush(GeneratedMessage msg, ChannelHandlerContext ctx) {
		if(null != msg && null != ctx) {
			ctx.write(msg);
		}
	}
	
	public void flush(ChannelHandlerContext ctx) {
		if(null != ctx) {
			ctx.flush();
		}
	}
	
	public abstract void tick();
}
