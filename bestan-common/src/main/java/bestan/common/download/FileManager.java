package bestan.common.download;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.io.FileUtils;

import com.google.common.collect.Lists;

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
	private BaseNetServerManager netServerManager;
	
	public static void deleteDirectory(String path) throws IOException {
		var file = new File(path);
		FileUtils.deleteDirectory(file);
	}
	
	private String getResourcePath() {
		return config.versionFilePath + config.resourceDir + version;
	}
	
	private void checkExpiredResource() throws IOException {
		if (lastChangeTime == 0) {
			return;
		}
		var curTime = BTimer.getTime();
		if (curTime - lastChangeTime <= config.resourceExpiredTime) {
			//尚未过期
			return;
		}
		lastChangeTime = 0;
		//关闭所有非当前资源的连接（假如不关闭，那么）
		netServerManager.closeChannels(new CloseChannelChecker(version));
	}
	
	//载入资源到内存
	private void loadFiles() {
		lock.lock();
		try {
			//记录变化时间
			lastChangeTime = BTimer.getTime();
			++version;
			curResource.loadResource(getResourcePath(), version);
		} catch (Exception e) {
			Glog.debug("loadFiles failed:error={}", ExceptionUtil.getLog(e));
		} finally {
			lock.unlock();
		}
	}
	
	public void checkLoad() throws IOException {
		checkExpiredResource();
		loadFiles();
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
				file1.getLastModified() == file2.getLastModified();
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
