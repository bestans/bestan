package bestan.common.db;

import java.util.concurrent.locks.ReentrantLock;

import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.ReadOptions;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;
import org.rocksdb.Transaction;

import com.google.common.primitives.Ints;
import com.google.protobuf.Message;

import bestan.common.logic.FormatException;

public class Storage implements Comparable<Object> {
	private ReentrantLock lock = new ReentrantLock();

	private String tableName;
	private ColumnFamilyHandle handle;
	private ReadOptions rOptions;
	private RocksDB db;
	private Message messageInstance;
	
	public Storage(String tableName, Message messageInstance, ColumnFamilyHandle handle, RocksDB db) throws FormatException {
		this.handle = handle;
		this.tableName = tableName;
		this.db = db;
		this.messageInstance = messageInstance;
		rOptions = new ReadOptions();
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
	public void put(Transaction txn, int key, int value) throws RocksDBException {
		put(txn, Ints.toByteArray(key), Ints.toByteArray(value));
	}

	public void put(Transaction txn, int key, Message message) throws RocksDBException {
		put(txn, Ints.toByteArray(key), message.toByteArray());
	}
	
	public int getInt(Transaction txn, int key) throws RocksDBException {
		var value = get(txn, Ints.toByteArray(key));
		return value != null ? Ints.fromByteArray(value) : 0;
	}
	
	public Message.Builder get(Transaction txn, int key) throws Exception {
		var value = get(txn, Ints.toByteArray(key));
		if (value == null)
			return null;
		
		return messageInstance.newBuilderForType().mergeFrom(value);
	}
	
	public void delete(Transaction txn, int key) throws RocksDBException {
		delete(txn, Ints.toByteArray(key));
	}

	public void delete(Transaction txn, Message key) throws RocksDBException {
		delete(txn, key.toByteArray());
	}
	
	@Override
	public int compareTo(Object arg) {
		return getTableType().compareTo(((Storage)arg).getTableType());
	}
}
