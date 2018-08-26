package bestan.common.message;

import bestan.common.logic.FormatException;
import bestan.common.net.AbstractProtocol;
import bestan.common.net.RpcManager;
import bestan.common.protobuf.Proto;

/**
 * @author yeyouhuan
 *
 */
public class RpcHandle implements IMessageHandle {
	@Override
	public void processProtocol(AbstractProtocol protocol) throws Exception {
		var message = (Proto.RpcMessage.Builder)protocol.getMessage().toBuilder();

		var res = MessageFactory.getMessageInstance(message.getResMessageId());
		if (res == null) {
			throw new FormatException("RpcHandle:ProcessProtocol failed:invalid resMessageId=%s,", message.getResMessageId());
		}
		var resBuilder = res.newBuilderForType();
		
		if (message.getIsRequest()) {
			var arg = MessageFactory.getMessageInstance(message.getArgMessageId());
			if (arg == null) {
				throw new FormatException("RpcHandle:ProcessProtocol failed:invalid argMessageId=%s,", message.getArgMessageId());
			}
			var argMessage = arg.newBuilderForType().mergeFrom(message.getMessageData()).build();
			
			var serverHandler = MessageFactory.getRpcServerHandler(message.getResMessageId());
			if (serverHandler == null) {
				throw new FormatException("RpcHandle:ProcessProtocol failed:cannot find serverHandler:resMessageId=%s,", message.getResMessageId());
			}
			serverHandler.server(protocol, argMessage, resBuilder);
			
			//将结果返回给client
			message.setIsRequest(false);
			message.setMessageData(resBuilder.build().toByteString());
			protocol.getChannelHandlerContext().writeAndFlush(message.build());
		} else {
			var rpcObject = RpcManager.getInstance().get(message.getRpcIndex());
			if (rpcObject == null) {
				return;
			}
			resBuilder.mergeFrom(message.getMessageData());

			var clientHandler = MessageFactory.getRpcClientHandler(message.getArgMessageId());
			if (clientHandler == null) {
				throw new FormatException("RpcHandle:ProcessProtocol failed:cannot find clientHandler:argMessageId=%s,", message.getArgMessageId());
			}
			clientHandler.client(protocol, rpcObject.getArgMessage(), resBuilder.build(), rpcObject.getParam());
		}
	}
}
