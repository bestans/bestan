package bestan.common.net;

import com.google.protobuf.Message;

public abstract class AbstractRpc extends AbstractProtocol {
	protected INetSession netSession;
	protected BaseRpcHeader header;
	protected Message arg;
	protected Message res;
	
	public AbstractRpc(INetSession netSession, BaseRpcHeader header, Message arg, Message res) {
		this.netSession = netSession;
		this.header = header;
		this.arg = arg;
		this.res = res;
	}
	
	public void Client() {
		
	}
	
	public void Server() {
		
	}
	
	@Override
	public void run() {
		if (header.isRequest) {
			header.isRequest = false; 
			Server();
			netSession.send(fillReplyMessage());
			return;
		}

		Client();
	}
	
	public abstract Message fillReplyMessage();
}
