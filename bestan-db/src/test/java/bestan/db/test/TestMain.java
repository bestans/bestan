package bestan.db.test;

import org.rocksdb.RocksDBException;
import org.rocksdb.Transaction;

import bestan.common.db.IDBHandle;
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
	
	public static void initPath(String path) {
		var config = LuaConfigs.loadSingleConfig("DBConfig.lua", RocksDBConfig.class);
		config.dbPath = path;
		MessageFactory.executeLoadFinishCallback();
		Glog.debug("config={}", config);
		state = StorageEnv.init(config);
	}
	
	public static void main(String[] args) {
		test1();
	}
	
    public static class InitDb implements IDBHandle{
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
    	initPath("d:/rocksdb_test");
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
