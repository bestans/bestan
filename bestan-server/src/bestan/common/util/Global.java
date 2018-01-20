package bestan.common.util;

import bestan.common.timer.DateValidate;

public class Global {
	public static final int INVALID_VALUE = -1;
	public static final float INVALID_FLOAT_VALUE = -1f;
	public static final int MAX_PLAYER_LEVEL = 90;
	public static final int GUILD_WORSHIP_LEVEL = 5;
	public static final int GUILD_WORSHIPED_TIMES = 10000;
	
	public static final int INT_MAX_VALUE = 2147483647;
	
	public static final String DBCOMMAND_THREAD_NAME_PREF = "DBCommandQueue";
	public static final String CACHE_THREAD_NAME_PREF = "CacheThread";
	
	public static final String LOGIN_THREAD_NAME_PREF = "LOGIN";
	
	// Connection Type
	public static final int CONNECTTYPE_INVALID = INVALID_VALUE;
	public static final int CONNECTTYPE_GS = 0;
	public static final int CONNECTTYPE_IWEB = 1;
	public static final int CONNECTTYPE_NUMBER = 2;
	
	// Object Type
	public static final int OBJECT_TYPE_INVALID = INVALID_VALUE;
	public static final int OBJECT_TYPE_PLAYER = 0;
	public static final int OBJECT_TYPE_HERO = 1;
	public static final int OBJECT_TYPE_TOWER = 2;
	public static final int OBJECT_TYPE_ITEM = 3;
	public static final int OBJECT_TYPE_CONNECTION = 4;
	public static final int OBJECT_TYPE_MOVIE = 5;
	public static final int OBJECT_TYPE_NUMBER = 6;
	
	public static final int OBJECT_SUBTYPE_INVALID = INVALID_VALUE;
	public static final int OBJECT_SUBTYPE_PLAYER = 1;
	public static final int OBJECT_SUBTYPE_ROBOT = 2;
	public static final int OBJECT_SUBTYPE_GM = 3;
	public static final int OBJECT_SUBTYPE_TEMP = 4;
	
	
	public static final String SIMULATE_LIB_NAME = "SimulateBattle";
	
	public static final String INVALID_STRING = "-1";
	
	/**
	 * 英雄最大星数
	 */
	public static final int MAX_HERO_STAR_NUM = 6;
	public static final String SERVER_CONFIG = "server.ini";
	public static final String GAMESERVER_CONFIG = "gameserver.ini";
	public static final String BANFUNC_CONFIG = "ban.ini";
	public static final String REDIS_CONFIG = "redis.ini";
	public static final String STARTSERVER_CONFIG = "openservertimes";
	public static final String BANWORD_CONFIG = "chatshield.txt";
	public static final String ADVER_CONFIG = "chatadvershield.txt";
	public static final String ITEM_GUID_NUM = "itemguid";
	
	public static String CONFIG_PATH = "config";
	public static String DATA_PATH = "data";
	public static String TABLE_PATH = "Table";
	public static String DBTOOL_DATA_PATH = "dbtooldata";
	public static String TABLE_SERVER_PATH = TABLE_PATH + java.io.File.separatorChar + "Server" + java.io.File.separatorChar;
	public static String TABLE_PUBLIC_PATH =  TABLE_PATH + java.io.File.separatorChar + "Public" + java.io.File.separatorChar;
	public static String TABLE_BATTLEPLUGIN_PATH =  TABLE_PUBLIC_PATH + "BattlePlugin" + java.io.File.separatorChar;
	
	public static final int MAX_RAID_STAR_NUM = 3;
	
	public static final int MAX_BATTLE_HERO_NUM = 3;
	public static final int MAX_BATTLE_BULLET_NUM = 4;
	
	/**
	 * 前五关的场景ID
	 */
	public static final int RAID_SCENEID_0 = 11001;
	public static final int RAID_SCENEID_1 = 11002;
	public static final int RAID_SCENEID_2 = 11003;
	public static final int RAID_SCENEID_3 = 11004;
	public static final int RAID_SCENEID_4 = 11005;
	
	public static final int TRUE = 1;
	public static final int FALSE = 0;
	
	public static final int MAX_HEADICON_INDEX = 4;
	public static final int DEFAULT_HEADICON_ID = 10001;
	
	public static final String RELOAD_GS_JAR = "gs.jar";
	public static final String RELOAD_WS_JAR = "worldserver.jar";
	public static final String RELOAD_BS_JAR = "battleserver.jar";
	public static final String RELOAD_AS_JAR = "agent.jar";
	
	public static final String GS_JAR_NAME = "gs.jar";
	public static final String JAR_FILE_PATH = "libs";
	public static final String VERSION_KEY = "Specification-Version";
	public static final String WS_JAR_NAME = "worldserver.jar";
	public static final String BS_JAR_NAME = "battleserver.jar";
	public static final String AS_JAR_NAME = "agent.jar";
	
	// 技能学习索引
	public static final int[] SKILL_LEARN_INDEX_LIST = { 9, 1, 2, 3 };

	// 守矿时长
	public static final int ONE_HOURS_MINE_TIME = (int)(1 * DateValidate.hourSeconds);
	public static final int THREE_HOURS_MINE_TIME = (int)(3 * DateValidate.hourSeconds);
	public static final int TEN_HOURS_MINE_TIME = (int)(10 * DateValidate.hourSeconds);

	// 抢矿持续时长
	public static final int MINE_PLUNDER_MAX_DURATION_TIME = 600;
	public static final int MINE_PLUNDER_HISTORY_COUNT = 600;
	// 矿有效期
	public static final int MAX_MINE_DURATION_TIME = (int)(4 * DateValidate.daySeconds);
	
	// 最大守矿队伍
	public static final int MAX_MINE_TEAM_COUNT = 3;
	public static final int BATTLE_INFO_HERO_COUNT = 3;
	public static final int BATTLE_INFO_BULLET_START_INDEX = BATTLE_INFO_HERO_COUNT;
	
	//公会副本伤害奖励邮件ID
	public static final int GUILD_HURT_MAIL_ID = 8999;
	//玩家排队装备
	public static final int GUILD_LIST_MAIL_ID = 9000;
	//完成副本额外奖励
	public static final int GUILD_RAID_OTHER_ID = 9001;
	
	// 登陆MD5校验key
	public static final String LOGIN_VERIFY_PKEY = "qfyxys2016";
	
	// 游戏区服 单位 
	// 假设 1001 - 1999 越狱   2001 - 2999 ios 。。。
	public static final int SERVER_ID_ZONE_UNIT = 1000;
}
