package bestan.common.net;

import bestan.common.log.Glog;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author yeyouhuan
 *
 */
public interface INetManager {
	public default void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		
	}

	public default void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		
	}

	public default void channelActive(ChannelHandlerContext ctx) throws Exception {
		
	}

	public default void channelInactive(ChannelHandlerContext ctx) throws Exception {
		
	}
	
	public default void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		Glog.debug("exceptionCaught:cause={}", cause);
	}
}
