package bestan.common.event;

import com.google.protobuf.Message;

public interface ICommandHandle {
	default void processCommand(BasePlayer player, Message message) {
		player.processCommand(message);
	}
}
