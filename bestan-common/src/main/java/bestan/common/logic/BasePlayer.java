package bestan.common.logic;

import com.google.protobuf.Message;

import bestan.common.guid.Guid;
import io.netty.channel.Channel;

public abstract class BasePlayer extends BaseObject {
	protected Channel channel;
	
	public BasePlayer(Guid guid) {
		super(guid);
	}
	
	public void setChannel(Channel channel) {
		lockObject();
		this.channel = channel;
		unlockObject();
	}

	public void sendProtocol(Message message) {
		channel.writeAndFlush(message);
	}
}
