package bestan.common.event;

import bestan.common.logic.ServerConfig;
import bestan.common.module.IModule;
import bestan.common.thread.BExecutor;

public class EventBusMannager implements IModule {
	private static BExecutor workExecutor = null;
	
	public static void postEvent(IEvent event) {
		workExecutor.execute(event);
	}

	@Override
	public void startup(ServerConfig config) {
		workExecutor = config.workExecutor;
	}
}
