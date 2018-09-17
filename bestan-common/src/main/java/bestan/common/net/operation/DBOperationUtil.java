package bestan.common.net.operation;

import java.util.List;

import bestan.common.net.BaseNetManager;
import bestan.common.protobuf.Proto.CommonSaveOp;
import bestan.common.protobuf.Proto.RpcCommonLoadOp;
import bestan.common.protobuf.Proto.RpcCommonLoadOpRes;
import bestan.common.protobuf.Proto.RpcCommonSaveOp;
import bestan.common.protobuf.Proto.RpcCommonSaveOpRes;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author yeyouhuan
 *
 */
public class DBOperationUtil {
	public static void commonSave(BaseNetManager netManager, ChannelHandlerContext ctx, String tableName, Object key, Object value) {
		commonSave(netManager, ctx, new CommonSave(tableName, key, value));
	}
	public static void commonSave(BaseNetManager netManager, ChannelHandlerContext ctx, CommonSave op) {
		var message = CommonSaveOp.newBuilder();
		message.addSaveOps(op.getBuilder());
		netManager.writeAndFlush(ctx, message.build());
	}
	public static void commonSave(BaseNetManager netManager, ChannelHandlerContext ctx, List<CommonSave> ops) {
		if (ops.size() <= 0) {
			return;
		}
		var message = CommonSaveOp.newBuilder();
		for (var op : ops) {
			message.addSaveOps(op.getBuilder());
		}
		netManager.writeAndFlush(ctx, message.build());
	}
	public static void rpcCommonSave(BaseNetManager netManager, ChannelHandlerContext ctx, String tableName, Object key, Object value, CommonDBParam param) {
		rpcCommonSave(netManager, ctx, new CommonSave(tableName, key, value), param);
	}
	public static void rpcCommonSave(BaseNetManager netManager, ChannelHandlerContext ctx, CommonSave op, CommonDBParam param) {
		var message = RpcCommonSaveOp.newBuilder();
		message.addSaveOps(op.getBuilder());
		message.setOpType(param.getOpType());
		netManager.sendRpc(ctx, message.build(), RpcCommonSaveOpRes.class, param);
	}
	public static void rpcCommonSave(BaseNetManager netManager, ChannelHandlerContext ctx, List<CommonSave> ops, CommonDBParam param) {
		if (ops.size() <= 0) {
			return;
		}
		var message = RpcCommonSaveOp.newBuilder();
		for (var op : ops) {
			message.addSaveOps(op.getBuilder());
		}
		message.setOpType(param.getOpType());
		netManager.sendRpc(ctx, message.build(), RpcCommonSaveOpRes.class, param);
	}
	public static void rpcCommonLoad(BaseNetManager netManager, ChannelHandlerContext ctx, String tableName, Object key, CommonDBParam param) {
		rpcCommonLoad(netManager, ctx, new CommonLoad(tableName, key), param);
	}
	public static void rpcCommonLoad(BaseNetManager netManager, ChannelHandlerContext ctx, CommonLoad op, CommonDBParam param) {
		var message = RpcCommonLoadOp.newBuilder();
		message.addLoadOps(op.getBuilder());
		message.setOpType(param.getOpType());
		netManager.sendRpc(ctx, message.build(), RpcCommonLoadOpRes.class, param);
	}
	public static void rpcCommonLoad(BaseNetManager netManager, ChannelHandlerContext ctx, List<CommonLoad> ops, CommonDBParam param) {
		if (ops.size() <= 0) {
			return;
		}
		var message = RpcCommonLoadOp.newBuilder();
		for (var op : ops) {
			message.addLoadOps(op.getBuilder());
		}
		message.setOpType(param.getOpType());
		netManager.sendRpc(ctx, message.build(), RpcCommonLoadOpRes.class, param);
	}
}
