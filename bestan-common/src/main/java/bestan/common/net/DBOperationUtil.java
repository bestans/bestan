package bestan.common.net;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.protobuf.ByteString;
import com.google.protobuf.Message;

import bestan.common.message.MessageFactory;
import bestan.common.protobuf.Proto;
import bestan.common.protobuf.Proto.CommonSaveOp;
import bestan.common.protobuf.Proto.DBCommonKey;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author yeyouhuan
 *
 */
public class DBOperationUtil {
	private static DBCommonKey.Builder getCommonKey(Object key) {
		var keyBuilder = DBCommonKey.newBuilder();
		if (key instanceof Integer) {
			keyBuilder.setIntKey((Integer)key);	
		} else if (key instanceof Long) {
			keyBuilder.setLongKey((Long)key);
		} else if (key instanceof Message) {
			keyBuilder.setMessagekey(((Message)key).toByteString());
		} else {
			return null;
		}
		return keyBuilder;
	}
	private static Proto.CommonSave.Builder getSaveOp(CommonSave op) {
		var keyBuilder = getCommonKey(op.key);
		if (null == keyBuilder) {
			return null;
		}
		keyBuilder.setTableName(ByteString.copyFrom(op.tableName.getBytes()));
		keyBuilder.setValueMessageId(MessageFactory.getMessageIndex(op.value));
		var builder = Proto.CommonSave.newBuilder();
		builder.setKey(keyBuilder);
		builder.setValue(op.value.toByteString());
		return builder;
	}
	private static List<Proto.CommonSave.Builder> getSaveOps(List<CommonSave> ops) {
		List<Proto.CommonSave.Builder> builders = Lists.newArrayList();
		for (var op : ops) {
			var builder = getSaveOp(op);
			if (builder == null) {
				continue;
			}
			builders.add(builder);
		}
		return builders;
	}
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
		var opBuilder = getSaveOp(op);
		if (null == opBuilder) {
			return;
		}
		message.addSaveOps(opBuilder);
		ctx.writeAndFlush(message.build());
	}
	public static void commonSave(ChannelHandlerContext ctx, List<CommonSave> ops) {
		var message = CommonSaveOp.newBuilder();
		var opBuilders = getSaveOps(ops);
		for (var opBuilder : opBuilders) {
			message.addSaveOps(opBuilder);
		}
		ctx.writeAndFlush(message.build());
	}
}
