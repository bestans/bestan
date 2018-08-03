package bestan.common.db;

import org.rocksdb.RocksDBException;

import bestan.common.db.DBConst.EM_DB;
import bestan.common.log.Glog;

/**
 * @author Administrator
 *
 */
public class MergeTable {
	private RocksDbState dstDB;
	private RocksDbState srcDB;
	
	public MergeTable(RocksDbState dstDB, RocksDbState srcDB) {
		this.dstDB = dstDB;
		this.srcDB = srcDB;
	}
	
	public static void merge(String dstPath, String srcPath) {
		var dstState = StorageEnv.initDB(dstPath);
		var srcState = StorageEnv.initDB(srcPath);
		var op = new MergeTable(dstState, srcState);
		op.mergeAllDB();
	}
	
	public void mergeAllDB() {
		try {
			mergeByCombine(EM_DB.PLAYER);
			
			dstDB.txnDb.compactRange();
		} catch (RocksDBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		dstDB.cleanup();
		srcDB.cleanup();
	}
	
	//直接合并
	public void mergeByCombine(EM_DB tableType) throws RocksDBException {
		var dstHandle = dstDB.GetHandle(tableType);
		var srcHandle = srcDB.GetHandle(tableType);
		if (dstHandle == null || srcHandle == null)
			throw new RuntimeException("not found table");
		
		var src_it = srcDB.txnDb.newIterator(srcHandle);
		src_it.seekToFirst();
		int count = 0;
		while (src_it.isValid()) {
			dstDB.txnDb.put(dstHandle, src_it.key(), src_it.value());
			src_it.next();
			++count;
		}
		Glog.debug("merge table({}) count num = {}", tableType.name(), count);
		dstDB.txnDb.compactRange();
	}
}
