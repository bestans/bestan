package bestan.common.net.server;

import com.google.protobuf.GeneratedMessage;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

public abstract class AbstractNetManager {
	public boolean init() {
		return true;
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
