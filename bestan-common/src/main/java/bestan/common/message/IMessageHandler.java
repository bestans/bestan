package bestan.common.message;

import bestan.common.net.AbstractProtocol;

/**
 * message handle 处理接口，派生类必须是xxxxhandler
 * @author yeyouhuan
 *
 */
public interface IMessageHandler {
	void processProtocol(AbstractProtocol protocol) throws Exception;
}
