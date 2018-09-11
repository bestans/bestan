package bestan.common.message;

import com.google.protobuf.Message;

import bestan.common.guid.Guid;
import bestan.common.logic.FormatException;
import bestan.common.logic.IObject;
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
		throw new FormatException("cannot run processProtocol:it's IObjectHandler.");
	}
	
	/**
	 * 处理协议，已经在上层加锁/解锁
	 * @param object 处理协议的对象
	 * @param message 协议内容
	 */
	default void runObjectProcess(Guid guid, int messageId, Message message) throws Exception {
		var object = ObjectManager.getInstance().getObject(guid);
		if (object == null) {
			throw new FormatException("%s cannot find object:messageId=%s,guid=%s", getClass().getSimpleName(), messageId, guid);
		}
		object.lockObject();
		try
		{
			process(object, messageId, message);
		} finally {
			object.unlockObject();
		}
	}
	
	void process(IObject object, int messageId, Message message) throws Exception;
	
	@Override
	default long getThreadIndex(Message message) {
		return ((BaseObjectProto)message).getGuid();
	}
}
