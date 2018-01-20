package bestan.common.server;

import java.io.IOException;
import java.math.BigInteger;
import java.util.jar.JarFile;

import org.slf4j.Logger;

import bestan.common.cache.CacheCommandQueue;
import bestan.common.config.RedisConfig;
import bestan.common.config.ServerConfig;
import bestan.common.crypto.Hash;
import bestan.common.crypto.RSAKey;
import bestan.common.db.DatabaseBoostrap;
import bestan.common.db.DatabaseConnectionException;
import bestan.common.db.IDatabaseInitialSchema;
import bestan.common.db.command.DBCommandQueue;
import bestan.common.exception.BestanUncaughtExceptionHandler;
import bestan.common.exception.TableLoadException;
import bestan.common.guid.GuidGenerateSingleton;
import bestan.common.hotdeploy.HotDeployReload;
import bestan.common.log.LogManager;
import bestan.common.net.AbstractNetworkManager;
import bestan.common.net.message.IMessageRegister;
import bestan.common.net.message.MessageFactory;
import bestan.common.tab.CommonTableDB;
import bestan.common.timer.TimerManager;
import bestan.common.util.Global;
import bestan.common.util.Utility;
import bestan.log.GLog;

public abstract class MainServer implements Runnable {
	public enum E_SERVER_TYPE {
		GameServer("GameServer"),
		WorldServer("WorldServer"),
		BattleServer("BattleServer"),
		HttpAgent("HttpAgent");
		
		private String name; 
		public String getName() {
			return this.name;
		}

		private E_SERVER_TYPE(String name) {
			this.name = name;
		}
	}
	
	private final static Logger logger = GLog.log;
	
	/**
	 * Server Version
	 */
	public static String VERSION = "404 not found";
	
	/**
	 * server type
	 */
	public final E_SERVER_TYPE eServerType;
	
	public static E_SERVER_TYPE ServerType;
	
	/**
	 * 逻辑线程实例
	 * TODO: 如果是多线程，应存储逻辑线程管理器
	 */
	protected final AbstractLogicServer logicServer;
	
	/**
	 * 配置表实例
	 */
	protected final CommonTableDB tableDB;
	
	/**
	 * 消息注册器
	 */
	protected final IMessageRegister messageRegister;
	
	/**
	 * 网络管理器
	 */
	protected final AbstractNetworkManager networkManager;
	
	/**
	 * 数据库启动执行脚本
	 */
	protected final IDatabaseInitialSchema databaseInitialSchema;
	
	/** 
	 * 守护线程启动类的类型
	 */
	protected final Class<? extends MainServer> jarClazz;
	
	/**
	 * 启动服务的jar包名称 
	 */ 
	protected final String jarName;
	
	/**
	 * 热加载用的jar name
	 */
	protected final String hotDeployJarName;
	
	/**
	 * 是否开启数据库服务
	 */
	protected final boolean onDBService;
	
	/**
	 * 是否开启缓存服务
	 */
	protected final boolean onCacheService;
	
	/**
	 * RSAKey
	 */
	protected RSAKey rsaKey;
	
	/**
	 * Constructor
	 * 
	 * @param eServerType
	 * @param jarClazz
	 * @param jarName
	 * @param logicServer
	 * @param tableDB
	 * @param messageRegister
	 * @param networkManager
	 * @param onCacheService
	 * @param onDBService
	 * @param databaseInitialSchema
	 */
	protected MainServer(
			final E_SERVER_TYPE eServerType,
			final Class<? extends MainServer> jarClazz,
			final String jarName,
			final String hotDeployJarName,
			final AbstractLogicServer logicServer, 
			final CommonTableDB tableDB, 
			final IMessageRegister messageRegister, 
			final AbstractNetworkManager networkManager,
			final boolean onCacheService,
			final boolean onDBService,
			final IDatabaseInitialSchema databaseInitialSchema) {
		MainServer.ServerType = eServerType;
		this.eServerType = eServerType;
		this.jarClazz = jarClazz;
		this.jarName  = jarName;
		this.hotDeployJarName = hotDeployJarName;
		this.logicServer = logicServer;
		this.tableDB = tableDB;
		this.messageRegister = messageRegister;
		this.networkManager = networkManager;
		this.onCacheService = onCacheService;
		this.onDBService = onDBService;
		this.databaseInitialSchema = databaseInitialSchema;
	}
	
	public AbstractLogicServer getLogicServer() {
		return this.logicServer;
	}
	
	public CommonTableDB getTableDBInstance() {
		return this.tableDB;
	}
	
	public IMessageRegister getMessageRegister() {
		return this.messageRegister;
	}
	
	public IDatabaseInitialSchema getDatabaseInitialSchema() {
		return this.databaseInitialSchema;
	}
	
	/**
	 * read version from manifest
	 */
	private void readVersionFromManifest() {
		try {
	        JarFile jarFile = Utility.getJarFile(jarClazz, Global.JAR_FILE_PATH, jarName);
			VERSION = jarFile.getManifest().getMainAttributes().getValue(Global.VERSION_KEY);			
		} catch (Exception e) {
		}
	}
	
