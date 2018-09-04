package bestan.common.protobuf;

import com.google.protobuf.Message;

import bestan.common.message.IMessageHandler;
import bestan.common.message.RpcHandle;

/**
 * 消息固定映射，系统使用的消息，按照-1、-2...的顺序编号
 * @author yeyouhuan
 *
 */
public enum MessageFixedEnum {
	INVALID(null, null),
	BASE_RPC(Proto.RpcMessage.class, RpcHandle.class);	//基础rpc
	
	private int messageId;
	private Class<? extends Message> messageCls;
	private Class<? extends IMessageHandler> handleCls;
	MessageFixedEnum(Class<? extends Message> messageCls, Class<? extends IMessageHandler> handleCls) {
		this.messageId = this.ordinal() * -1;
		this.messageCls = messageCls;
		this.handleCls = handleCls;
	}
	
	public int getMessageId() {
		return messageId;
	}
	
	public Class<? extends Message> getMessageClass() {
		return messageCls;
	}
	
	public Class<? extends IMessageHandler> getMessageHandleClass() {
		return handleCls;
	}
}
