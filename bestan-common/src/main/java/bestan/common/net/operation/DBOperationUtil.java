package bestan.common.net.operation;

import java.util.List;

import bestan.common.net.BaseNetManager;
import bestan.common.protobuf.Proto.CommonSaveOp;
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
	public static void RpcCommonSave(BaseNetManager netManager, ChannelHandlerContext ctx, String tableName, Object key, Object value, CommonSaveParam param) {
		RpcCommonSave(netManager, ctx, new CommonSave(tableName, key, value), param);
	}
	public static void RpcCommonSave(BaseNetManager netManager, ChannelHandlerContext ctx, CommonSave op, CommonSaveParam param) {
		var message = RpcCommonSaveOp.newBuilder();
		message.addSaveOps(op.getBuilder());
		netManager.sendRpc(ctx, message.build(), RpcCommonSaveOpRes.class, param);
	}
	public static void RpcCommonSave(BaseNetManager netManager, ChannelHandlerContext ctx, List<CommonSave> ops, CommonSaveParam param) {
		if (ops.size() <= 0) {
			return;
		}
		var message = RpcCommonSaveOp.newBuilder();
		for (var op : ops) {
			message.addSaveOps(op.getBuilder());
		}
		netManager.sendRpc(ctx, message.build(), RpcCommonSaveOpRes.class, param);
	}
}
