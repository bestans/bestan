package bestan.test;

import bestan.common.datastruct.ServerOption;
import bestan.common.datastruct.ServerOption.SERVER_TYPE;
import bestan.common.net.message.MessageFactory;
import bestan.common.server.LogicServerManager;
import bestan.gameserver.net.LogicServer;
import bestan.gameserver.net.MessageDispatcher;
import bestan.gameserver.net.MessageHandlerRegister;
import bestan.gameserver.net.MessageRegister;
import bestan.gameserver.net.NetServerManager;
import bestan.log.GLog;

public class GameServer implements Runnable {
	private boolean finish = false;
	private static GameServer instance;
	
	public static GameServer getInstance() {
		return instance;
	}
	public static void generateInstance(GameServer server) {
		instance = server;
	}
	
	public static void main(String[] args) {
		LogicServer logicServer = LogicServer.getInstance();
		
		// LogicServer 注册进 LogicManager
		LogicServerManager.getInstance().register(logicServer);
		
		GameServer server = new GameServer();
		// 生成单例
		GameServer.generateInstance(server);
		if(!server.init()) {
			server.finish();
			return;
		}
		new Thread(server, "GameServer").start();
	}

	@Override
	public synchronized void run() {
		NetServerManager.getInstance().startServer();
		LogicServerManager.getInstance().getLogicServer().start();
		GLog.log.debug("logicserver start");

		// TODO Auto-generated method stub
		while (!finish) {
			try {
				wait(5000);
			} catch (InterruptedException e) {
				setFinish();
				GLog.log.error("gameserver run failed,reason:{}", e.getMessage());
			}
		}
	}

	public void setFinish() {
		this.finish = true;
	}
	
	public boolean init() {
		MessageDispatcher.getDispatcher().initMap(new MessageHandlerRegister());
		MessageFactory.getFactory().init(new MessageRegister());
		
		LogicServer logicServer = LogicServer.getInstance();
		logicServer.init(MessageDispatcher.getDispatcher());
		
		// LogicServer 注册进 LogicManager
		LogicServerManager.getInstance().register(logicServer);
		
		ServerOption option = new ServerOption();
		option.serverType = SERVER_TYPE.HTTP_SERVER;
		NetServerManager.getInstance().init(option);
		return true;
	}
	
	protected void finish() {}
}
