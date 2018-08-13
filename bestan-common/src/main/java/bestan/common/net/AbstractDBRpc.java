package bestan.common.net;

import com.google.protobuf.Message;

public abstract class AbstractDBRpc extends AbstractRpc {

	public AbstractDBRpc(INetSession netSession, BaseRpcHeader header, Message arg, Message res) {
		super(netSession, header, arg, res);
		// TODO Auto-generated constructor stub
	}

}
