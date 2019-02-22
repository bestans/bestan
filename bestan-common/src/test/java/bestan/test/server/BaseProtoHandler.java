package bestan.test.server;

import bestan.common.log.Glog;
import bestan.common.net.AbstractProtocol;
import bestan.common.net.handler.IMessageHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

/**
 * @author yeyouhuan
 *
 */
public class BaseProtoHandler implements IMessageHandler {

	@Override
	public void processProtocol(AbstractProtocol protocol) {
		Glog.debug("BaseProtoHandle:message={}", protocol.getMessage());
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		var channelFuture = protocol.writeAndFlush(protocol.getMessage());
		channelFuture.addListener(new GenericFutureListener<Future<? super Void>>() {

			@Override
			public void operationComplete(Future<? super Void> future) throws Exception {
				Glog.debug("BaseProtoHandler:future={}", future);
			}
		});
	}
}
