package bestan.common.net.message;

import com.google.protobuf.Message;

public interface IMessageRegister {
	void messageRegister();
	
	default void register(int index, Message msgInstance) {
		MessageFactory.getFactory().register(index, msgInstance);
	}
}
