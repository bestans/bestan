package bestan.common.message;

import com.google.protobuf.Message;

import bestan.common.log.Glog;
import bestan.common.logic.FormatException;
import bestan.common.logic.IObject;
import bestan.common.logic.ObjectManager;
import bestan.common.net.AbstractProtocol;
import bestan.common.net.NetConst;

/**
 * @author yeyouhuan
 *
 */
public interface IObjectHandler extends IMessageHandler {
	@Override
	default void processProtocol(AbstractProtocol protocol) throws Exception{
		var channel = protocol.getChannelHandlerContext().channel();
		if (!channel.hasAttr(NetConst.GUID_ATTR_INDEX)) {
			var exception = new FormatException("processProtocol failed:can't find channel guid,handler=%s", getClass().getSimpleName());
			protocol.getChannelHandlerContext().fireExceptionCaught(exception);
			return;
		}
		var guid = channel.attr(NetConst.GUID_ATTR_INDEX).get();
		var object = ObjectManager.getInstance().getObject(guid);
		if (null == object) {
			Glog.error("processProtocol failed:can't find object,handler={},guid={}",
					getClass().getSimpleName(), guid);
			return;
		}
		object.lockObject();
		try {
			process(object, protocol.getMessage(), protocol);
		} finally {
			object.unlockObject();
		}
	}
	
	/**
	 * @param object 非空对象，已经加锁了
	 * @param message 待处理的消息
	 * @param protocol 解析过来的协议
	 */
	void process(IObject object, Message message, AbstractProtocol protocol);
}
