package bestan.common.thread;

import bestan.common.event.IEvent;

public interface BExecutor {
	void execute(IEvent event);
}
