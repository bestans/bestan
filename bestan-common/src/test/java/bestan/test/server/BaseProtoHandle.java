package bestan.test.server;

import bestan.common.log.Glog;
import bestan.common.message.IMessageHandle;
import bestan.common.net.AbstractProtocol;

/**
 * @author yeyouhuan
 *
 */
public class BaseProtoHandle implements IMessageHandle {

	@Override
	public void processProtocol(AbstractProtocol protocol) {
		Glog.debug("BaseProtoHandle:message={}", protocol.getMessage());
	}
}
