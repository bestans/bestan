package bestan.common.db;

import org.rocksdb.RocksDBException;
import org.rocksdb.Transaction;

import bestan.common.log.Glog;

public interface IDBHandle {
	default void baseHandle() {
		Transaction txn = null;
		try {
			txn = StorageEnv.start();
			handle(txn);
			txn.commit();
		} catch (DBException e) {
			Glog.debug("DbHandle:{}:DBException:errcode={},message={},stack={},", getClass().getSimpleName(), e.getErrorCodeMessage(), e.getMessage(), e.getStackTrace());
			StorageEnv.rollback(txn);
		} catch (Exception e) {
			Glog.debug("DbHandle:{}:Exception:message={},stack={}", getClass().getSimpleName(), e.getMessage(), e.getStackTrace());
			StorageEnv.rollback(txn);
		} finally {
			StorageEnv.end();
		}
	}
	void handle(Transaction txn) throws RocksDBException;
}
