package bestan.common.logic;

import com.google.protobuf.Message;

import bestan.common.event.IEvent;
import bestan.common.guid.Guid;

public class MessagePack implements IEvent {
	protected Guid guid;
	protected IObject object;
	protected Message message;
	
	public MessagePack(Guid guid, Message message) {
		this.guid = guid;
		this.message = message;
		this.object = null;
	}
	
	public MessagePack(IObject object, Message message) {
		this.guid = null;
		this.message = message;
		this.object = object;
	}
	
	@Override
	public void run() {
		if (object != null) {
			object.execute(message);
			return;
		}

		var obj = ObjectManager.getInstance().getObject(guid);
		if (obj != null) {
			obj.execute(message);
		}
	}
}
