package bestan.common.eventbus;

import java.util.concurrent.Executor;

public class BEventBus<T extends Runnable> implements IEventBus<T> {
	private Executor executor;
	
	public BEventBus(Executor executor) {
		this.executor = executor;
	}

	@Override
	public void post(T event) {
		executor.execute(event);
	}
}