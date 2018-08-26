package bestan.common.db;

import java.util.Map;

import com.google.common.collect.Maps;

import bestan.common.protobuf.Proto.DBTableDefine;

/**
 * DB操作工具类
 * @author yeyouhuan
 *
 */
public class DBUtil {
	/**
	 * 
	 * @param oldDefine 旧数据库中的define
	 * @param newDefine 新数据库中的define
	 * @return
	 */
	public static boolean checkStartTableDefine(DBTableDefine.Builder oldDefine, DBTableDefine.Builder newDefine) {
		Map<String, String> oldMap = Maps.newHashMap();
		for (var it : oldDefine.getTablesList()) {
			//oldMap.put(it.getTableName().toString(), it.getTableMessageName().toString());
		}
		return true;
	}
}
