package bestan.common.eventbus;

import java.util.List;
import java.util.concurrent.Executor;

public class CategoryEventBus<T extends IEvent> implements IEventBus<T> {
	private List<Executor> executors;
	
	CategoryEventBus(List<Executor> executors){
		this.executors = executors;
	}
	
	@Override
	public void post(T event) {
		executors.get((int) (event.getID() % executors.size())).execute(event);
	}
}
