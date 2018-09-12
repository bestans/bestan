package bestan.common.logic;

import com.google.protobuf.Message;

import bestan.common.guid.Guid;
import bestan.common.log.Glog;
import bestan.common.net.AbstractProtocol;
import bestan.common.net.BaseNetManager;
import bestan.common.protobuf.Proto.RpcCommonSaveOp;
import bestan.common.protobuf.Proto.RpcCommonSaveOpRes;
import io.netty.channel.ChannelHandlerContext;

public interface IObject {
	Guid getGuid();
	void lockObject();
	void unlockObject();
	
	/**
	 * 处理消息，已经在上层加锁/解锁了
	 * @param messageId 消息索引
	 * @param message 消息内容
	 */
	default void processMessage(int messageId, Message message) {
	}
	
	default void Tick() {
		
	}
	
	default void sendCallback(IObject dst, Message arg) {
		ProtocolManager.getInstance().sendCallback(this, dst, arg);
	}

	default void sendMessage(IObject dst, Message message) {
		ObjectManager.getInstance().sendMessage(dst, message);
	}

	default void sendMessage(Guid guid, Message message) {
		ObjectManager.getInstance().sendMessage(guid, message);
	}
	
	default Message callbackExecute(Message protocol) {
		return null;
	}
	
	default void callbackReply(boolean success, Throwable t, AbstractProtocol arg, AbstractProtocol res) {
		
	}
	
	OBJECT_TYPE getObjectType();
	
	public enum OBJECT_TYPE {
		PLAYER,
		MANAGER,
	}
	
	default void writeAndFlush(ChannelHandlerContext ctx, BaseNetManager netManager, Message message) {
		netManager.writeAndFlush(ctx, this, message);
	}

	default void rpcCommonSaveClient(RpcCommonSaveOp arg, RpcCommonSaveOpRes res, int opType) {
		Glog.debug("{} rpcCommonSaveClient:client:res={},opType={}", getClass().getSimpleName(), res.getRetcode(), opType);
	}
	default void rpcCommonSaveTimeout(RpcCommonSaveOp arg, int opType) {
		Glog.debug("{} rpcCommonSaveClient:Timeout:opType={}", getClass().getSimpleName(), opType);
	}
}
