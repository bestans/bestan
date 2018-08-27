package bestan.common.net.operation;

import java.util.List;

import com.google.protobuf.Message;

import bestan.common.net.INetManager;
import bestan.common.protobuf.Proto.CommonSaveOp;
import bestan.common.protobuf.Proto.RpcCommonSaveOp;
import bestan.common.protobuf.Proto.RpcCommonSaveOpRes;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author yeyouhuan
 *
 */
public class DBOperationUtil {
	public static void commonSave(ChannelHandlerContext ctx, String tableName, int key, Message value) {
		commonSave(ctx, new CommonSave(tableName, key, value));
	}
	public static void commonSave(ChannelHandlerContext ctx, String tableName, Long key, Message value) {
		commonSave(ctx, new CommonSave(tableName, key, value));
	}
	public static void commonSave(ChannelHandlerContext ctx, String tableName, Message key, Message value) {
		commonSave(ctx, new CommonSave(tableName, key, value));
	}
	public static void commonSave(ChannelHandlerContext ctx, CommonSave op) {
		var message = CommonSaveOp.newBuilder();
		message.addSaveOps(op.getBuilder());
		ctx.writeAndFlush(message.build());
	}
	public static void commonSave(ChannelHandlerContext ctx, List<CommonSave> ops) {
		if (ops.size() <= 0) {
			return;
		}
		var message = CommonSaveOp.newBuilder();
		for (var op : ops) {
			message.addSaveOps(op.getBuilder());
		}
		ctx.writeAndFlush(message.build());
	}
	
	public static void RpcCommonSave(ChannelHandlerContext ctx, String tableName, int key, Message value, CommonSaveParam param) {
		RpcCommonSave(ctx, new CommonSave(tableName, key, value), param);
	}
	public static void RpcCommonSave(ChannelHandlerContext ctx, String tableName, Long key, Message value, CommonSaveParam param) {
		RpcCommonSave(ctx, new CommonSave(tableName, key, value), param);
	}
	public static void RpcCommonSave(ChannelHandlerContext ctx, String tableName, Message key, Message value, CommonSaveParam param) {
		RpcCommonSave(ctx, new CommonSave(tableName, key, value), param);
	}
	public static void RpcCommonSave(ChannelHandlerContext ctx, CommonSave op, CommonSaveParam param) {
		var message = RpcCommonSaveOp.newBuilder();
		message.addSaveOps(op.getBuilder());
		INetManager.sendRpc(ctx, message.build(), RpcCommonSaveOpRes.class, param);
	}
	public static void RpcCommonSave(ChannelHandlerContext ctx, List<CommonSave> ops, CommonSaveParam param) {
		if (ops.size() <= 0) {
			return;
		}
		var message = RpcCommonSaveOp.newBuilder();
		for (var op : ops) {
			message.addSaveOps(op.getBuilder());
		}
		INetManager.sendRpc(ctx, message.build(), RpcCommonSaveOpRes.class, param);
	}
}
