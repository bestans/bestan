package bestan.common.net.nio;

import java.nio.channels.SocketChannel;

import bestan.common.datastruct.Pair;
import bestan.common.net.message.DelayedEventHandler;

import com.google.protobuf.Message;

public class MessagePack {
	private Integer msgId;
	private Message msg;

	@SuppressWarnings("unused")
	private SocketChannel channel;
	private Pair<DelayedEventHandler, Object> delayHandle;
	
	public MessagePack(Integer msgId, Message msg, SocketChannel channel) {
		this.msgId = msgId;
		this.msg = msg;
		this.channel = channel;
		this.delayHandle = null;
	}
	
	public MessagePack(int msgId, Message msg, SocketChannel channel) {
		this.msgId = new Integer(msgId);
		this.msg = msg;
		this.delayHandle = null;
	}
	
	public MessagePack(int msgId, Message msg) {
		this.msgId = new Integer(msgId);
		this.msg = msg;
		this.channel = null;
		this.delayHandle = null;
	}
	
	public MessagePack(Pair<DelayedEventHandler, Object> delayHandle) {
		this.delayHandle = delayHandle;
	}
	
	public MessagePack(DelayedEventHandler handler, Object data) {
		this.delayHandle = new Pair<DelayedEventHandler, Object>(handler, data);
	}
	
	
	public Message getMessage() {
		return this.msg;
	}
	
	public int getMessageId() {
		return this.msgId.intValue();
	}
	
	public Integer getIntegerMessageId() {
		return this.msgId;
	}
	
	public Pair<DelayedEventHandler, Object> getDelayHandle() {
		return this.delayHandle;
	}
	
	public void setChannel(SocketChannel channel) {
		this.channel = channel;
	}
	

	//========================================
}
