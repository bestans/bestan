package bestan.common.db;

import java.util.concurrent.locks.ReentrantLock;

import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.ReadOptions;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;
import org.rocksdb.Transaction;
import com.google.common.primitives.Ints;

public class Storage implements Comparable<Object> {
	private ReentrantLock lock = new ReentrantLock();
	
	private DBConst.EM_DB tableType;
	private ColumnFamilyHandle handle;
	private ReadOptions rOptions;
	private RocksDB db;
	
	public Storage(DBConst.EM_DB tableType, ColumnFamilyHandle handle, RocksDB db) {
		this.handle = handle;
		this.tableType = tableType;
		this.db = db;
		rOptions = new ReadOptions();
	}
	
	public DBConst.EM_DB getTableType(){
		return tableType;
	}
	public void lock() {
		lock.lock();
	}
	public void unlock() {
		lock.unlock();
	}
	public void put(Transaction txn, byte[] key, byte[] value) throws RocksDBException {
		StorageEnv.lock();
		txn.put(handle, key, value);
	}
	public byte[] get(Transaction txn, byte[] key) throws RocksDBException {
		StorageEnv.lock();
		return txn.get(handle, rOptions, key);
	}
	public void delete(Transaction txn, byte[] key) throws RocksDBException {
		StorageEnv.lock();
		txn.delete(handle, key);
	}
	public RocksIterator newIterator() {
		StorageEnv.lock();
		return db.newIterator(handle);
	}
	public void put(Transaction txn, int key, int value) throws RocksDBException {
		put(txn, Ints.toByteArray(key), Ints.toByteArray(value));
	}
	public int get(Transaction txn, int key) throws RocksDBException {
		var value = get(txn, Ints.toByteArray(key));
		return value != null ? Ints.fromByteArray(value) : 0;
	}
	public void delete(Transaction txn, int key) throws RocksDBException {
		delete(txn, Ints.toByteArray(key));
	}

	@Override
	public int compareTo(Object arg) {
		if (((Storage)arg).getTableType() == this.getTableType()) {
			return 0;
		} else if (this.getTableType().ordinal() > ((Storage)arg).getTableType().ordinal()) {
			return 1;
		} else
			return -1;
	}
}
