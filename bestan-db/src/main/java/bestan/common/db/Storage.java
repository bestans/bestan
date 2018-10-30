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
import bestan.common.net.operation.TableDataType.DataProcess;

public class Storage implements Comparable<Object> {
	private ReentrantLock lock = new ReentrantLock();

	private String tableName;
	private ColumnFamilyHandle handle;
	private ReadOptions rOptions;
	private RocksDB db;

	private DataProcess keyProcess;
	private DataProcess valueProcess;
	
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
	
	void rawPut(byte[] key, byte[] value) throws RocksDBException {
		db.put(key, value);
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
	
	/**
	 * @return 获取table原始的iterator
	 */
	RocksIterator rawNewIerator() {
		return db.newIterator(handle);
	}
	
	public IteratorDecorator newIterator() {
		StorageEnv.lock();
		var itr = db.newIterator(handle);
		if (null == itr) {
			throw new FormatException("newIterator is null,storage=%s", getTableType());
		}
		return new IteratorDecorator(itr, this);
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
	
	public Object getKeyObject(byte[] bytes) {
		return keyProcess.convert(bytes);
	}
	
	public Object getValueObject(byte[] bytes) {
		return valueProcess.convert(bytes);
	}
	
	public byte[] getKeyBytes(Object key) {
		return keyProcess.getBytes(key);
	}
	
	public byte[] getValueBytes(Object value) {
		return valueProcess.getBytes(value);
	}
 	
	public DataProcess getValueProcess() {
		return valueProcess;
	}
	
	@Override
	public int compareTo(Object arg) {
		return getTableType().compareTo(((Storage)arg).getTableType());
	}
	
	public static class IteratorDecorator{
		private RocksIterator iterator;
		private Storage storage;
		
		private IteratorDecorator(RocksIterator iterator, Storage storage) {
			this.iterator = iterator;
			this.storage = storage;
		}
		
		/**
		 * Position at the first entry in the source. The iterator is Valid() after this call if the source is not empty.
		 */
		public void seekToFirst() {
			iterator.seekToFirst();
		}
		
		/**
		 * Position at the last entry in the source. The iterator is valid after this call if the source is not empty.
		 */
		public void seekToLast() {
			iterator.seekToLast();
		}
		
		/**
		 * Position at the first entry in the source whose key is that or past target.<p>
		 * The iterator is valid after this call if the source contains a key that comes at or past target.
		 * @param key object describing a key or a key prefix to seek for.
		 */
		public void seek(Object key) {
			iterator.seek(storage.getKeyBytes(key));
		}
		
		/**
		 * Position at the first entry in the source whose key is that or before target.<p>
		 * The iterator is valid after this call if the source contains a key that comes at or before target.
		 * @param key object describing a key or a key prefix to seek for. 
		 */
		public void seekForPrev(Object key) {
			iterator.seekForPrev(storage.getKeyBytes(key));
		}
		
		/**
		 * An iterator is either positioned at an entry, or not valid. This method returns true if the iterator is valid.
		 * @return true if iterator is valid.
		 */
		public boolean isValid() {
			return iterator.isValid();
		}
		
		/**
		 * Moves to the next entry in the source. After this call, Valid() is true if the iterator was not positioned at the last entry in the source.
		 */
		public void next() {
			iterator.next();
		}
		
		/**
		 * Moves to the previous entry in the source. After this call, Valid() is true if the iterator was not positioned at the first entry in source.
		 */
		public void prev() {
			iterator.prev();
		}
		
		/**
		 * Frees the underlying C++ object <p>
		 * It is strong recommended that the developer calls this after they have finished using the object.
		 */
		public void close() {
			iterator.close();
		}
		
		/**
		 * Return the key for the current entry. The underlying storage for the returned slice is valid only until the next modification of the iterator.
		 * @return
		 */
		public Object key() {
			return storage.getKeyObject(iterator.key());
		}
		
		/**
		 * Return the value for the current entry. The underlying storage for the returned slice is valid only until the next modification of the iterator.
		 * @return
		 */
		public Object value() {
			return storage.getValueObject(iterator.value());
		}
	}
}
