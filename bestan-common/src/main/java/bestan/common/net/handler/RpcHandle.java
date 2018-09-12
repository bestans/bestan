package bestan.common.net.handler;

import com.google.protobuf.Message;

import bestan.common.logic.FormatException;
import bestan.common.message.MessageFactory;
import bestan.common.net.AbstractProtocol;
import bestan.common.net.ObjectMessagePack;
import bestan.common.net.RpcManager;
import bestan.common.protobuf.Proto;

/**
 * @author yeyouhuan
 *
 */
public class RpcHandle implements IMessageHandler {
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
			
			var serverHandler = MessageFactory.getRpcServerHandler(message.getArgMessageId());
			if (serverHandler == null) {
				throw new FormatException("RpcHandle:ProcessProtocol failed:cannot find serverHandler:argMessageId=%s,", message.getArgMessageId());
			}
			serverHandler.server(protocol, argMessage, resBuilder);
			
			//将结果返回给client
			message.setIsRequest(false);
			message.setMessageData(resBuilder.build().toByteString());
			protocol.getChannelHandlerContext().writeAndFlush(new ObjectMessagePack(protocol.getGuidValue(), (Message)message.build()));
		} else {
			var rpcObject = RpcManager.getInstance().get(message.getRpcIndex());
			if (rpcObject == null) {
				return;
			}
			resBuilder.mergeFrom(message.getMessageData());

			var clientHandler = MessageFactory.getRpcClientHandler(message.getResMessageId());
			if (clientHandler == null) {
				throw new FormatException("RpcHandle:ProcessProtocol failed:cannot find clientHandler:argMessageId=%s,", message.getResMessageId());
			}
			clientHandler.client(protocol, rpcObject.getArgMessage(), resBuilder.build(), rpcObject.getParam());
		}
	}
}
