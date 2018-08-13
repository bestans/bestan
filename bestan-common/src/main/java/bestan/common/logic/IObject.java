package bestan.common.logic;

import bestan.common.net.AbstractProtocol;

public interface IObject {
	default void process(AbstractProtocol player) {
		
	}
	
	default void sendCallback(IObject dst, AbstractProtocol arg) {
		ProtocolManager.getInstance().sendCallback(this, dst, arg);
	}

	default AbstractProtocol call(AbstractProtocol protocol) {
		return null;
	}
	
	default void callReply(boolean success, Throwable t, AbstractProtocol arg, AbstractProtocol res) {
		
	}
}
