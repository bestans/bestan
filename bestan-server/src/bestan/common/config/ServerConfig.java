package bestan.common.config;

import java.io.IOException;

import org.slf4j.Logger;

import bestan.common.reload.IHotdeploy;
import bestan.common.reload.ReloadResult;
import bestan.common.util.Global;
import bestan.common.util.Utility;
import bestan.log.GLog;

public class ServerConfig implements IHotdeploy {
	private static Logger logger = GLog.log;
	
	private static ServerConfig instance;
	
	protected ServerConfig() {
	}
	
	public static ServerConfig getInstance() {
		if(null == instance) {
			instance = new ServerConfig();
		}
		
		return instance;
	}
	
	// gs
	public String netMode;
	public String gsIP;
	public int gsPort;
	public int zoneid;
	
	// 客戶端列表中的id
	private short[] loginServerIds;
	public short getLoginServerId() {
		return this.loginServerIds[0];
	}
	
	public boolean isInSameServer(short loginServerId) {
		for(int i = 0; i < this.loginServerIds.length; ++i) {
			if(loginServerId == this.loginServerIds[i]) {
				return true;
			}
		}
		
		return false;
	}
	
	// 是否合服
	public boolean isCombineServer;
	
	// world server
	public boolean isConnectWorldServer;
	public String worldServerIP;
	public int worldServerPort;
	
	// battle server
	public boolean isConnectBattleServer;
	public String battleServerIP;
	public int battleServerPort;
	
	// http agent
	public boolean isConnectHttpAgent;
	public String httpAgentIP;
	public int httpAgentPort;
	
	// db
	public String adapter;
	public String db_user;
	public String db_passwd;
	public String db_instance;
	public int db_pool_size;
	public String db_driver;
	public String db_url;
	public String db_ip;
	public int db_port;
	public int db_back_thread_size;

	public int linkType;		
	public int logicThreadNum;
	public int max_online_num;
	
	//RSAKey
	public String NKey;
	public String EKey;
	public String DKey;
	
	public String turn_length;
	
	//ITEM GUID Config Path
	public String itemguidconfigpath;
	
	public int readTimeOut;
	public int readInGameTimeOut;
	public int writeTimeOut;
	public int writeInGameTimeOut;
	public int storetimeout;
	public int removetimeout;
	public int onetick_db_limitnum;
	public int onetick_process_limitnum;
	
	// login thread
	public int login_pool_count;
	public int loginLimitCount;		
	
	// LogicThreadStatistics
	public boolean onLogicThreadStatistics;
	
	
	/**
	 * Load ServerConfig
	 * @throws IOException
	 */
	public boolean load() {
		try {
			ConfigurationParams param = new ConfigurationParams(false, Global.CONFIG_PATH, Global.SERVER_CONFIG);
			Configuration reader = new Configuration(param);
		
			//gs
			this.netMode = reader.getString("NetMode");
			this.zoneid = reader.getInt("ZoneId");
			this.gsIP   = reader.getString("GSIP");
			this.gsPort = reader.getInt("GSPort");
			String tmpString = reader.getString("LoginServerId"); 
			this.loginServerIds = Utility.string2ShortArray(tmpString, "\\|");
			this.isCombineServer = this.loginServerIds.length > 1;
			
			//WorldServer
			this.isConnectWorldServer = reader.getInt("IsConnectWorldServer") == 1;
			this.worldServerIP = reader.getString("WorldServerIP");
			this.worldServerPort = reader.getInt("WorldServerPort");
			
			//BattleServer
			this.isConnectBattleServer = reader.getInt("IsConnectBattleServer") == 1;
			this.battleServerIP = reader.getString("BattleServerIP");
			this.battleServerPort = reader.getInt("BattleServerPort");
			
			//Agent
			this.isConnectHttpAgent = reader.getInt("IsConnectHttpAgent") == 1;
			this.httpAgentIP = reader.getString("HttpAgentIP");
			this.httpAgentPort = reader.getInt("HttpAgentPort");
			
			//DB
			this.adapter = reader.getString("Adapter");
			this.db_driver = reader.getString("DBDriver");
			this.db_ip = reader.getString("DBIP");
			this.db_port = reader.getInt("DBPort");
			this.db_user = reader.getString("DBUser");
			this.db_passwd = reader.getString("DBPasswd");
			this.db_instance = reader.getString("DBInstance");
			this.db_url = String.format(reader.getString("DBUrl"), this.db_ip, this.db_port, this.db_instance);
			this.db_pool_size = reader.getInt("PoolSize");
			this.db_back_thread_size = reader.getInt("DBBackThreadSize");
			
			//ServerInfo
			this.turn_length = reader.getString("TurnLength");
			this.max_online_num = reader.getInt("MaxOnline");
			this.readTimeOut = reader.getInt("ReadTimeout");
			this.writeTimeOut = reader.getInt("WriteTimeout");
			this.readInGameTimeOut = reader.getInt("ReadGameTimeout");
			this.writeInGameTimeOut = reader.getInt("WriteGameTimeout");
			this.storetimeout = reader.getInt("StoreTimeout") * 1000;
			this.removetimeout = reader.getInt("RemoveTimeout") * 1000;
			this.onetick_db_limitnum = reader.getInt("OneTickDBLimitNum");
			this.onetick_process_limitnum = reader.getInt("OneTickProcessLimitNum");

			//RSAKey
			this.NKey = reader.getString("n");
			this.EKey = reader.getString("e"); 
			this.DKey = reader.getString("d");
			
			//Link
			this.linkType = reader.getInt("LinkType");
			
			//Thread
			this.logicThreadNum = reader.getInt("LogicThreadNum");	

			this.login_pool_count = reader.getInt("LoginPoolCount");
			this.loginLimitCount = reader.getInt("LoginLimitCount");
			
			this.onLogicThreadStatistics = reader.getInt("OnLogicThreadStatistics") == 1;
			reader.clear();
		} catch(IOException e) {
			logger.error("Config load failed...");
			e.printStackTrace();
			return false;
		}
		
		return true;
	}

	public ReloadResult reload() throws Exception {
		ReloadResult ret = new ReloadResult(true);
		ret.setSuccess(load());
		if(!ret.isSuccess()) {
			ret.appendMsg("reload ini failed");
		} else {
			ret.appendMsg("reload ini ok");
		}
		
		return ret;
	}
}
