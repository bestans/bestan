package bestan.common.eventbus;

import com.google.protobuf.Message;

public abstract class BasePlayerTask implements Runnable {
	private BasePlayer player;
	private Message message;
	
	BasePlayerTask(BasePlayer player, Message message){
		this.player = player;
		this.message = message;
	}
	
	@Override
	public void run() {
		player.processCommand(message);
	}
}
