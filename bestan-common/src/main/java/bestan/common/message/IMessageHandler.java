package bestan.common.message;

import com.google.protobuf.Message;

import bestan.common.net.AbstractProtocol;

/**
 * message handler 处理接口，派生类必须是xxxxhandler
 * @author yeyouhuan
 *
 */
public interface IMessageHandler {
	void processProtocol(AbstractProtocol protocol) throws Exception;
	default long getThreadIndex(Message message) {
		return 0;
	}
	
	default boolean isObjectHandler() {
		return false;
	}
}
