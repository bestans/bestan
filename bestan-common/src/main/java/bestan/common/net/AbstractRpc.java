package bestan.common.net;

import com.google.protobuf.Message;

public abstract class AbstractRpc extends AbstractProtocol {
	protected RpcHeader header;
	protected Message arg;
	protected Message res;
	
	public AbstractRpc(RpcHeader header, Message arg, Message res) {
		super(null, null);
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
			header.getChannelHandlerContext().writeAndFlush(fillReplyMessage());
			return;
		}

		Client();
	}
	
	public abstract Message fillReplyMessage();
}
