package bestan.common.logic;

import com.google.protobuf.Message;

import bestan.common.guid.Guid;
import bestan.common.net.AbstractProtocol;
import bestan.common.net.ProtocolHeader;

public interface IObject {
	Guid getGuid();
	void lockObject();
	void unlockObject();

	default void processProtocol(ProtocolHeader header, Message message) {
		
	}
	default void executeProtocol(ProtocolHeader header, Message message) {
		lockObject();
		try {
			processProtocol(header, message);
		}finally {
			unlockObject();
		}
	}
	
	default void processMessage(Message message) {
		
	}
	default void execute(Message message) {
		lockObject();
		try {
			processMessage(message);
		}finally {
			unlockObject();
		}
	}
	
	default void Tick() {
		
	}
	
	default void sendCallback(IObject dst, AbstractProtocol arg) {
		ProtocolManager.getInstance().sendCallback(this, dst, arg);
	}

	default void sendMessage(IObject dst, Message message) {
		ObjectManager.getInstance().sendMessage(dst, message);
	}

	default void sendMessage(Guid guid, Message message) {
		ObjectManager.getInstance().sendMessage(guid, message);
	}
	
	default AbstractProtocol call(AbstractProtocol protocol) {
		return null;
	}
	
	default void callReply(boolean success, Throwable t, AbstractProtocol arg, AbstractProtocol res) {
		
	}
}
