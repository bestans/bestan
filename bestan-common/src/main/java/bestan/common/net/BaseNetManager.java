package bestan.common.net;

import com.google.protobuf.Message;

import bestan.common.logic.IObject;
import bestan.common.message.MessageFactory;
import bestan.common.net.RpcManager.RpcObject;
import bestan.common.protobuf.Proto;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.internal.logging.InternalLoggerFactory;

/**
 * @author yeyouhuan
 *
 */
public abstract class BaseNetManager implements INetManager {
	protected static InternalLoggerFactory LOGGER_FACTORY = new NettyInternalLoggerFactory();
	
	protected IProtocol baseProtocol;

	public BaseNetManager(IProtocol baseProtocol) {
		this.baseProtocol = baseProtocol;
	}
	
	public void writeAndFlush(ChannelHandlerContext ctx, Message message) {
		ctx.writeAndFlush(baseProtocol.packMessage(message));
	}

	public void writeAndFlush(ChannelHandlerContext ctx, IObject object, Message message) {
		ctx.writeAndFlush(baseProtocol.packMessage(object, message));
	}
	
	public void sendRpc(ChannelHandlerContext ctx, Message arg, Class<? extends Message> resCls, Object param) {
		sendRpc(ctx, arg, resCls, 10, param);
	}
	
	public void sendRpc(ChannelHandlerContext ctx, Message arg, Class<? extends Message> resCls) {
		sendRpc(ctx, arg, resCls, 10, null);
	}
	
	public void sendRpc(ChannelHandlerContext ctx, Message arg, Class<? extends Message> resCls, int timeout) {
		sendRpc(ctx, arg, resCls, timeout, null);
	}
	
	public void sendRpc(ChannelHandlerContext ctx, Message arg, Class<? extends Message> resCls, int timeout, Object param) {
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
		writeAndFlush(ctx, builder.build());
	}
	
	public void sendRpc(ChannelHandlerContext ctx, RpcObject rpc) {
		writeAndFlush(ctx, rpc.getRpcMessage().build());
		RpcManager.getInstance().put(rpc);
	}
}
