package bestan.test.server;

import bestan.common.log.Glog;
import bestan.common.message.IMessageHandler;
import bestan.common.net.AbstractProtocol;

/**
 * @author yeyouhuan
 *
 */
public class BaseProtoHandle implements IMessageHandler {

	@Override
	public void processProtocol(AbstractProtocol protocol) {
		Glog.debug("BaseProtoHandle:message={}", protocol.getMessage());
	}
}
