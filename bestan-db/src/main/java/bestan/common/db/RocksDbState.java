package bestan.common.db;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.rocksdb.Checkpoint;
import org.rocksdb.ColumnFamilyDescriptor;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.ColumnFamilyOptions;
import org.rocksdb.FlushOptions;
import org.rocksdb.OptimisticTransactionDB;
import org.rocksdb.Options;
import org.rocksdb.ReadOptions;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;
import org.rocksdb.Transaction;
import org.rocksdb.WriteBatch;
import org.rocksdb.WriteOptions;

import com.google.common.collect.Lists;

import bestan.common.db.DBConst.EM_DB;
import bestan.common.db.util.JStormUtils;
import bestan.common.db.util.Utils;
import bestan.common.log.Glog;
import cn.hutool.core.date.DateUtil;

public class RocksDbState {
    protected static final String ROCKSDB_DATA_FILE_EXT = "sst";
    protected static final String SST_FILE_LIST = "sstFile.list";
    protected static final String ENABLE_METRICS = "rocksdb.hdfs.state.metrics";

    protected String topoGlogyName;
    protected Map conf;

    protected String stateName;

    public RocksDB rocksDb;
    public OptimisticTransactionDB txnDb;
    protected String rocksDbDir;
    protected String rocksDbCheckpointDir;
    protected Collection<String> lastCheckpointFiles;

    protected int ttlTimeSec;

    protected long lastCleanTime;
    protected long cleanPeriod;

    protected long lastSuccessBatchId;
    
    protected final List<ColumnFamilyHandle> columnFamilyHandles;
    protected final List<Storage> storages;
    
    public RocksDbState(){
    	columnFamilyHandles = Lists.newArrayList(new ColumnFamilyHandle[EM_DB.values().length]);
    	storages = Lists.newArrayList(new Storage[EM_DB.values().length]);
    }
    
    public ColumnFamilyHandle GetHandle(EM_DB dbType) {
    	return columnFamilyHandles.get(dbType.ordinal());
    }
    
    public Storage getStorage(EM_DB tableType) {
    	return storages.get(tableType.ordinal());
    }
    
    public void initEnv(String topoGlogyName, Map conf, String workerDir) {
        this.conf = conf;

        // init local rocksdb even
        this.rocksDbDir = workerDir + "/db";
        this.rocksDbCheckpointDir = workerDir + "/checkpoint";
        initLocalRocksDbDir();
        initRocksDb2();

        Glog.info("Local: dataDir={}, checkpointDir={}", rocksDbDir, rocksDbCheckpointDir);
    }
    
    protected void initRocksDb() {
        var options = RocksDbOptionsFactory.createOptions();
        try {
        	rocksDb = RocksDbOptionsFactory.createWithColumnFamily(conf, rocksDbDir, columnFamilyHandles, storages);
            Glog.info("Finish the initialization of RocksDB");
        } catch (IOException e) {
            Glog.error("Failed to open rocksdb located at {}, error={}", rocksDbDir, e.getMessage());
            throw new RuntimeException(e.getMessage());
        }

        lastCheckpointFiles = new HashSet<String>();
        lastCleanTime = System.currentTimeMillis();
        lastSuccessBatchId = -1;
    }

