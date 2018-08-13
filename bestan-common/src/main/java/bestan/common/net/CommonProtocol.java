package bestan.common.net;

import com.google.protobuf.Message;

import bestan.common.logic.ObjectManager;

public class CommonProtocol extends AbstractProtocol {
	protected Message message;
	
	public CommonProtocol(ProtocolHeader header, Message message) {
		super(header);
		this.message = message;
	}

	@Override
	public void run() {
		var obj = ObjectManager.getInstance().getObject(header.getGuid());
		if (obj != null) {
			obj.executeProtocol(header, message);
		}
	}
}
