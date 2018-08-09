package bestan.common.event;

import bestan.common.thread.BExecutor;

public class BEventBus<T extends IEvent> implements IEventBus<T> {
	private BExecutor executor;
	
	public BEventBus(BExecutor executor) {
		this.executor = executor;
	}

	@Override
	public void post(T event) {
		executor.execute(event);
	}
}
