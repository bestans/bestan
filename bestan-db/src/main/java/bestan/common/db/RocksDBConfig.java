package bestan.common.db;

import java.util.Map;

import org.rocksdb.CompactionStyle;
import org.rocksdb.util.SizeUnit;

import com.google.common.collect.Maps;

import bestan.common.db.TableDataProcess.TableDataType;
import bestan.common.logic.FormatException;
import bestan.common.lua.BaseLuaConfig;
import bestan.common.lua.LuaAnnotation;
import bestan.common.lua.LuaException;
import bestan.common.lua.LuaParamAnnotation;
import bestan.common.lua.LuaParamAnnotation.LuaParamPolicy;
import bestan.common.message.IMessageLoadFinishCallback;
import bestan.common.message.MessageFactory;

@LuaAnnotation(load = false, optional = true)
public class RocksDBConfig extends BaseLuaConfig {
	public static final RocksDBConfig option = new RocksDBConfig();
	
	/**
	 * 数据库所在路径 
	 */
	public String dbPath;

	/**
	 * 数据库table表，key是数据库表名，value是解析的数据格式
	 */
	@LuaParamAnnotation(policy=LuaParamPolicy.REQUIRED)
	public Map<String, TableStruct> tables = Maps.newHashMap();

	/**
	 * 检查表message有效性
	 */
	public boolean checkTableMessageValid = false;
	
	/**
	 * If true, the database will be created if it is missing
	 * Default: false
	 */
	public boolean createIfMissing = false;

	/**
	 * If true, missing column families will be automatically created.
	 * Default: false
	 */
	public boolean createMissingColumnFamilies = false;
	
	/**
	 * Maximal info log files to be kept.
	 * Default: 100
	 */
	public int keepLogFileNum = 100;
	
	/**
	 * The periodicity when obsolete files get deleted. The default
	 * value is 6 hours. The files that get out of scope by compaction
	 * process will still get automatically delete on every compaction,
	 * regardless of this setting
	 */
	public long deleteObsoleteFilesPeriodMicros = 6L * 60 * 60 * 1000000;
	
	/**
	 * The maximum number of write buffers that are built up in memory.
	 * The default and the minimum number is 2, so that when 1 write buffer
	 * is being flushed to storage, new writes can continue to the other write buffer.
	 * If max_write_buffer_number > 3, writing will be slowed down to options.
	 * delayed_write_rate if we are writing to the last write buffer allowed.
	 * 
	 * DEFAULT:4
	 */
	public int maxWriteBufferNumber = 4;
	
	/**
	 * Amount of data to build up in memory (backed by an unsorted log on disk) 
	 * before converting to a sorted on-disk file. 
	 * Larger values increase performance, especially during bulk loads.
	 * Up to max_write_buffer_number write buffers may be held in memory at the same time,
	 * so you may wish to adjust this parameter to control memory usage.
	 * Also, a larger write buffer will result in a longer recovery time
	 * the next time the database is opened.
	 * Note that write_buffer_size is enforced per column family.
	 * See db_write_buffer_size for sharing memory across column families.
	 * Default: 64MB
	 * 
	 * the memory used by memtables is capped at maxWriteBufferNumber*writeBufferSize. 
	 * When that is reached, any further writes are blocked until the flush finishes and frees memory used by the memtables.
	 * 期望的内存使用4 * 64M = 256M 
	 */
	public long writeBufferSize = 64 * SizeUnit.MB;
	
	
	/**
	 * The minimum number of write buffers that will be merged together
	 * before writing to storage.  If set to 1, then 
	 * all write buffers are flushed to L0 as individual files and this increases
	 * read amplification because a get request has to check in all of these
	 * files. Also, an in-memory merge may result in writing lesser
	 * data to storage if there are duplicate records in each of these
	 * individual write buffers.  
	 * 
	 * min_write_buffer_number_to_merge is the minimum number of memtables to be
	 * merged before flushing to storage. For example, if this option is set to 2,
	 * immutable memtables are only flushed when there are two of them - a single
	 * immutable memtable will never be flushed. If multiple memtables are merged together,
	 * less data may be written to storage since two updates are merged to a single key.
	 * However, every Get() must traverse all immutable memtables linearly to check if the key is there.
	 * Setting this option too high may hurt read performance.
	 * Default: 1
	 */
	public int minWriteBufferNumberToMerge = 1; 

	/**
	 * Target file size for compaction.
	 * target_file_size_base is per-file size for level-1.
	 * Target file size for level L can be calculated by
	 * target_file_size_base * (target_file_size_multiplier ^ (L-1))
	 * For example, if target_file_size_base is 2MB and
	 * target_file_size_multiplier is 10, then each file on level-1 will
	 * be 2MB, and each file on level 2 will be 20MB,
	 * and each file on level-3 will be 200MB.
	 * 
	 * Default: 64MB.
	 * 
	 * target_file_size_base and target_file_size_multiplier --
	 * Files in level 1 will have target_file_size_base bytes. 
	 * Each next level's file size will be target_file_size_multiplier 
	 * bigger than previous one. However, by default target_file_size_multiplier is 1,
	 * so files in all L1..Lmax levels are equal. 
	 * Increasing target_file_size_base will reduce total number of database files, 
	 * which is generally a good thing. We recommend setting target_file_size_base 
	 * to be maxBytesForLevelBase / 10, so that there are 10 files in level 1.
	 */
	public long targetFileSizeBase = 32 * SizeUnit.MB;
	