    protected void initRocksDb2() {
        var options = RocksDbOptionsFactory.createOptions();
        try {
        	txnDb = RocksDbOptionsFactory.createWithColumnFamily2(conf, rocksDbDir, columnFamilyHandles, storages);
            Glog.info("Finish the initialization of RocksDB");
        } catch (IOException e) {
            Glog.error("Failed to open rocksdb located at {}, error={}", rocksDbDir, e.getMessage());
            throw new RuntimeException(e.getMessage());
        }

        lastCheckpointFiles = new HashSet<String>();
        lastCleanTime = System.currentTimeMillis();
        lastSuccessBatchId = -1;
    }
    
    
    /**
     * 初始化db目录
     */
    private void initLocalRocksDbDir() {
        try {
        	FileUtils.forceMkdir(new File(rocksDbDir));
            FileUtils.forceMkdir(new File(rocksDbCheckpointDir));
        } catch (IOException e) {
            Glog.error("Failed to create dir for dbpath={}, checkpointPath={}, error={}",
            		rocksDbDir, rocksDbCheckpointDir, e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
    
    private List<ColumnFamilyDescriptor> getExistingColumnFamilyDesc(Options options) {
        try {
            List<byte[]> families = Lists.newArrayList();
            List<byte[]> existingFamilies = RocksDB.listColumnFamilies(options, rocksDbDir);
            if (existingFamilies != null) {
                families.addAll(existingFamilies);
            } else {
                families.add(RocksDB.DEFAULT_COLUMN_FAMILY);
            }

            ColumnFamilyOptions familyOptions = RocksDbOptionsFactory.createColumnFamilyOptions();
            List<ColumnFamilyDescriptor> columnFamilyDescriptors = new ArrayList<>();
            for (byte[] bytes : families) {
                columnFamilyDescriptors.add(new ColumnFamilyDescriptor(bytes, familyOptions));
                Glog.info("Load column family of {}", new String(bytes));
            }
            return columnFamilyDescriptors;
        } catch (RocksDBException e) {
            throw new RuntimeException("Failed to retrieve existing column families.", e);
        }
    }
    
    public void put(EM_DB dbType, byte[] key, byte[] value) {
        try {
            rocksDb.put(columnFamilyHandles.get(dbType.ordinal()), key, value);
        } catch (RocksDBException e) {
            Glog.error("Failed to put data, key={}, value={}", key, value);
            throw new RuntimeException(e.getMessage());
        }
    }

    public void putBatch(Map<byte[], byte[]> batch) {
        try {
            WriteBatch writeBatch = new WriteBatch();
            for (Map.Entry<byte[], byte[]> entry : batch.entrySet()) {
                writeBatch.put(entry.getKey(), entry.getValue());
            }
            rocksDb.write(new WriteOptions(), writeBatch);
        } catch (RocksDBException e) {
            Glog.error("Failed to put batch={}", batch);
            throw new RuntimeException(e.getMessage());
        }
    }

    public void cleanup() {
        if (rocksDb != null)
            rocksDb.close();
        
        if (txnDb != null)
        	txnDb.close();
    }

    public RocksIterator newIterator(EM_DB table_type) {
    	return txnDb.newIterator(GetHandle(table_type));
    }

    /**
     * Flush the data in memtable of RocksDB into disk, and then create checkpoint
     * 
     * @param batchId
     */
    public void checkpoint(String batchId) {
        try {
            rocksDb.flush(new FlushOptions());
            Checkpoint cp = Checkpoint.create(rocksDb);
            cp.createCheckpoint(getLocalCheckpointPath(batchId));
        } catch (RocksDBException e) {
            Glog.error("Failed to create checkpoint for batch-{},error={}", batchId, e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * remove obsolete checkpoint data at local disk and remote backup storage
     * 
     * @param batchId id of success batch
     */
    public void remove(long batchId) {
        removeObsoleteLocalCheckpoints(batchId);
    }

    private String getLocalCheckpointPath(String batchId) {
        return rocksDbCheckpointDir + "/" + batchId;
    }

    private Collection<String> getFileList(Collection<File> files) {
        Collection<String> ret = new HashSet<String>();
        for (File file : files)
            ret.add(file.getName());
        return ret;
    }

    private void removeObsoleteLocalCheckpoints(long successBatchId) {
        File cpRootDir = new File(rocksDbCheckpointDir);
        for (String cpDir : cpRootDir.list()) {
            try {
                long cpId = JStormUtils.parseLong(cpDir);
                if (cpId < successBatchId)
                    FileUtils.deleteQuietly(new File(rocksDbCheckpointDir + "/" + cpDir));
            } catch (Throwable e) {
                File file = new File(rocksDbCheckpointDir + "/" + cpDir);
                // If existing more thant one hour, remove the unexpected file
                if (System.currentTimeMillis() - file.lastModified() > 60 * 60 * 1000) {
                    Glog.debug("Unexpected file-" + cpDir + " in local checkpoint dir, " + rocksDbCheckpointDir, e);
                    FileUtils.deleteQuietly(file);
                }
            }
        }
    }

    public static void test1() {
        Map conf = new HashMap<Object, Object>();
        conf.putAll(Utils.loadConf("conf.property"));
        DBConst.init();
        RocksDbState state = new RocksDbState();
        state.initEnv("test", conf, "d:/rocksdb_test");
        ColumnFamilyHandle handle = state.GetHandle(EM_DB.PLAYER);
        RocksIterator itr = state.rocksDb.newIterator(handle);
        itr.seekToFirst();
        while (itr.isValid()) {
        	Glog.debug("old_value:key={},value={}", JStormUtils.bytesToInt(itr.key()), JStormUtils.bytesToInt(itr.value()));
        	itr.next();
        }
        
        // setup checkpoint
        int batchNum = JStormUtils.parseInt(conf.get("batch.num"), 100);
        for (int i = 0; i < batchNum; i++) {
            state.put(EM_DB.PLAYER, JStormUtils.intToBytes(i), JStormUtils.intToBytes(i));
        }

        state.checkpoint(DateUtil.format(DateUtil.date(), "yyyyMMddHHmmss"));
    }

    public static void getValue(Transaction txn, ColumnFamilyHandle handle, WriteOptions wOp, ReadOptions rOp, int key) throws RocksDBException {
    	var vBytes = txn.get(handle, rOp, JStormUtils.intToBytes(key));
    	if (vBytes != null) {
    		Glog.debug("getvalue:key={},value={}", key, JStormUtils.bytesToInt(vBytes));
    	} else {
    		Glog.debug("getvalue:key={},value=null", key);
    	}
    }
    public static void setValue(Transaction txn, ColumnFamilyHandle handle, int key, int value) throws RocksDBException {
    	txn.put(handle, JStormUtils.intToBytes(key), JStormUtils.intToBytes(value));
    	Glog.debug("setvalue:key={},value={}", key, value);
    }

    public static void test2() {
//        final Options options = new Options()
//                .setCreateIfMissing(true);
//    	try {
//			var txnDb1 = OptimisticTransactionDB.open(options, "d:/rocksdb_test");
//		} catch (RocksDBException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
        Map conf = new HashMap<Object, Object>();
        conf.putAll(Utils.loadConf("conf.property"));
        DBConst.init();
        RocksDbState state = new RocksDbState();
        state.initEnv("test", conf, "d:/rocksdb_test");
        ColumnFamilyHandle handle = state.GetHandle(EM_DB.PLAYER);
        var txnDb = state.txnDb;
        var wOp = new WriteOptions();
        var rOp = new ReadOptions();
        try {
        	var txn = txnDb.beginTransaction(wOp);
        	getValue(txn, handle, wOp, rOp, 100);
        	setValue(txn, handle, 100, 103);
        	//getValue(txn, handle, wOp, rOp, 100);
        	txnDb.put(handle, JStormUtils.intToBytes(100), JStormUtils.intToBytes(103));
        	getValue(txn, handle, wOp, rOp, 100);
        	txn.commit();
        	//txn.rollback();
        } catch (RocksDBException e) {
			// TODO Auto-generated catch block
        	//System.out.println(e.ge);
			//e.printStackTrace();
		} finally {
			// TODO: handle finally clause
		}
    }
    public static void test3() {
      Map conf = new HashMap<Object, Object>();
      conf.putAll(Utils.loadConf("conf.property"));
      DBConst.init();
      RocksDbState state = new RocksDbState();
      state.initEnv("test", conf, "d:/rocksdb_test");
      ColumnFamilyHandle handle = state.GetHandle(EM_DB.PLAYER);
      var txnDb = state.txnDb;

      try {
		  for (int i = 100; i > 0; --i)
		  {
			  txnDb.put(handle, JStormUtils.intToBytes(i), JStormUtils.intToBytes(i* 3));
		  }
      } catch (RocksDBException e) {
			// TODO Auto-generated catch block
		e.printStackTrace();
      }
      var it = txnDb.newIterator(handle);
      for (it.seek(JStormUtils.intToBytes(91)); it.isValid(); it.next()) {
    	  Glog.debug("value:key={},value={}", JStormUtils.bytesToInt(it.key()), JStormUtils.bytesToInt(it.value()));
      }
  }
    public static void main(String[] args) {
    	test2();
    }
}
