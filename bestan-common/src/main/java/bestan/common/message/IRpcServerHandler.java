package bestan.common.message;

import com.google.protobuf.Message;

import bestan.common.net.AbstractProtocol;

/**
 * @author yeyouhuan
 *
 */
public interface IRpcServerHandler extends IMessageHandler {
	void server(AbstractProtocol protocol, Message arg, Message.Builder res);
	
	@Override
	default void processProtocol(AbstractProtocol protocol) throws Exception {
		//不处理
	}
}
