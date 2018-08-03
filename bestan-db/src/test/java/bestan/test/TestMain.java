package bestan.test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.rocksdb.OptimisticTransactionDB;
import org.rocksdb.Options;
import org.rocksdb.RocksDBException;
import org.rocksdb.Transaction;

import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Shorts;

import bestan.common.db.DBConst.EM_DB;
import bestan.common.db.DBException;
import bestan.common.db.IDbHandle;
import bestan.common.db.MergeTable;
import bestan.common.db.StorageEnv;
import bestan.common.db.util.JStormUtils;
import bestan.common.log.Glog;

public class TestMain {
    private static void initLocalRocksDbDir() {
        try {
            File file = new File("d:/test");
            if (file.exists())
                FileUtils.cleanDirectory(file);
            FileUtils.forceMkdir(new File("d:/test/backup"));
        } catch (IOException e) {
            Glog.error("Failed to create dir for path=" + "d:/test/backup", e);
            throw new RuntimeException(e.getMessage());
        }
    }
    
    private static void test1() {
    	try {
			FileUtils.forceMkdir(new File("d:/test2/backup2"));
		} catch (IOException e) {
			Glog.error(e.getMessage());
		}
    }

	private enum db_test {
		value1,
		value2,
	}
    private static void test2() {
    	byte[] b1 = "test".getBytes();
    	byte[] b2 = "test2".getBytes();
    	String s = new String(b1);
    	String s2 = new String(b2);
    	System.out.println(s.equals(s2));
    	System.out.println(db_test.values().length);
    	for (db_test d : db_test.values()) {
    		System.out.println(d.name());
    	}
    	String s3 = "value1".toString();
    	db_test var = db_test.valueOf(s3);
    	System.out.println(var == null);
    }
    private static void test3() {
        final Options options = new Options()
		        .setCreateIfMissing(true);
		try {
			var txnDb1 = OptimisticTransactionDB.open(options, "d:/rocksdb_test11");
		} catch (RocksDBException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    }

    private static void test4()
    {
    	System.out.println("start");
    	TestStatic.output();
    	System.out.println("start1");
    	System.out.println(TestStatic.getInstance().value);
    }
    
    private static void test5() {
    	List<Integer> arr = Lists.newArrayList(new Integer[5]);
    	System.out.println(arr.size());
    	
    	arr.forEach(value->{ System.out.println(value); });
    }
    
    private static void test6() {
        StorageEnv.init();
        var t1 = new Thread1();
        var t2 = new Thread2();
        t1.start();
        t2.start();
        try {
			t1.join();
	        t2.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		StorageEnv.close();
        System.out.println("finish");
    }
    private static void test7() {
        StorageEnv.init();
		Transaction txn = null;
		try {
			txn = StorageEnv.start();
			var tb2 = StorageEnv.getStorage(EM_DB.PLAYER2);
			var tb1 = StorageEnv.getStorage(EM_DB.PLAYER);
			Glog.debug("key={},value1={},value2={}", 1, tb1.get(txn, 1), tb2.get(txn, 1));
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
		
		StorageEnv.close();
    }
    private static void test8() {
    	var begin = System.currentTimeMillis();
		Doubles.stringConverter().convert("1.00");
		int times = 10000;
		int total = 0;
		for (int i = 0; i < times; ++i) {
			for (int k = 0, j = 0; k < 10000; ++k, j += 1000) {
				var bytes = Shorts.toByteArray((short)j);
				total += Ints.fromByteArray(bytes);
			}
		}
		var cur = System.currentTimeMillis();
		int total2 = 0;
		for (int i = 0; i < times; ++i) {
			for (int k=0, j = 0; k < 10000; j += 1000, ++k) {
				var bytes = JStormUtils.intToBytes(j);
				total2 += JStormUtils.bytesToInt(bytes);
			}
		}
		
		Glog.debug("interval={},total={}", cur - begin, total);
		Glog.debug("interval2={}, total2={}", System.currentTimeMillis() - cur, total2);
    }
    public static class InitDb implements IDbHandle{
    	private int start = 0;
    	
    	public InitDb(int key) {
    		this.start = key;
    	}

    	@Override
    	public void handle(Transaction txn) throws RocksDBException {
			var player = StorageEnv.getStorage(EM_DB.PLAYER);
			int key = start;
			for (int i = 0; i < 20; ++i, ++key) {
				Glog.debug("key={},value={}", key, player.get(txn, key));
				player.put(txn, key, key + 1000);
			}
    	}
    }
    private static void initDB(String path, int key) {
    	StorageEnv.init(path);
    	new InitDb(key).baseHandle();
    	StorageEnv.close();
    }
    public static class PrintDB implements IDbHandle {
    	@Override
    	public void handle(Transaction txn) throws RocksDBException {
			var player = StorageEnv.getStorage(EM_DB.PLAYER);
			var it = player.newIterator();
			it.seekToFirst();
			while (it.isValid()) {
				Glog.debug("key={},value={}", Ints.fromByteArray(it.key()), Ints.fromByteArray(it.value()));
				it.next();
			}
		}
    }
    private static void test10() {
    	StorageEnv.init("d:/rocksdb_test1");
    	new PrintDB().baseHandle();
		StorageEnv.close();
    }
    
    private static void test9() {
    	initDB("d:/rocksdb_test1", 10);
    	initDB("d:/rocksdb_test2", 1000);
    	
    }
    
    private static void test11() {
    	MergeTable.merge("d:/rocksdb_test1", "d:/rocksdb_test2");
    	test10();
    	Glog.info("finish");
    }
	public static void main(String[] args) {
			Glog.info("test");
	}

}
