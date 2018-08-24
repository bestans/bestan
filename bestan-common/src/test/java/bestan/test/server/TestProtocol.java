package bestan.test.server;

import com.google.protobuf.Message;

import bestan.common.logic.FormatException;
import bestan.common.message.MessageFactory;
import bestan.common.net.AbstractProtocol;
import bestan.common.net.IProtocol;
import bestan.common.net.ProtocolHeader;
import bestan.common.protobuf.Proto;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author yeyouhuan
 *
 */
public class TestProtocol extends AbstractProtocol {
	private static final Proto.BaseProto baseMessageInstance = Proto.BaseProto.getDefaultInstance();
	
	public TestProtocol(ProtocolHeader header, ChannelHandlerContext ctx, Message.Builder message) {
		super(header, ctx, message);
	}
	public TestProtocol() {
		super(null, null, null);
	}

	@Override
	public Message encode(Message message) {
		var builder = baseMessageInstance.newBuilderForType();
		builder.setMessageId(MessageFactory.getMessageIndex(message));
		builder.setMessageData(message.toByteString());
		return builder.build();
	}

	@Override
	public IProtocol decode(ChannelHandlerContext ctx, byte[] data) throws Exception {
		var base = baseMessageInstance.newBuilderForType().mergeFrom(data);
		int messageId = base.getMessageId();
		var messageInstance = MessageFactory.getMessageInstance(messageId);
		if (messageInstance == null) {
			throw new FormatException("%s:cannot find message(%s)", getClass().getSimpleName(), messageId);
		}
		var messageBuilder = messageInstance.newBuilderForType().mergeFrom(base.getMessageData());
		
		return new TestProtocol(new ProtocolHeader(messageId), ctx, messageBuilder);
	}
	
	@Override
	protected AbstractProtocol newProtocol(ProtocolHeader header, ChannelHandlerContext ctx, Message.Builder message) {
		// TODO Auto-generated method stub
		return null;
	}
}
