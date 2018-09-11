package bestan.common.net;

import com.google.protobuf.Message;

import bestan.common.log.Glog;
import bestan.common.message.MessageFactory;
import bestan.common.net.RpcManager.RpcObject;
import bestan.common.protobuf.Proto;
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

	public static void sendRpc(ChannelHandlerContext ctx, Message arg, Class<? extends Message> resCls, Object param) {
		sendRpc(ctx, arg, resCls, 10, param);
	}
	
	public static void sendRpc(ChannelHandlerContext ctx, Message arg, Class<? extends Message> resCls) {
		sendRpc(ctx, arg, resCls, 10, null);
	}
	
	public static void sendRpc(ChannelHandlerContext ctx, Message arg, Class<? extends Message> resCls, int timeout) {
		sendRpc(ctx, arg, resCls, timeout, null);
	}
	
	public static void sendRpc(ChannelHandlerContext ctx, Message arg, Class<? extends Message> resCls, int timeout, Object param) {
		var resIndex = MessageFactory.getMessageIndex(resCls);
		if (resIndex == 0) {
			return;
		}
		var argIndex = MessageFactory.getMessageIndex(arg);
		if (argIndex == 0) {
			return;
		}
		var builder = Proto.RpcMessage.newBuilder();
		builder.setArgMessageId(argIndex);
		builder.setResMessageId(resIndex);
		builder.setIsRequest(true);
		builder.setRpcIndex(RpcManager.getInstance().getAndIncrementIndex());
		builder.setMessageData(arg.toByteString());
		RpcManager.getInstance().put(builder, arg, param, timeout);
		ctx.writeAndFlush(builder.build());
	}
	
	public static void sendRpc(ChannelHandlerContext ctx, RpcObject rpc) {
		ctx.writeAndFlush(rpc.getRpcMessage());
		RpcManager.getInstance().put(rpc);
	}
	
}
