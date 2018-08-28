package bestan.common.logic;

import com.google.protobuf.Message;

import bestan.common.guid.Guid;
import bestan.common.timer.BTimer;
import io.netty.channel.Channel;

public abstract class BasePlayer extends BaseObject {
	protected Channel channel;
	
	public BasePlayer(Guid guid) {
		super(guid);
		BTimer.attach(this, Gmatrix.getInstance().getServerConfig().playerTickInterval);
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
