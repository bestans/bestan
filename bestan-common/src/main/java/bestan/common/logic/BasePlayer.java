package bestan.common.logic;

import com.google.protobuf.Message;

import bestan.common.guid.Guid;
import bestan.common.net.BaseNetManager;
import bestan.common.timer.BTimer;
import io.netty.channel.ChannelHandlerContext;

public abstract class BasePlayer extends BaseObject {
	protected ChannelHandlerContext channel;
	
	public BasePlayer(Guid guid) {
		super(guid);
		BTimer.attach(this, Gmatrix.getInstance().getServerConfig().playerTickInterval);
	}
	
	public void setChannel(ChannelHandlerContext channel) {
		lockObject();
		this.channel = channel;
		unlockObject();
	}

	public void writeAndFlush(BaseNetManager netManager, Message message) {
		netManager.writeAndFlush(channel, this, message);
	}
}
