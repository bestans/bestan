package bestan.gameserver.net;

import bestan.common.datastruct.ServerOption;
import bestan.common.net.server.AbstractNetServerManager;

public class NetServerManager extends AbstractNetServerManager {
	private static NetServerManager instance;
	
	@Override
	public boolean init(ServerOption option) {
		if (!super.init(option))
			return false;
		
		
		return true;
	}

	public static NetServerManager getInstance() {
		if(null == instance) {
			instance = new NetServerManager();			
		}
		
		return instance;
	}
	
	@Override
	public void tick() {
//		int nowSeconds = DateValidate.getCurrentTimeSecond();
//		if(runTimeSec >= nowSeconds) {
//			return;
//		}
//		
//		runTimeSec = nowSeconds;
//		if(null != this.worldClient && this.worldClient.isTickValid()) {
//			this.worldClient.connect();
//		}
//		if(null != this.battleClient && this.battleClient.isTickValid()) {
//			this.battleClient.connect();
//		}
//		if(null != this.agentClient && this.agentClient.isTickValid()) {
//			this.agentClient.connect();
//		}
	}
}
