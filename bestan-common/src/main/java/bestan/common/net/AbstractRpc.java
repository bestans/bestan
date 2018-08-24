package bestan.common.net;

import com.google.protobuf.Message;

import io.netty.channel.ChannelHandlerContext;

public abstract class AbstractRpc extends AbstractProtocol {
	public AbstractRpc(ProtocolHeader header, ChannelHandlerContext ctx, Message message) {
		
	}

	private boolean isRequest = false;
	protected int argMessageId;
	protected int resMessageId;
	protected Object argParam;
	protected Message arg;
	protected Message res;
	
	public int getRpcIndex() {
		return 0;
	}
	
	public int getTimeout() {
		return 0;
	}
	protected void decodeRpcMessage() {
		
	}
	
	public void Client() {
		
	}
	
	public void Server() {
		
	}
	
	//public int 
	@Override
	public void run() {
		if (isRequest) {
			var rpc = RpcManager.getInstance().get(getRpcIndex());
			if (rpc == null) {
				return;
			}
			rpc.Server();
			Server();
			getChannelHandlerContext().writeAndFlush(fillReplyMessage());
			return;
		}

		RpcManager.getInstance().put(this);
		Client();
	}
	
	public abstract Message fillReplyMessage();
}
