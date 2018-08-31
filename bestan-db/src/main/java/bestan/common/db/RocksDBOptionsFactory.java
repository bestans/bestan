/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package bestan.common.db;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.rocksdb.BlockBasedTableConfig;
import org.rocksdb.BloomFilter;
import org.rocksdb.ColumnFamilyDescriptor;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.ColumnFamilyOptions;
import org.rocksdb.DBOptions;
import org.rocksdb.OptimisticTransactionDB;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.StringAppendOperator;

import com.google.common.collect.Lists;

import bestan.common.log.Glog;
import bestan.common.message.MessageFactory;

public class RocksDBOptionsFactory {
    private static final int DEFAULT_BLOOM_FILTER_BITS = 10;
    private static final String DEFAULT_COLUMN_FAMILY = "default";

    public static Options createOptions(RocksDBConfig cfg) {
    	var option = new Options();
    	option.setCreateIfMissing(cfg.createIfMissing);
    	option.setCreateMissingColumnFamilies(cfg.createMissingColumnFamilies);
    	option.setKeepLogFileNum(cfg.keepLogFileNum);
    	option.setDeleteObsoleteFilesPeriodMicros(cfg.deleteObsoleteFilesPeriodMicros);
    	option.setMaxWriteBufferNumber(cfg.maxWriteBufferNumber);
    	option.setWriteBufferSize(cfg.writeBufferSize);
    	option.setMinWriteBufferNumberToMerge(cfg.minWriteBufferNumberToMerge);
    	option.setTargetFileSizeBase(cfg.targetFileSizeBase);
    	option.setMaxBytesForLevelBase(cfg.maxBytesForLevelBase);
    	option.setMaxOpenFiles(cfg.maxOpenFiles);
    	option.setMaxBackgroundJobs(cfg.maxBackgroundJobs);
    	option.setCompactionStyle(cfg.compactionStyle);
    	option.setLevelZeroFileNumCompactionTrigger(cfg.levelZeroFileNumCompactionTrigger);
    	option.setLevelZeroSlowdownWritesTrigger(cfg.levelZeroSlowdownWritesTrigger);
    	option.setLevelZeroStopWritesTrigger(cfg.levelZeroStopWritesTrigger);
    	option.setNumLevels(cfg.numLevels);
    	
    	var tableOptions = new BlockBasedTableConfig();
    	tableOptions.setBlockSize(cfg.blockSize);
    	tableOptions.setBlockCacheSize(cfg.blockCacheSize);
    	tableOptions.setCacheIndexAndFilterBlocks(cfg.cacheIndexAndFilterBlocks);
    	option.setTableFormatConfig(tableOptions);
    	return option;
    }
    public static Options createOptions() {
    	return createOptions(RocksDBConfig.option);
    }

    public static DBOptions createDefaultDbOptions(DBOptions currentOptions) {
        if (currentOptions == null)
            currentOptions = new DBOptions();
        currentOptions.setCreateIfMissing(true);
        currentOptions.setCreateMissingColumnFamilies(true);
        //currentOptions.setMemTableConfig(new HashLinkedListMemTableConfig());
        //currentOptions.setStatsDumpPeriodSec(300);
        //currentOptions.createStatistics();
        currentOptions.setMaxOpenFiles(-1);
        currentOptions.setMaxBackgroundFlushes(2);
        currentOptions.setMaxBackgroundCompactions(2);
        return currentOptions;
    }

