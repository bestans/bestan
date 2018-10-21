package bestan.common.db;

import java.util.Set;
import java.util.TreeSet;

import org.rocksdb.RocksDBException;
import org.rocksdb.Transaction;
import org.rocksdb.WriteOptions;

import bestan.common.db.DBException.ErrorCode;
import bestan.common.log.Glog;

public class StorageEnv {
	private static RocksDBState dbState;
	private static WriteOptions wOp;
	
	public static RocksDBState initDB(RocksDBConfig config) {
        var state = new RocksDBState(config);
        state.initEnv();
        return state;
	}
	public static void init(RocksDBConfig config) {
		dbState = initDB(config);
		wOp = new WriteOptions();
	}
	
	public static void close() {
		try {
			dbState.txnDb.compactRange();
		} catch (RocksDBException e) {
			Glog.error("db close error:msg={}", e.getMessage());
		}
		dbState.cleanup();
	}
	
	public static Storage getStorage(String tableName) {
		if (ThreadContext.getInstance().isLocked())
			return null;
		Storage storage = dbState.getStorage(tableName);
		ThreadContext.getInstance().addStorage(storage);
		return storage;
	}
	public static Transaction start() {
		if (ThreadContext.getInstance().isLocked())
			throw new DBException(ErrorCode.DB_RELOCK, "lock when start");
		
		return dbState.txnDb.beginTransaction(wOp);
	}
	
	public static void end() {
		ThreadContext.getInstance().unlock();
	}
	
	public static void rollback(Transaction txn) {
		if (txn == null)
			return;
		
		try {
			txn.rollback();
		} catch (RocksDBException e) {
			Glog.debug("rollback RocksDBException:error={}", e.getMessage());
		}
	}
	public static void lock() {
		ThreadContext.getInstance().lock();
	}
	
	private static class ThreadContext {
		private final static ThreadLocal<ThreadContext> tl = new ThreadLocal<ThreadContext>();
		
		private boolean locked = false;
		private Set<Storage> storageList = new TreeSet<>();
		
		private ThreadContext() {
		}
		static ThreadContext getInstance() {
			var tc = tl.get();
			if (tc == null) {
				tl.set(tc = new ThreadContext());
			}
			return tc;
		}
		public boolean isLocked() {
			return locked;
		}
		public void lock() {
			if (locked) return;
			
			storageList.forEach(storage->{storage.lock();});
			locked = true;
		}
		public void unlock() {
			if (!locked) return;
			
			storageList.forEach(storage->{ storage.unlock(); });
			storageList.clear();
			locked = false;
		}
		public void addStorage(Storage storage) {
			storageList.add(storage);
		}
	}
}
