package bestan.common.net;

public class RpcHeader extends ProtocolHeader {
	public RpcHeader(int messageId) {
		super(messageId);
		// TODO Auto-generated constructor stub
	}
	public long id = 0;
	public boolean isRequest = true;
	
}
