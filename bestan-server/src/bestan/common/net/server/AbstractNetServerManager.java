package bestan.common.net.server;

import javax.naming.InitialContext;

import bestan.common.datastruct.ServerOption;
import bestan.log.GLog;

public abstract class AbstractNetServerManager extends AbstractNetManager {
	private AbstractServer netServer;
	
	public boolean init(ServerOption option)
	{
		switch (option.serverType) {
		case HTTP_SERVER:
			netServer = new HttpServer(option);
			break;

		default:
			return false;
		}
		
		return true;
	}

	@Override
	public void tick() {
		
	}

	public boolean startServer() {
		try {
			netServer.start();
		} catch (Exception e) {
			GLog.log.error("netserver start failied:{}", e.getMessage());
			return false;
		}
		
		return true;
	}
	
	public void finish() throws Exception {
		netServer.stop();
	}
}