	private void printServerInfo() {
		System.out.println(eServerType.name + " - bestan`s multiplayer online framework for game development -");
		System.out.println("Running on version " + VERSION);
		System.out.println("(C) 2014-MORE");
		System.out.println("INC: WangChunYuan 8-405 Beijing China.");
		System.out.println();
	}
	
	/**
	 * load Log4J
	 */
	private void intLogManager() {
		try {
		  LogManager.init();
		} catch(Exception e) {
			System.out.println("ERROR: " + eServerType.name + " can't initialize logging.");
			System.out.println("Verify you have created config/logback.xml");
			System.exit(1);
		}
	}
	
	/**
	 * load Server.ini
	 */
	private void initConfig() {
		if(logger.isDebugEnabled()) {
			logger.debug("Init ServerConfig Begin...");
		}
		boolean bRet = ServerConfig.getInstance().load();
		if(false == bRet) {
			logger.error("Init ServerConfig Failed!");
			System.exit(1);
		}
		if(logger.isDebugEnabled()) {
			logger.debug("Init ServerConfig End");
		}
		
		if(logger.isDebugEnabled()) {
			logger.debug("Init RedisConfig Begin...");
		}
		bRet = RedisConfig.getInstance().load();
		if(false == bRet) {
			logger.error("Init RedisConfig Failed!");
			System.exit(1);
		}
		if(logger.isDebugEnabled()) {
			logger.debug("Init RedisConfig End");
		}
	}
	
	/**
	 * load GuidGenerate
	 */
	private void initGuidGenerate() {
		if(logger.isDebugEnabled()) {
			logger.debug("Init GuidGenerate Begin...");
		}
		
		boolean bRet = false;
		try {
			bRet = GuidGenerateSingleton.getSingleton().init();
		} catch (IOException e) {
			logger.error("Init GuidGenerate Failed!");
			System.exit(1);
		}
		if(false == bRet) {
			logger.error("Init GuidGenerate Failed!");
			System.exit(1);
		}
		
		if(logger.isDebugEnabled()) {
			logger.debug("Init GuidGenerate End...");
		}
	}
	
	/**
	 * init Message Register
	 */
	private void initMessageRegister() {
		if(logger.isDebugEnabled()) {
			logger.debug("Init MessageRegister Begin...");
		}
		
		if(!MessageFactory.getFactory().init(messageRegister)) {
			logger.error(eServerType.name + " init MessageRegister failed");
			System.exit(1);
		}
		
		if(logger.isDebugEnabled()) {
			logger.debug("Init MessageRegister Begin...");
		}
	}
	
	/**
	 * load DBFactory
	 */
	private void initDBFactory() {
		if(!onDBService) {
			return;
		}
		
		if(logger.isDebugEnabled()) {
			logger.debug("Init DB Begin...");
		}
		
		try {
			new DatabaseBoostrap().initializeDatabase(databaseInitialSchema);
		} catch (DatabaseConnectionException e) {
			logger.error(eServerType.name + " can't connect to database");
			logger.error("Verify \"server.ini\" file to make sure access to database is possible.");
			System.exit(1);
		}
		
		if(logger.isDebugEnabled()) {
			logger.debug("Init DB End");
		}
	}
	
	/**
	 * load Cache
	 */
	private void initCache() {
		if(!onCacheService) {
			return;
		}
		
		if(logger.isDebugEnabled()) {
			logger.debug("Init Cache Begin...");
		}
		CacheCommandQueue.get();
		if(logger.isDebugEnabled()) {
			logger.debug("Init Cache End");
		}
	}
	
	/**
	 * load Table Config
	 */
	private void initTable() throws TableLoadException {
		if(logger.isDebugEnabled()) {
			logger.debug("Init Table Begin...");
		}
		
		if(null == tableDB) {
			logger.error(eServerType.name + ":MainServer tableDB is nil, please implements CommonTableDB class");
			System.exit(1);
		} else {
			tableDB.load();
		}
		
		if(logger.isDebugEnabled()) {
			logger.debug("Init Table End");
		}
	}
	
	/**
	 * load Timer
	 */
	private void initTimer() {
		if(logger.isDebugEnabled()) {
			logger.debug("Init Timer Begin...");
		}
		
		boolean bRet = TimerManager.getInstance().init(this.tableDB);
		if(false == bRet) {
			System.exit(1);
		}
		
		if(logger.isDebugEnabled()) {
			logger.debug("Init Timer End.");
		}
	}
	
	/**
	 * init net work manager
	 */
	private void initNetworkManager() {
		if(logger.isDebugEnabled()) {
			logger.debug("Init Network Begin...");
		}
		
		if(!this.networkManager.init(eServerType)) {
			logger.error("MainServer::Network init failed");
			System.exit(1);
		}
		
		
		if(logger.isDebugEnabled()) {
			logger.debug("Init Network End");
		}
	}
	
	/**
	 * init Hotdeploy
	 */
	private void initHotDeploy() {
		if(logger.isDebugEnabled()) {
			logger.debug("Init HotDeploy Begin...");
		}
		
		boolean bRet = HotDeployReload.getInstance().init(this.tableDB, this.hotDeployJarName);
		if(false == bRet) {
			logger.error("MainServer::HotDeploy failed");
			System.exit(1);
		}
		
		if(logger.isDebugEnabled()) {
			logger.debug("Init HotDeploy End.");
		}
	}
	
