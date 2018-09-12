package bestan.test.server;

import com.google.protobuf.Message;

import bestan.common.logic.FormatException;
import bestan.common.message.MessageFactory;
import bestan.common.net.CommonProtocol;
import bestan.common.net.IProtocol;
import bestan.common.net.MessagePack;
import bestan.common.protobuf.Proto;
import bestan.common.protobuf.Proto.BaseProto;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author yeyouhuan
 *
 */
public class TestProtocol extends CommonProtocol {
	private static final Proto.BaseProto baseMessageInstance = Proto.BaseProto.getDefaultInstance();
	
	public TestProtocol(ChannelHandlerContext ctx, int messageId, Message message) {
		super(ctx, messageId, message);
	}
	public TestProtocol() {
		super(null, 0, null);
	}

	@Override
	public Message encode(MessagePack message) {
		var builder = baseMessageInstance.newBuilderForType();
		builder.setMessageId(MessageFactory.getMessageIndex(message.getMessage()));
		builder.setMessageData(message.getMessage().toByteString());
		return builder.build();
	}

	@Override
	public Message decode(byte[] data) throws Exception {
		return baseMessageInstance.newBuilderForType().mergeFrom(data).build();
	}
	
	@Override
	public IProtocol makeProtocol(ChannelHandlerContext ctx, Message message) throws Exception {
		var base = (BaseProto)message;
		int messageId = base.getMessageId();
		var messageInstance = MessageFactory.getMessageInstance(messageId);
		if (messageInstance == null) {
			throw new FormatException("%s:cannot find message(%s)", getClass().getSimpleName(), messageId);
		}
		var messageBuilder = messageInstance.newBuilderForType().mergeFrom(base.getMessageData());
		return new TestProtocol(ctx, messageId, messageBuilder.build());
	}
}
