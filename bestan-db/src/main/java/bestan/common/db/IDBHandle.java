package bestan.common.db;

import org.rocksdb.RocksDBException;
import org.rocksdb.Transaction;

import bestan.common.log.Glog;
import bestan.common.util.ExceptionUtil;

public interface IDBHandle {
	default void baseHandle() {
		Transaction txn = null;
		try {
			txn = StorageEnv.start();
			handle(txn);
			txn.commit();
		} catch (DBException e) {
			Glog.debug("DbHandle:{}:DBException:handler={},exception={}", getClass().getSimpleName(), ExceptionUtil.getLog(e));
			StorageEnv.rollback(txn);
		} catch (Exception e) {
			Glog.debug("DbHandle:{}:Exception:handler={},exception={}", getClass().getSimpleName(), ExceptionUtil.getLog(e));
			StorageEnv.rollback(txn);
		} finally {
			StorageEnv.end();
		}
	}
	void handle(Transaction txn) throws RocksDBException;
}