	/**
	 * Control maximum total data size for a level.
	 * max_bytes_for_level_base is the max total for level-1.
	 * Maximum number of bytes for level L can be calculated as
	 * (max_bytes_for_level_base) * (max_bytes_for_level_multiplier ^ (L-1))
	 * For example, if max_bytes_for_level_base is 200MB, and if
	 * max_bytes_for_level_multiplier is 10, total data size for level-1
	 * will be 200MB, total file size for level-2 will be 2GB,
	 * and total file size for level-3 will be 20GB.
	 * 
	 * max_bytes_for_level_base and max_bytes_for_level_multiplier -- 
	 * max_bytes_for_level_base is total size of level 1. As mentioned, 
	 * we recommend that this be around the size of level 0. 
	 * Each subsequent level is max_bytes_for_level_multiplier larger than previous one. 
	 * The default is 10 and we do not recommend changing that.
	 * 
	 * Default: 256MB.
	 */
	public long maxBytesForLevelBase = 256 * SizeUnit.MB;
	
	/**
	 * Number of open files that can be used by the DB.  You may need to
	 * increase this if your database has a large working set. Value -1 means
	 * files opened are always kept open. You can estimate number of files based
	 * on target_file_size_base and target_file_size_multiplier for level-based
	 * compaction. For universal-style compaction, you can usually set it to -1.
	 * 
	 * max_open_files -- RocksDB keeps all file descriptors in a table cache. 
	 * If number of file descriptors exceeds max_open_files, 
	 * some files are evicted from table cache and their file descriptors closed.
	 * This means that every read must go through the table cache to lookup the file needed. 
	 * Set max_open_files to -1 to always keep all files open, which avoids expensive table cache calls.
	 * Default: -1
	 */
	public int maxOpenFiles = -1;
	
	/**
	 * Maximum number of concurrent background jobs (compactions and flushes).
	 */
	public int maxBackgroundJobs = 2;

	@LuaParamAnnotation(policy=LuaParamPolicy.OPTIONAL)
	public CompactionStyle compactionStyle = CompactionStyle.LEVEL;
	
	/**
	 * Number of files to trigger level-0 compaction. A value <0 means that
	 * level-0 compaction will not be triggered by number of files at all.
	 * Default: 4
	 */
	public int levelZeroFileNumCompactionTrigger = 4;
	
	
	/**
	 * Soft limit on number of level-0 files. We start slowing down writes at this
	 * point. A value <0 means that no writing slow down will be triggered by
	 * number of files in level-0.
	 * Default: 20
	 */
	public int levelZeroSlowdownWritesTrigger = 20;
	
	/**
	 * Maximum number of level-0 files.  We stop writes at this point.
	 * Default: 30
	 */
	public int levelZeroStopWritesTrigger = 30;
	
	
	/**
	 * Number of levels for this database
	 * Default: 5
	 */
	public int numLevels = 5;

	/**
	 * table_factory -- Defines the table format. Here's the list of tables we support:
	 * Block based -- This is the default table. It is suited for storing data on disk and flash storage. 
	 *		It is addressed and loaded in block sized chunks (see block_size option). Thus the name block based.
	 * Plain Table -- Only makes sense with prefix_extractor.
	 * 		It is suited for storing data on memory (on tmpfs filesystem). It is byte-addressible.
	 * 默认使用Block based
	 * 以下都是BlockBasedTableConfig配置项
	 */
	
	/**
	 * Approximate size of user data packed per block.  Note that the
	 * block size specified here corresponds to uncompressed data. 
	 * The actual size of the unit read from disk may be smaller if compression is enabled. 
	 * This parameter can be changed dynamically.
	 */
	public long blockSize = 32 * SizeUnit.KB;
	
	public long blockCacheSize = 64 * SizeUnit.MB;
	
	public boolean cacheIndexAndFilterBlocks = true;

	public final static class TableStruct extends BaseLuaConfig implements IMessageLoadFinishCallback {
		@LuaParamAnnotation(policy=LuaParamPolicy.REQUIRED)
		public TableDataType keyType;
		@LuaParamAnnotation(policy=LuaParamPolicy.REQUIRED)
		public TableDataType valueType;
		@LuaParamAnnotation(policy=LuaParamPolicy.OPTIONAL)
		public String keyName = "";
		@LuaParamAnnotation(policy=LuaParamPolicy.OPTIONAL)
		public String valueName = "";
		
		@LuaParamAnnotation(policy=LuaParamPolicy.OPTIONAL)
		public TableDataProcess keyProcess;
		@LuaParamAnnotation(policy=LuaParamPolicy.OPTIONAL)
		public TableDataProcess valueProcess;

		@Override
		public void afterLoad() throws LuaException {
			keyProcess = new TableDataProcess(keyType);
			valueProcess = new TableDataProcess(valueType);
			
			//注册回调，当message载入后，回调计算message
			MessageFactory.registerLoadFinishCallback(this);
		}

		@Override
		public void onMessageLoadFinish() throws Exception {
			if (keyType == TableDataType.MESSAGE) {
				var instance = MessageFactory.getMessageInstance(keyName);
				if (null == instance) {
					throw new FormatException("TableStruct parse key message type failed:invalid keyName=%s", keyName); 
				}
				keyProcess.setMessageInstance(instance);
			}
			if (valueType == TableDataType.MESSAGE) {
				var instance = MessageFactory.getMessageInstance(valueName);
				if (null == instance) {
					throw new FormatException("TableStruct parse value message type failed:invalid valueName=%s", valueName); 
				}
				valueProcess.setMessageInstance(instance);
			}
		}
	}
}
