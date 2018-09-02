package bestan.common.db;

import java.util.concurrent.locks.ReentrantLock;

import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.ReadOptions;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;
import org.rocksdb.Transaction;

import bestan.common.db.RocksDBConfig.TableStruct;
import bestan.common.logic.FormatException;

public class Storage implements Comparable<Object> {
	private ReentrantLock lock = new ReentrantLock();

	private String tableName;
	private ColumnFamilyHandle handle;
	private ReadOptions rOptions;
	private RocksDB db;

	private TableDataProcess keyProcess;
	private TableDataProcess valueProcess;
	
	public Storage(String tableName, TableStruct tableStruct, ColumnFamilyHandle handle, RocksDB db) throws FormatException {
		this.handle = handle;
		this.tableName = tableName;
		this.db = db;
		rOptions = new ReadOptions();
		
		this.keyProcess = tableStruct.keyProcess;
		this.valueProcess = tableStruct.valueProcess;
	}
	
	public String getTableType(){
		return tableName;
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
	
	public void put(Transaction txn, Object key, Object value) throws RocksDBException {
		put(txn, keyProcess.getBytes(key), valueProcess.getBytes(value));
	}
	
	public void delete(Transaction txn, Object key) throws RocksDBException {
		delete(txn, keyProcess.getBytes(key));
	}
	
	public Object get(Transaction txn, Object key) throws RocksDBException {
		var value = get(txn, keyProcess.getBytes(key));
		if (value == null) {
			return null;
		}
		return valueProcess.convert(value);
	}
	
	public Integer getInt(Transaction txn, Object key) throws RocksDBException {
		return (Integer)get(txn, key);
	}
	
	public Object getKeyObject(byte[] bytes) throws RocksDBException {
		return keyProcess.convert(bytes);
	}
	
	public Object getValueObject(byte[] bytes) throws RocksDBException {
		return valueProcess.convert(bytes);
	}
	
	@Override
	public int compareTo(Object arg) {
		return getTableType().compareTo(((Storage)arg).getTableType());
	}
}
