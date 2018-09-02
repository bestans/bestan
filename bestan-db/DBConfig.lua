--1kb
local KB = 1024
--1兆
local MB = KB * KB

local config =
{
    --数据库路径
    dbPath = "d:/db_test1",
    
    --数据库表
    --key：表名
    --value：keyType指key类型，valueType指value类型，valueName和keyName仅在类型为message时有效
    tables = {
		player = { keyType = "INT", valueType = "INT" },
    },
    
    --检查table解析的message是否有效
    checkTableMessageValid = false,
    
    --如果是true，当数据库不存在是将会创建
    createIfMissing = true,
    
    --如果是true，当数据库表不存在时将会创建
    createMissingColumnFamilies = true;
    
    --保留的log最大数目
    keepLogFileNum = 100,
    
    --废弃日志文件删除间隔时间（微妙）
    deleteObsoleteFilesPeriodMicros = 6 * 60 * 60 * 1000000,
    
    --writebuffer最大数量
    maxWriteBufferNumber = 4,
    
    --writebuffer大小（字节）
    writeBufferSize = 64 * MB,
    
    --触发memtables写入到数据库的最少immutable memtables数量
    minWriteBufferNumberToMerge = 1,
    
    --level-1每个文件的大小
    targetFileSizeBase = 32 * MB,
    
    --level-1数据大小上限
    maxBytesForLevelBase = 256 * MB,
    
    --推荐-1，表示打开的文件会一直打开
    maxOpenFiles = -1,
    
    --内部工作（compactions and flushes）最大数量
    maxBackgroundJobs = 2,
    
    --触发level-0 compaction的文件数量
    levelZeroFileNumCompactionTrigger = 4,
    
    --触发减缓写入的level-0的文件数量
    levelZeroSlowdownWritesTrigger = 20,
    
    --触发停止写入的level-0的文件数量
    levelZeroStopWritesTrigger = 30,
    
    --数据库分几层
    numLevels = 5,
    
    blockSize = 32 * KB,
 
    blockCacheSize = 64 * MB,
    
    cacheIndexAndFilterBlocks = true,
}

return config