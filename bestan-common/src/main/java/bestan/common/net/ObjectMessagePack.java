package bestan.common.net;

import com.google.protobuf.Message;

import bestan.common.guid.Guid;

/**
 * @author yeyouhuan
 *
 */
public class ObjectMessagePack extends MessagePack {
	private Guid guid;
	
	public ObjectMessagePack(Guid guid, Message message) {
		super(message);
		this.guid = guid;
	}

	public ObjectMessagePack(long guid, Message message) {
		super(message);
		this.guid = new Guid(guid);
	}
	
	public Guid getGuid() {
		return guid;
	}
}