	/**
	 * RSA Key
	 */
	private void initRsaKey() {
		try {
			if(logger.isDebugEnabled()) {
				logger.debug("Init RSAKey Begin...");
			}
			
			if (ServerConfig.getInstance().EKey == null) {
				throw new Exception("Missing RSA key pair in server.ini; run tools.GenerateKeys");
			}
			@SuppressWarnings("unused")
			RSAKey key = new RSAKey(new BigInteger(ServerConfig.getInstance().NKey),
					new BigInteger(ServerConfig.getInstance().DKey), new BigInteger(
							ServerConfig.getInstance().EKey));

			if(logger.isDebugEnabled()) {
				logger.debug("Init RSAKey End.");
			}
		} catch (Exception e) {
			logger.error("RSAKey failed", e);
			System.exit(1);
		}
	}
	
	/**
	 * register Shutdown Hook
	 */
	private void registerShutdownHook() {
		// add Runtime shutdown hook eg: ctl+c will be signal handled in this thread
		Runtime.getRuntime().addShutdownHook(new Thread() {
			private final Logger log = GLog.log;

			@Override
			public void run() {
				log.warn("User requesting shutdown");
				finish();
				log.info("Shutdown completed. See you later!");
			}
		});		
	}
	
	protected boolean init() {
		// 1. 初始化logger （必须放到第一个初始化）
		intLogManager();
		
		try {
			// 2.  读取jar包中的版本号
			readVersionFromManifest();
			// 3.  打印服务器基本信息
			printServerInfo();
			// 4.  导入Server.ini 配置文件
			initConfig();
			// 5.  初始化GUID生成器
			initGuidGenerate();
			// 6.  读取服务器所需的表格
			initTable();
			// 7.  初始化Timer
			initTimer();
			// 8.  初始化缓存系统
			initCache();
			// 9.  初始化消息注册器
			initMessageRegister();
			// 10. 初始化数据库系统并执行服务器启动时的对应的数据库执行脚本
			initDBFactory();
			// 11. 初始化网络模块
			initNetworkManager();
			// 12. hot deploy init
			initHotDeploy();
			// 13. RSA Key
			initRsaKey();
			// 14. register Shutdown Hook
			registerShutdownHook();
			return true;
		} catch (Exception e) {
			logger.error("MainServer init failed", e);
			System.exit(1);
		}
		
		return false;
	}
	
	
	protected void finish() {
		logger.warn("=============================Server will be shut down begin.");
		try {
			// 1. 关闭网络
			logger.warn("=============1. Close NetWork Begin");
			networkManager.finish();
			logger.warn("=============1. Close NetWork Success");
		} catch (Exception e) {
			logger.trace(e.getMessage());
		}
		
		try {
			// 2. 设置主线程为待关闭状态 并等待主线程退出
			logger.warn("=============2. Close LogicServerManager Begin");
			logicServer.finish();
			logicServer.join();
			logger.warn("=============3. Close LogicServerManager Success");
		} catch (Exception e) {
			logger.trace(e.getMessage());
		}
		
		try {
			// 4. 关闭offline bg thread
			logger.warn("=============3. Close CacheCommandQueue Begin");
			CacheCommandQueue.get().finish();
			logger.warn("=============3. Close CacheCommandQueue All Success");
		} catch (Exception e) {
			logger.trace(e.getMessage());
		}
		
		try {
			// 5. 关闭DB 线程  等待DB中的指令全执行完再关闭
			logger.warn("=============4. Close DBCommandQueue Begin");
			DBCommandQueue.get().finish();
			for(int i = 0; i < ServerConfig.getInstance().db_pool_size; i++) {
				DBCommandQueue.get().enqueue(null, i+1);
			}
		} catch (Exception e) {
			logger.trace(e.getMessage());
		}
	}
	
	/**
	 * 线程启动时 执行的逻辑块
	 */
	private void start() {
		try {
			if(logger.isDebugEnabled()) {
				logger.debug("NetworkManager Start Begin...");
			}
			networkManager.start();
			if(logger.isDebugEnabled()) {
				logger.debug("NetworkManager Start End.");
			}
			
			if(logger.isDebugEnabled()) {
				logger.debug("LogicServer Start Begin...");
			}
			this.logicServer.start();
			if(logger.isDebugEnabled()) {
				logger.debug("LogicServer Start End.");
			}
		} catch(Exception e) {
			logger.error("MainServer start failed.", e);
			System.exit(1);
		}
	}
	
	public void run() {
		BestanUncaughtExceptionHandler.setup(true);
		
		//=========================================================================
		// Initialize Secure random in an extra thread because it can take up
		// to 20 seconds on some computers with low entropy. The hard disk
		// access during start up will speed it up.
		new Thread() {
			@Override
			public void run() {
				Hash.random(4);
			}
		}.start();
		//=========================================================================
		
		// 启动业务功能
		start();
	}
	
	
}
