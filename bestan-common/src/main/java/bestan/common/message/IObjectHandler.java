package bestan.common.message;

import com.google.protobuf.Message;

import bestan.common.guid.Guid;
import bestan.common.logic.BasePlayer;
import bestan.common.logic.FormatException;
import bestan.common.logic.ObjectManager;
import bestan.common.net.AbstractProtocol;
import bestan.common.protobuf.Proto.BaseObjectProto;

/**
 * @author yeyouhuan
 *
 */
public interface IObjectHandler extends IMessageHandler {

	@Override
	default boolean isObjectHandler() {
		return true;
	}
	@Override
	default void processProtocol(AbstractProtocol protocol) throws Exception {
		var baseMessage = (BaseObjectProto)protocol.getMessage();
		var messageId = protocol.getMessageId();
		var handler = MessageFactory.getMessageHandle(messageId);
		if (handler == null) {
			throw new FormatException("IObjectHandler:cannot find message handler:messageId=%s,guid=%s", messageId, baseMessage.getGuid());
		}
		var object = ObjectManager.getInstance().getObject(new Guid(baseMessage.getGuid()));
		if (object == null) {
			throw new FormatException("IObjectHandler:cannot find object:messageId=%s,guid=%s", messageId, baseMessage.getGuid());
		}
		var messageInstance = MessageFactory.getMessageInstance(messageId);
		if (messageInstance == null) {
			throw new FormatException("IObjectHandler:cannot find message instance:messageId=%s,guid=%s", messageId, baseMessage.getGuid());
		}
		var message = messageInstance.newBuilderForType().mergeFrom(baseMessage.getMessageData()).build();
		object.lockObject();
		try {
			process(object, message);
		} finally {
			object.unlockObject();
		}
	}
	
	/**
	 * 处理协议，已经在上层加锁/解锁
	 * @param object 处理协议的对象
	 * @param message 协议内容
	 */
	void process(BasePlayer player, Message message);
	
	@Override
	default long getThreadIndex(Message message) {
		return ((BaseObjectProto)message).getGuid();
	}
}
