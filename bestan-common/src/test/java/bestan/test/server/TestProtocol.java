package bestan.test.server;

import com.google.protobuf.Message;

import bestan.common.message.MessageFactory;
import bestan.common.net.AbstractProtocol;
import bestan.common.protobuf.Proto;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author yeyouhuan
 *
 */
public class TestProtocol extends AbstractProtocol {
	private static final Proto.BaseProto baseMessageInstance = Proto.BaseProto.getDefaultInstance();
	
	public TestProtocol(ChannelHandlerContext ctx, Message message) {
		super(ctx, message);
	}
	public TestProtocol() {
		super(null, null);
	}

	@Override
	public Message encode(Message message) {
		var builder = baseMessageInstance.newBuilderForType();
		builder.setMessageId(MessageFactory.getMessageIndex(message));
		builder.setMessageData(message.toByteString());
		return builder.build();
	}

	@Override
	public void run() {
		
	}

	@Override
	protected Message getBaseMessage() {
		return baseMessageInstance;
	}

	@Override
	protected AbstractProtocol newProtocol(ChannelHandlerContext ctx, Message message) {
		// TODO Auto-generated method stub
		return new TestProtocol(ctx, message);
	}
}
