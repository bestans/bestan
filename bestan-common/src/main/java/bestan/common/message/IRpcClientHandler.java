package bestan.common.message;

import com.google.protobuf.Message;

import bestan.common.net.AbstractProtocol;
import bestan.common.net.RpcManager.RpcObject;

/**
 * @author yeyouhuan
 *
 */
public interface IRpcClientHandler extends IMessageHandle {
	void client(AbstractProtocol protocol, Message arg, Message res, Object param);
	default void OnTimeout(AbstractProtocol protocol, RpcObject rpc, Message arg, Object param) {
		
	}
	
	@Override
	default void processProtocol(AbstractProtocol protocol) throws Exception {
		//不处理
	}
}