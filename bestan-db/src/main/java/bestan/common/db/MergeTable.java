package bestan.common.db;

import org.rocksdb.RocksDBException;

import bestan.common.log.Glog;
import bestan.common.logic.FormatException;
import bestan.common.lua.LuaConfigs;

/**
 * @author yeyouhuan
 *
 */
public class MergeTable {
	/**
	 * 将来源db数据全部合并到目标db
	 * @param dstPath 目标db配置文件路径
	 * @param srcPath 来源db配置文件路径
	 * @param handler merge操作对象
	 */
	public static void merge(String dstPath, String srcPath, MergeDBHandler handler) {
		var dstConfig = LuaConfigs.loadSingleConfig(dstPath, RocksDBConfig.class);
		if (null == dstConfig) {
			throw new FormatException("can not find dst config! path=%s", dstConfig);
		}
		var srcConfig = LuaConfigs.loadSingleConfig(srcPath, RocksDBConfig.class);
		if (null == srcConfig) {
			throw new FormatException("can not find src config! path=%s", srcConfig);
		}
		merge(dstConfig, srcConfig, handler);
	}

	/**
	 * @param dstConfig 目标db配置文件
	 * @param srcConfig 来源db配置文件
	 * @param handler merge操作对象
	 */
	public static void merge(RocksDBConfig dstConfig, RocksDBConfig srcConfig, MergeDBHandler handler) {
		var dstDB = StorageEnv.initDB(dstConfig);
		var srcDB = StorageEnv.initDB(srcConfig);
		try {
			handler.merge(dstDB, srcDB);
			dstDB.compactRange();
		} catch (RocksDBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		dstDB.cleanup();
		srcDB.cleanup();
	}
	
	//直接合并
	public static void mergeByCombine(RocksDBState dstDB, RocksDBState srcDB, String tableName) throws RocksDBException {
		var dstTable = dstDB.getStorage(tableName);
		var srcTable = srcDB.getStorage(tableName);
		if (null == dstTable || null == srcTable)
			throw new FormatException("cannot find table in src or dst db");

		int count = 0;
		var srcIt = srcTable.rawNewIerator();
		srcIt.seekToFirst();
		while (srcIt.isValid()) {
			dstTable.rawPut(srcIt.key(), srcIt.value());
			srcIt.next();
			++count;
		}
		Glog.debug("merge table({}) success.count={}", tableName, count);
		dstDB.compactRange();
	}
	
	public interface MergeDBHandler {
		/**
		 * @param dst 目标db对象
		 * @param src 来源db对象
		 */
		public void merge(RocksDBState dstDB, RocksDBState srcDB) throws RocksDBException;
	}
}
