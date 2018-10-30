package bestan.common.db;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.rocksdb.Checkpoint;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.FlushOptions;
import org.rocksdb.OptimisticTransactionDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;
import org.rocksdb.Transaction;
import org.rocksdb.WriteOptions;

import com.google.common.collect.Maps;

import bestan.common.db.RocksDBConfig.TableStruct;
import bestan.common.db.util.JStormUtils;
import bestan.common.log.Glog;

public class RocksDBState {
    protected static final String ROCKSDB_DATA_FILE_EXT = "sst";
    protected static final String SST_FILE_LIST = "sstFile.list";

    private RocksDBConfig config;

    private OptimisticTransactionDB txnDb;
    protected String rocksDbDir;
    protected String rocksDbCheckpointDir;
    protected Collection<String> lastCheckpointFiles;

    protected int ttlTimeSec;

    protected long lastCleanTime;
    protected long cleanPeriod;

    protected long lastSuccessBatchId;
    
    protected final Map<String, ColumnFamilyHandle> columnFamilyHandles = Maps.newHashMap();
    protected final Map<String, Storage> storages = Maps.newHashMap();
    
    public RocksDBState(RocksDBConfig config){
    	this.config = config;
    	config.tables.put(DBConst.DEFAULT_COLUMN_FAMILY, new TableStruct());
    }

    public ColumnFamilyHandle GetHandle(String tableName) {
    	return columnFamilyHandles.get(tableName);
    }
    
    public Storage getStorage(String tableName) {
    	return storages.get(tableName);
    }

    public void initEnv() {
        this.rocksDbDir = config.dbPath + "/db";
        this.rocksDbCheckpointDir = config.dbPath + "/checkpoint";
        initLocalRocksDbDir();
        initRocksDb();

        Glog.info("Local: dataDir={}, checkpointDir={}", rocksDbDir, rocksDbCheckpointDir);
    }
    
    protected void initRocksDb() {
        try {
        	txnDb = RocksDBOptionsFactory.createWithColumnFamily(config, rocksDbDir, columnFamilyHandles, storages);
            Glog.info("Finish the initialization of RocksDB");
        } catch (Exception e) {
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

    public void cleanup() {
        if (txnDb != null)
        	txnDb.close();
    }

    public RocksIterator newIterator(String tableName) {
    	return txnDb.newIterator(GetHandle(tableName));
    }

    /**
     * Flush the data in memtable of RocksDB into disk, and then create checkpoint
     * 
     * @param batchId
     */
    public void checkpoint(String batchId) {
        try {
            txnDb.flush(new FlushOptions());
            Checkpoint cp = Checkpoint.create(txnDb);
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
    
    void compactRange() throws RocksDBException {
    	txnDb.compactRange();
    }
    
    Transaction beginTransaction(WriteOptions writeOptions) {
    	return txnDb.beginTransaction(writeOptions);
    }
}
