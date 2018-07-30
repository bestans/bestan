package bestan.test;

import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.ReadOptions;
import org.rocksdb.RocksDBException;
import org.rocksdb.Transaction;
import org.rocksdb.WriteOptions;
import org.slf4j.Logger;

import bestan.common.db.DBException;
import bestan.common.db.RocksDbState;
import bestan.common.db.StorageEnv;
import bestan.common.db.DBConst.EM_DB;
import bestan.log.Glog;

public class Thread1 extends Thread {
	public Thread1()
	{
		super("thread1");
	}
	public void test1() {
		Transaction txn = null;
		try {
			txn = StorageEnv.start();
			var tb1 = StorageEnv.getStorage(EM_DB.PLAYER);
			var tb2 = StorageEnv.getStorage(EM_DB.PLAYER2);
			for (int i = 0; i < 1; ++i) {
				tb1.put(txn, i, tb1.get(txn, i) + 10);
				tb2.put(txn, i, tb2.get(txn, i) + 100);
			}
			txn.commit();
		} catch (DBException e) {
			Glog.debug("DBException:errcode={},message={}", e.getErrorCodeMessage(), e.getMessage());
			StorageEnv.rollback(txn);
		} catch (Exception e) {
			Glog.debug("Exception:message={}", e.getMessage());
			StorageEnv.rollback(txn);
		} finally {
			StorageEnv.end();
		}
	}
	public void run() {
		for (int i = 0; i < 100; ++i) {
			test1();
			
			try {
				sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
