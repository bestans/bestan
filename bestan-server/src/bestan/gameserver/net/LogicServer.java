package bestan.gameserver.net;

import bestan.common.server.AbstractLogicServer;
import bestan.log.GLog;

public class LogicServer extends AbstractLogicServer {
	private static LogicServer instance;
	
	protected LogicServer() {
		super("LogicServer");

		
	}
	
	public static LogicServer getInstance() {
		if (instance == null) {
			instance = new LogicServer();
		}
		
		return instance;
	}

	@Override
	public void run() {
		while (keepRunning) {
			try {
				doMessageHandlerTick();
			} catch (Exception e) {
				GLog.log.error("Main Thread catch Handler exception:", e);
			}
		}
	}
}
