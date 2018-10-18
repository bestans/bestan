package bestan.common.download;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import bestan.common.log.Glog;
import bestan.common.logic.FormatException;
import bestan.common.protobuf.Proto.FileBaseInfo;
import bestan.common.protobuf.Proto.FileInfo;
import bestan.common.util.ExceptionUtil;
import bestan.common.util.PairData;

/**
 * @author yeyouhuan
 *
 */
public class FileResource {
	private int curVersion = 0;
	private ReentrantLock lock = new ReentrantLock();
	private Map<String, FileResourceUnit> allResource = Maps.newHashMap();
	private Map<String, FileResourceUnit> loadResource = Maps.newHashMap();
	private FileResourceUnit versionFile;
	private FileResourceConfig config;
	
	public FileResource(FileResourceConfig config) {
		this.config = config;
	}
	
	public boolean loadResource(int version) {
		lock.lock();
		try {
			curVersion = version;
			loadResource.clear();
			FileManager.traverseFolder(config.resourceDir, this);
			
			allResource = loadResource;
			loadResource = Maps.newHashMap();
			
			versionFile = allResource.get(config.versionFile);
			if (null == versionFile) {
				return false;
			}
			allResource.remove(config.versionFile);
			return true;
		} catch (Exception e) {
			Glog.error("FileResource load failed.path={},exception={}", config.resourceDir, ExceptionUtil.getLog(e));
		} finally {
			lock.unlock();
		}
		return false;
	}
	
	public void addFile(File file, String path) throws IOException {
		if (!file.exists()) {
			throw new FormatException("file doesn't exist.path=%s", path);
		}
		var oldFile = allResource.get(path);
		if (oldFile != null && oldFile.getLastModified() == file.lastModified()) {
			//Glog.debug("addFile {} ignore because file not change", path);
			loadResource.put(path, oldFile);
			return;
		}
		var fileInfo = FileInfo.newBuilder();
		fileInfo.getBaseInfoBuilder().setLastModified(file.lastModified());
		fileInfo.getBaseInfoBuilder().setFileName(path);
		var size = (int)file.length();
		if (size <= 0) {
			throw new FormatException("file is empty.filename=%s", path); 
		}
		fileInfo.setSize(size);
		var data = new byte[size];
		var access = new RandomAccessFile(file, "r");
		try {
			access.read(data);
		} finally {
			access.close();
		}
		loadResource.put(path, new FileResourceUnit(fileInfo.build(), data));
		
		Glog.debug("addFile path={},lastModified={}", path, file.lastModified());
	}

	public boolean checkIsSameResource(long lastModified) {
		lock.lock();
		try {
			return versionFile.getLastModified() == lastModified;
		} finally {
			lock.unlock();
		}
	}
	
	public PairData<List<FileResourceUnit>, Integer> getUpdateList(Map<String, FileBaseInfo> req) {
		lock.lock();
		try {
			List<FileResourceUnit> ret = null;
			for (var it : allResource.entrySet()) {
				var reqInfo = req.get(it.getKey());
				if (reqInfo != null && it.getValue().getLastModified() == reqInfo.getLastModified()) {
					//已经是一致的
					continue;
				}
				if (ret == null) {
					ret = Lists.newArrayList();
				}
				ret.add(it.getValue());
			}
			return PairData.newPair(ret, curVersion);
		} finally {
			lock.unlock();
		}
	}
}
