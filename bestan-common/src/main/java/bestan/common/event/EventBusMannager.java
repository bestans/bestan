package bestan.common.event;

import bestan.common.module.IModule;
import bestan.common.thread.BExecutor;

public class EventBusMannager {
	private static BExecutor workExecutor;
	
	public static void postEvent(IEvent event) {
		workExecutor.execute(event);
	}

	public static class EventBusModule implements IModule {
		public EventBusModule(BExecutor executor) {
			workExecutor = executor;
		}
		@Override
		public void startup() {
			
		}
	}
}
