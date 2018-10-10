package bestan.common.download;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.io.FileUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import bestan.common.log.Glog;
import bestan.common.net.server.BaseNetServerManager;
import bestan.common.protobuf.Proto.FileBaseInfo;
import bestan.common.protobuf.Proto.FileInfo;
import bestan.common.timer.BTimer;
import bestan.common.util.ExceptionUtil;

/**
 * @author yeyouhuan
 *
 */
public enum FileManager {
	INSTANCE;
	
	private FileResource curResource;
	private long lastChangeTime;
	private int version;
	
	protected Map<String, FileInfo> allFiles;
	protected FileInfo versionFileInfo;
	protected FileResourceConfig config;
	
	protected ReentrantReadWriteLock lock1 = new ReentrantReadWriteLock();
	private ReentrantLock lock = new ReentrantLock();
	private Map<Integer, Long> resourceTimeMap = Maps.newHashMap();
	private BaseNetServerManager netServerManager;
	private Long lastLoadResourceTime;
	
	public static void deleteDirectory(String path) throws IOException {
		var file = new File(path);
		FileUtils.deleteDirectory(file);
	}
	
	private String getResourcePath(int version) {
		return config.versionFilePath + config.resourceDir + version;
	}
	
	private void checkExpiredResource() throws IOException {
		if (resourceTimeMap.size() <= 1) {
			return;
		}
		var curTime = BTimer.getTime();
		if (curTime - lastChangeTime <= config.resourceExpiredTime) {
			//尚未过期
			return;
		}
		//关闭所有非当前资源的连接（假如不关闭，那么）
		netServerManager.closeChannels(new CloseChannelChecker(version));
		var it = resourceTimeMap.entrySet().iterator();
		while (it.hasNext()) {
			var entry = it.next();
			if (version == entry.getKey()) {
				//跳过当前资源目录
				continue;
			}
			//清理过期资源
			FileUtils.deleteDirectory(new File(getResourcePath(entry.getKey())));
			it.remove();
		}
	}
	
	//载入资源到内存
	private void loadFiles() {
		lock.lock();
		try {
			var curTime = BTimer.getTime();
			if (curTime - lastChangeTime <= config.updateChangeMinInterval) {
				return;
			}

			//记录变化时间
			lastChangeTime = BTimer.getTime();
			
			String path = config.versionFilePath + config.resourceDir + version;
			//清理目录
			FileUtils.deleteDirectory(new File(path));
			//将资源从更新目录拷贝到下载目录
			FileUtils.copyDirectory(new File(config.updatePath), new File(path));
			//载入资源
			var tempResource = new FileResource(path);
			curResource = tempResource;
		} catch (Exception e) {
			Glog.debug("loadFiles failed:error={}", ExceptionUtil.getLog(e));
		} finally {
			lock.unlock();
		}
	}
	
	public void checkLoad() throws IOException {
		checkExpiredResource();
		if (BTimer.getTime() - lastChangeTime <= config.updateChangeMinInterval) {
			return;
		}
		
		loadFiles();
	}
	
	private FileInfo getFileInfo(File file) {
		return null;
	}
	private static void addFile(File file, String partName) {
		Glog.trace("file:path={},part={}", file.getAbsolutePath(), partName);
	}
	private static void traverseFolder(File file, String partName) {
        if (file == null || !file.exists()) {
        	return;
        }

        partName += "/" + file.getName();
        if (!file.isDirectory()) {
        	addFile(file, partName);
        	return;
        }
        for (var it : file.listFiles()) {
        	traverseFolder(it, partName);
        }
    }
	public static void traverseFolder(String filePath)
	{
		traverseFolder(new File(filePath), ".");
	}
	
	public static boolean isEqual(FileBaseInfo file1, FileBaseInfo file2) {
		return file1.getFileName().equals(file2.getFileName()) && 
				file1.getFileCode().equals(file2.getFileCode());
	}
	
	public FileResource getResource() {
		return curResource;
	}
	
	public List<FileInfo> getUpdateList(Map<String, FileBaseInfo> req) {
		List<FileInfo> ret = null;
		for (var it : allFiles.entrySet()) {
			var reqInfo = req.get(it.getKey());
			if (reqInfo != null && isEqual(it.getValue().getBaseInfo(), reqInfo)) {
				//已经是一致的
				continue;
			}
			if (ret == null) {
				ret = Lists.newArrayList();
			}
			ret.add(it.getValue());
		}
		return ret;
	}
}
