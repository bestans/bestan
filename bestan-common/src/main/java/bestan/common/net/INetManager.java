package bestan.common.net;

import java.io.IOException;

import bestan.common.log.Glog;
import bestan.common.util.ExceptionUtil;
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
	
	public default void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		String causeLog;
		if (cause instanceof IOException) {
			causeLog = "IOException";
		} else {
			causeLog = ExceptionUtil.getLog(cause);
		}
		Glog.debug("{} exceptionCaught:exception={},cause={}", getClass().getSimpleName(), cause.getClass(), causeLog);
		ctx.close();
	}
}
