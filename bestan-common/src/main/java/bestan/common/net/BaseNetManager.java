package bestan.common.net;

import java.util.List;

import com.google.protobuf.Message;

import bestan.common.guid.Guid;
import bestan.common.logic.IObject;
import bestan.common.message.MessageFactory;
import bestan.common.net.RpcManager.RpcObject;
import bestan.common.net.operation.CommonDBParam;
import bestan.common.net.operation.CommonLoad;
import bestan.common.net.operation.CommonSave;
import bestan.common.protobuf.Proto;
import bestan.common.protobuf.Proto.RpcCommonLoadOp;
import bestan.common.protobuf.Proto.RpcCommonLoadOpRes;
import bestan.common.protobuf.Proto.RpcCommonSaveOp;
import bestan.common.protobuf.Proto.RpcCommonSaveOpRes;
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
		sendRpc(ctx, arg, resCls, NetConst.RPC_TIMEOUT, param);
	}
	
	public void sendRpc(ChannelHandlerContext ctx, Message arg, Class<? extends Message> resCls) {
		sendRpc(ctx, arg, resCls, NetConst.RPC_TIMEOUT, null);
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

	public void rpcCommonSave(ChannelHandlerContext ctx, String tableName, Object key, Object value, int opType, IObject object) {
		rpcCommonSave(ctx, tableName, key, value, new CommonDBParam(opType, object));
	}
	public void rpcCommonSave(ChannelHandlerContext ctx, String tableName, Object key, Object value, int opType, Guid guid) {
		rpcCommonSave(ctx, tableName, key, value, new CommonDBParam(opType, guid));
	}
	public void rpcCommonSave(ChannelHandlerContext ctx, String tableName, Object key, Object value, CommonDBParam param) {
		rpcCommonSave(ctx, new CommonSave(tableName, key, value), param);
	}
	public void rpcCommonSave(ChannelHandlerContext ctx, CommonSave op, CommonDBParam param) {
		var message = RpcCommonSaveOp.newBuilder();
		message.addSaveOps(op.getBuilder());
		message.setOpType(param.getOpType());
		sendRpc(ctx, message.build(), RpcCommonSaveOpRes.class, param.getTimeout(), param);
	}
	public void rpcCommonSave(ChannelHandlerContext ctx, List<CommonSave> ops, CommonDBParam param) {
		if (ops.size() <= 0) {
			return;
		}
		var message = RpcCommonSaveOp.newBuilder();
		for (var op : ops) {
			message.addSaveOps(op.getBuilder());
		}
		message.setOpType(param.getOpType());
		sendRpc(ctx, message.build(), RpcCommonSaveOpRes.class, param.getTimeout(), param);
	}

	public void rpcCommonLoad(ChannelHandlerContext ctx, String tableName, Object key, int opType, Guid guid) {
		rpcCommonLoad(ctx, new CommonLoad(tableName, key), new CommonDBParam(opType, guid));
	}
	public void rpcCommonLoad(ChannelHandlerContext ctx, String tableName, Object key, int opType, IObject object) {
		rpcCommonLoad(ctx, new CommonLoad(tableName, key), new CommonDBParam(opType, object));
	}
	public void rpcCommonLoad(ChannelHandlerContext ctx, String tableName, Object key, CommonDBParam param) {
		rpcCommonLoad(ctx, new CommonLoad(tableName, key), param);
	}
	public void rpcCommonLoad(ChannelHandlerContext ctx, CommonLoad op, CommonDBParam param) {
		var message = RpcCommonLoadOp.newBuilder();
		message.addLoadOps(op.getBuilder());
		message.setOpType(param.getOpType());
		sendRpc(ctx, message.build(), RpcCommonLoadOpRes.class, param.getTimeout(), param);
	}
	public void rpcCommonLoad(ChannelHandlerContext ctx, List<CommonLoad> ops, CommonDBParam param) {
		if (ops.size() <= 0) {
			return;
		}
		var message = RpcCommonLoadOp.newBuilder();
		for (var op : ops) {
			message.addLoadOps(op.getBuilder());
		}
		message.setOpType(param.getOpType());
		sendRpc(ctx, message.build(), RpcCommonLoadOpRes.class, param.getTimeout(), param);
	}
}
