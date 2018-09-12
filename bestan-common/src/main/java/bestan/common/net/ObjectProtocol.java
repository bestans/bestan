package bestan.common.net;

import com.google.protobuf.Message;

import bestan.common.guid.Guid;
import bestan.common.logic.FormatException;
import bestan.common.logic.IObject;
import bestan.common.message.MessageFactory;
import bestan.common.net.handler.IMessageHandler;
import bestan.common.net.handler.IObjectHandler;
import bestan.common.protobuf.Proto;
import bestan.common.protobuf.Proto.BaseObjectProto;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author yeyouhuan
 *
 */
public class ObjectProtocol extends AbstractProtocol {
	private static final Proto.BaseObjectProto baseMessageInstance = Proto.BaseObjectProto.getDefaultInstance();
	
	private Guid guid;
	
	public ObjectProtocol(ChannelHandlerContext ctx, int messageId, Message message, long guid) {
		super(ctx, messageId, message);
		this.guid = new Guid(guid);
	}

	public Guid getGuid() {
		return guid;
	}
	
	public ObjectProtocol() {
		super(null, 0, null);
	}
	
	@Override
	public Message encode(MessagePack message) {
		var builder = baseMessageInstance.newBuilderForType();
		builder.setMessageId(MessageFactory.getMessageIndex(message.getMessage()));
		builder.setMessageData(message.getMessage().toByteString());
		builder.setGuid((((ObjectMessagePack)message).getGuid().getValue()));
		return builder.build();
	}

	@Override
	public Message decode(byte[] data) throws Exception {
		return baseMessageInstance.newBuilderForType().mergeFrom(data).build();
	}

	@Override
	public IProtocol makeProtocol(ChannelHandlerContext ctx, Message message) throws Exception {
		var base = (BaseObjectProto)message;
		int messageId = base.getMessageId();
		var messageInstance = MessageFactory.getMessageInstance(messageId);
		if (messageInstance == null) {
			throw new FormatException("%s:cannot find message(%s)", getClass().getSimpleName(), messageId);
		}
		var messageBuilder = messageInstance.newBuilderForType().mergeFrom(base.getMessageData());
		return new ObjectProtocol(ctx, messageId, messageBuilder.build(), base.getGuid());
	}
	
	@Override
	protected void runProtocol(IMessageHandler handler) throws Exception {
		if (handler.isObjectHandler()) {
			var objectHandler = (IObjectHandler)handler;
			objectHandler.runObjectProcess(guid, messageId, message);
		} else {
			super.runProtocol(handler);
		}
	}
	
	@Override
	public long getGuidValue() {
		return guid.getValue();
	}

	@Override
	public MessagePack packMessage(IObject object, Message message) {
		return new ObjectMessagePack(object.getGuid(), message);
	}
	
	@Override
	public MessagePack packMessage(Message message) {
		return new ObjectMessagePack(Guid.ZERO, message);
	}
}