    public static ColumnFamilyOptions createColumnFamilyOptions(RocksDBConfig cfg) {
    	var option = new ColumnFamilyOptions();
    	option.setMergeOperator(new StringAppendOperator());
    	option.setMaxWriteBufferNumber(cfg.maxWriteBufferNumber);
    	option.setWriteBufferSize(cfg.writeBufferSize);
    	option.setTargetFileSizeBase(cfg.targetFileSizeBase);
    	option.setCompactionStyle(cfg.compactionStyle);
    	option.setLevelZeroFileNumCompactionTrigger(cfg.levelZeroFileNumCompactionTrigger);
    	option.setLevelZeroSlowdownWritesTrigger(cfg.levelZeroSlowdownWritesTrigger);
    	option.setLevelZeroStopWritesTrigger(cfg.levelZeroStopWritesTrigger);
    	option.setNumLevels(cfg.numLevels);
    	option.setMaxBytesForLevelBase(cfg.maxBytesForLevelBase);
    	
    	var tableOptions = new BlockBasedTableConfig();
    	tableOptions.setBlockSize(cfg.blockSize);
    	tableOptions.setBlockCacheSize(cfg.blockCacheSize);
    	tableOptions.setFilter(new BloomFilter(DEFAULT_BLOOM_FILTER_BITS, false));
    	tableOptions.setCacheIndexAndFilterBlocks(cfg.cacheIndexAndFilterBlocks);
    	option.setTableFormatConfig(tableOptions);
    	return option;
    }
    public static ColumnFamilyOptions createColumnFamilyOptions() {
    	return createColumnFamilyOptions(RocksDBConfig.option);
    }
    public static List<ColumnFamilyDescriptor> getExistingColumnFamilyDescFinal(RocksDBConfig config, Options options, String rocksDbDir) {
        try {
            List<String> strfamilies = Lists.newArrayList();
            List<byte[]> oldFamilies = RocksDB.listColumnFamilies(options, rocksDbDir);
            List<byte[]> families = Lists.newArrayList();
            if (oldFamilies != null) {
            	families.addAll(oldFamilies);
            }
            for (byte[] temp_b : oldFamilies) {
            	strfamilies.add(new String(temp_b));
            }
        	for (var it : config.tables.entrySet()) {
        		String dbName = it.getKey();
        		boolean find = false;
        		for (String old_db : strfamilies) {
	        		if (old_db.equals(dbName)) {
	        			find = true;
	        			break;
	        		}
        		}
        		if (!find) {
        			families.add(dbName.getBytes());
        		}
        	}

            ColumnFamilyOptions familyOptions = RocksDBOptionsFactory.createColumnFamilyOptions();
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
    
    public static List<ColumnFamilyDescriptor> getExistingColumnFamilyDesc(Options options, String rocksDbDir) {
        try {
            List<String> strfamilies = Lists.newArrayList();
            List<byte[]> oldFamilies = RocksDB.listColumnFamilies(options, rocksDbDir);
            List<byte[]> families = Lists.newArrayList();
            if (oldFamilies != null) {
            	families.addAll(oldFamilies);
            }
            for (byte[] temp_b : oldFamilies) {
            	strfamilies.add(new String(temp_b));
            }
        	for (String db_name : DBConst.dbDescs) {
        		boolean find = false;
        		for (String old_db : strfamilies) {
	        		if (old_db.equals(db_name)) {
	        			find = true;
	        			break;
	        		}
        		}
        		if (!find) {
        			families.add(db_name.getBytes());
        		}
        	}

            ColumnFamilyOptions familyOptions = RocksDBOptionsFactory.createColumnFamilyOptions();
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
    
    public static OptimisticTransactionDB createWithColumnFamily(final RocksDBConfig config, String rocksDbDir, Map<String, ColumnFamilyHandle> columnFamilyHandles, final Map<String, Storage> storages) throws Exception {
        final Options options = new Options()
                .setCreateIfMissing(true);
        DBOptions dbOptions = RocksDBOptionsFactory.createDefaultDbOptions(null);
        List<ColumnFamilyDescriptor> columnFamilyDescriptors = getExistingColumnFamilyDescFinal(config, options, rocksDbDir);

        try {
        	List<ColumnFamilyHandle> srcColumnFamilyHandles = Lists.newArrayList();
            var txnDb = OptimisticTransactionDB.open(dbOptions, rocksDbDir, columnFamilyDescriptors, srcColumnFamilyHandles);
            int n = Math.min(columnFamilyDescriptors.size(), srcColumnFamilyHandles.size());
            for (int i = 0; i < n; i++) {
                ColumnFamilyDescriptor descriptor = columnFamilyDescriptors.get(i);
                String tableName = new String(descriptor.getName());
                String messageName = config.tables.get(tableName);
                if (null == messageName) {
                	continue;
                }
                if (srcColumnFamilyHandles.get(i) == null) {
            		throw new IOException("Failed to initialize RocksDb. empty family handle.");
                }
                var messageInstance = MessageFactory.getMessageInstance(messageName);
//        		if (messageInstance == null)
//        			throw new FormatException("Storage construct failed: invalid table value message, table=%s, message=%s", 
//        					tableName, messageName);
        		
                columnFamilyHandles.put(tableName, srcColumnFamilyHandles.get(i));
                storages.put(tableName, new Storage(tableName, messageInstance, srcColumnFamilyHandles.get(i), txnDb));
            }
            Glog.info("Finished loading RocksDB with existing column dbPath={},desc_size={}",
            		rocksDbDir, columnFamilyDescriptors.size());
            // enable compaction
            txnDb.compactRange();
            return txnDb;
        } catch (RocksDBException e) {
            throw new IOException("Failed to initialize RocksDb." + e.getMessage());
        }
    }
    
    public static OptimisticTransactionDB createWithColumnFamily2(final Map conf, String rocksDbDir, final List<ColumnFamilyHandle> columnFamilyHandles, final List<Storage> storages) throws IOException {
        //Options options = RocksDbOptionsFactory.createDefaultOptions(null);
        final Options options = new Options()
                .setCreateIfMissing(true);
        DBOptions dbOptions = RocksDBOptionsFactory.createDefaultDbOptions(null);
        List<ColumnFamilyDescriptor> columnFamilyDescriptors = getExistingColumnFamilyDesc(options, rocksDbDir);

        try {
        	List<ColumnFamilyHandle> srcColumnFamilyHandles = Lists.newArrayList();
            var txnDb = OptimisticTransactionDB.open(dbOptions, rocksDbDir, columnFamilyDescriptors, srcColumnFamilyHandles);
            int n = Math.min(columnFamilyDescriptors.size(), srcColumnFamilyHandles.size());
            for (int i = 0; i < n; i++) {
                ColumnFamilyDescriptor descriptor = columnFamilyDescriptors.get(i);
                String tableName = new String(descriptor.getName());
//                if (tableName.equals(DBConst.DEFAULT_COLUMN_FAMILY)) {
//                	continue;
//                }
                var tableType = DBConst.dbDescMap.get(tableName);
                var tableIndex = tableType.ordinal();
                if (columnFamilyHandles.get(tableIndex) != null) {
                	throw new IOException("Failed to initialize RocksDb. duplicate family");
                }
                columnFamilyHandles.set(tableIndex, srcColumnFamilyHandles.get(i));
                //storages.set(tableIndex, new Storage(tableType, srcColumnFamilyHandles.get(i), txnDb));
            }
            for (ColumnFamilyHandle handle : columnFamilyHandles) {
            	if (handle == null) {
            		throw new IOException("Failed to initialize RocksDb. empty family handle.");
            	}
            }
            Glog.info("Finished loading RocksDB with existing column dbPath={},desc_size={},handle_size={}",
            		rocksDbDir, columnFamilyDescriptors.size(), columnFamilyHandles.size());
            // enable compaction
            txnDb.compactRange();
            return txnDb;
        } catch (RocksDBException e) {
            throw new IOException("Failed to initialize RocksDb." + e.getMessage());
        }
    }
}