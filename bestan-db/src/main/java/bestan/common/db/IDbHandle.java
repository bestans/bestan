package bestan.common.db;

import org.rocksdb.RocksDBException;
import org.rocksdb.Transaction;

import bestan.common.log.Glog;

public interface IDbHandle {
	default void baseHandle() {
		Transaction txn = null;
		try {
			txn = StorageEnv.start();
			handle(txn);
			txn.commit();
		} catch (DBException e) {
			Glog.debug("DbHandle:{}:DBException:errcode={},message={}", getClass().getSimpleName(), e.getErrorCodeMessage(), e.getMessage());
			StorageEnv.rollback(txn);
		} catch (Exception e) {
			Glog.debug("DbHandle:{}:Exception:message={}", getClass().getSimpleName(), e.getMessage());
			StorageEnv.rollback(txn);
		} finally {
			StorageEnv.end();
		}
	}
	void handle(Transaction txn) throws RocksDBException;
}
