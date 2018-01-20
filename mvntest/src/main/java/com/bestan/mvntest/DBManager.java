package com.bestan.mvntest;

import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

public class DBManager {
	private RocksDB db;
	private static DBManager instance = new DBManager();
	
	public boolean init()
	{
		try {
			RocksDB.loadLibrary();
			Options options = new Options().setCreateIfMissing(true);
		
			db = RocksDB.open(options, "db");
		} catch (RocksDBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
	
	public static DBManager GetInstance()
	{
		return instance;
	}
	public void put(byte[] key, byte[] value)
	{
		try {
			db.put(key, value);
		} catch (RocksDBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public byte[] get(byte[] key)
	{
		try {
			return db.get(key);
		} catch (RocksDBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public void delete(byte[] key)
	{
		try {
			db.delete(key);
		} catch (RocksDBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
