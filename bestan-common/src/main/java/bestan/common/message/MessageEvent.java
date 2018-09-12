package bestan.common.message;

import com.google.protobuf.Message;

import bestan.common.event.IEvent;
import bestan.common.guid.Guid;
import bestan.common.logic.ObjectManager;

/**
 * @author yeyouhuan
 *
 */
public class MessageEvent implements IEvent {
	private Guid guid;
	private Message message;
	
	public MessageEvent(Guid guid, Message message) {
		this.guid = guid;
		this.message = message;
	}
	
	public Guid getGuid() {
		return guid;
	}
	
	public Message getMessage() {
		return message;
	}

	@Override
	public void run() {
		var object = ObjectManager.getInstance().getObject(guid);
		if (object == null) {
			return;
		}
		var messageId = MessageFactory.getMessageIndex(message);
		object.lockObject();
		try {
			object.processMessage(messageId, message);
		} finally {
			object.unlockObject();
		}
	}
}
