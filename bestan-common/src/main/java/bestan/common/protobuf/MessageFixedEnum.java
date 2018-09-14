package bestan.common.protobuf;

import com.google.protobuf.Message;

import bestan.common.net.handler.IMessageHandler;
import bestan.common.net.handler.RpcHandle;
import bestan.common.net.operation.RpcCommonLoadClientHandler;
import bestan.common.net.operation.RpcCommonSaveClientHandler;
import bestan.common.protobuf.Proto.RpcCommonLoadOp;
import bestan.common.protobuf.Proto.RpcCommonLoadOpRes;

/**
 * 消息固定映射，系统使用的消息，按照-1、-2...的顺序编号
 * @author yeyouhuan
 *
 */
public enum MessageFixedEnum {
	INVALID(null, null),
	BASE_RPC(Proto.RpcMessage.class, RpcHandle.class),	//基础rpc
	RPC_COMMON_LOAD(RpcCommonLoadOp.class, null),	//通用db载入数据
	RPC_COMMON_LOAD_RES(RpcCommonLoadOpRes.class, RpcCommonLoadClientHandler.class), //通用db载入数据及client处理
	RPC_COMMON_SAVE(Proto.RpcCommonSaveOp.class, null),	//通用db保存rpc
	RPC_COMMON_SAVE_RES(Proto.RpcCommonSaveOpRes.class, RpcCommonSaveClientHandler.class);	//通用db保存rpc返回结果
	
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
