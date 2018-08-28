package bestan.common.logic;

import com.google.protobuf.Message;

import bestan.common.guid.Guid;
import bestan.common.message.pack.CommonSaveRetcode;
import bestan.common.net.AbstractProtocol;
import bestan.common.protobuf.Proto.RpcCommonSaveOp;

public interface IObject {
	Guid getGuid();
	void lockObject();
	void unlockObject();

	/**
	 * 通用保存操作结果，已经在上层加锁了
	 * @param arg
	 * @param res
	 * @param opType
	 */
	default void commonSaveReply(RpcCommonSaveOp arg, int opType, CommonSaveRetcode retcode) {
		
	}

	/**
	 * 处理消息，已经加锁了
	 * @param messageId
	 * @param message
	 */
	default void processMessage(int messageId, Message message) {
	}
	
	default void executeMessage(Message message) {
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
}
