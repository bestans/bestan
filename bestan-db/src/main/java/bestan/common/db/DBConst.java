package bestan.common.db;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class DBConst {
	public enum EM_DB
	{
		DEFAULT,
		PLAYER,
		COMMON,
		PLAYER2,
		PLAYER3,
	}

	public static final String DEFAULT_COLUMN_FAMILY = "default";
	public static List<String> dbDescs;
	public static Map<String, EM_DB> dbDescMap;
	
	public static void init() {
		dbDescs = Lists.newArrayList();
		dbDescMap = Maps.newHashMap();
		for (EM_DB dbType : EM_DB.values()) {
			dbDescs.add(dbType.name().toLowerCase());
			dbDescMap.put(dbType.name().toLowerCase(), dbType);
		}
	}
}
