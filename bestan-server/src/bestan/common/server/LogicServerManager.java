package bestan.common.server;


public class LogicServerManager {
	private static LogicServerManager instance;
	
	public static LogicServerManager getInstance() {
		if(null == instance) {
			instance = new LogicServerManager();
		}
		
		return instance;
	}
	
	private AbstractLogicServer logicServer;
	
	public void register(AbstractLogicServer logicServer) {
		this.logicServer = logicServer;
	}
	
	public AbstractLogicServer getLogicServer() {
		return this.logicServer;
	}
	
	
}
