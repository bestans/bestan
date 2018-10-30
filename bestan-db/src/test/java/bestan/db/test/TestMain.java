package bestan.db.test;

import org.rocksdb.RocksDBException;
import org.rocksdb.Transaction;

import bestan.common.db.IDBHandle;
import bestan.common.db.MergeTable;
import bestan.common.db.MergeTable.MergeDBHandler;
import bestan.common.db.RocksDBConfig;
import bestan.common.db.RocksDBState;
import bestan.common.db.StorageEnv;
import bestan.common.log.Glog;
import bestan.common.lua.LuaConfigs;
import bestan.common.message.MessageFactory;
import cn.hutool.core.date.DateUtil;

/**
 * @author yeyouhuan
 *
 */
public class TestMain {
	private static RocksDBState state;

	public static void initPath() {
		initPath("DBConfig.lua");
	}
	public static void initPath(String cfgPath) {
		var config = LuaConfigs.loadSingleConfig(cfgPath, RocksDBConfig.class);
		MessageFactory.executeLoadFinishCallback();
		Glog.debug("config={}", config);
		state = StorageEnv.initDB(config);
	}

	public static void main(String[] args) {
		test4();
	}

	public static void test5() {
		var db = StorageEnv.initDB("DBConfig.lua");
		var storage = db.getStorage("player");
		var txn = StorageEnv.beginTransaction(db);
	}
	
	public static void test4() {
		var handler = new MergeDBHandler() {
			@Override
			public void merge(RocksDBState dstDB, RocksDBState srcDB) throws RocksDBException {
				MergeTable.mergeByCombine(dstDB, srcDB, "player");
			}
		};
		MergeTable.merge("DBConfig.lua", "DBConfig2.lua", handler);
	}
	
	public static void test3() {
		initPath();
		var storage = state.getStorage("player");

		try {
			var txn = StorageEnv.beginTransaction();
			for (int i = 1000; i > 0; i-=10) {
				storage.put(txn, i, i * 3);
			}
			txn.commit();
		} catch (RocksDBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		var it = storage.newIterator();
		for (it.seek(911); it.isValid(); it.next()) {
			Glog.debug("value:key={},value={}", it.key(), it.value());
		}
	}

	public static void test2() {
		initPath();
		var storage = state.getStorage("player");
		try {
			var txn = StorageEnv.beginTransaction();
			var value = storage.get(txn, 100);
			Glog.debug("get value={}", value);
			storage.put(txn, 100, 104);
			Glog.debug("set value={}", value);
			value = storage.get(txn, 100);
			Glog.debug("get value={}", value);
			// txn.commit();
			txn.rollback();
		} catch (RocksDBException e) {
			// TODO Auto-generated catch block
			// System.out.println(e.ge);
			// e.printStackTrace();
		} finally {
			// TODO: handle finally clause
		}
	}

	public static class InitDb implements IDBHandle {
		private int start = 0;

		public InitDb(int key) {
			this.start = key;
		}

		@Override
		public void handle(Transaction txn) throws RocksDBException {
			var player = StorageEnv.getStorage("player");
			int key = start;
			for (int i = 0; i < 20; ++i, ++key) {
				Glog.debug("key={},value={}", key, player.getInt(txn, key));
				player.put(txn, key, key + 1000);
			}
		}
	}

	public static void test1() {
		initPath();
		var handle = new IDBHandle() {
			@Override
			public void handle(Transaction txn) throws RocksDBException {
				var storage = state.getStorage("player");
				var itr = storage.newIterator();
				itr.seekToFirst();
				while (itr.isValid()) {
					Glog.debug("old_value:key={},value={}", itr.key(), itr.value());
					itr.next();
				}
			}
		};
		handle.baseHandle();

		// setup checkpoint
		new InitDb(100).baseHandle();

		state.checkpoint(DateUtil.format(DateUtil.date(), "yyyyMMddHHmmss"));
	}
}
