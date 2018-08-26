package bestan.common.message;

import bestan.common.net.AbstractProtocol;

/**
 * message handle 处理接口
 * @author yeyouhuan
 *
 */
public interface IMessageHandle {
	void processProtocol(AbstractProtocol protocol) throws Exception;
}
