package bestan.common.net;

public class ProtocolHeader {
	protected int messageId;
	protected boolean isRpc = false;
	protected boolean isRequest = false;
	
	public ProtocolHeader(int messageId) {
		this.messageId = messageId;
	}
	
	public int getMessageId() {
		return messageId;
	}
}
