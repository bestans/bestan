package bestan.common.net;

import com.google.protobuf.Message;

/**
 * @author yeyouhuan
 *
 */
public class MessagePack {
	private Message message;
	
	public MessagePack(Message message) {
		this.message = message;
	}
	
	public Message getMessage() {
		return message;
	}
}
