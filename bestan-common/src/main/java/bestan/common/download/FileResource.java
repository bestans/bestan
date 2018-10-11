package bestan.common.download;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.omg.CORBA.PUBLIC_MEMBER;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import bestan.common.logic.FormatException;
import bestan.common.protobuf.Proto.FileBaseInfo;
import bestan.common.protobuf.Proto.FileInfo;

/**
 * @author yeyouhuan
 *
 */
public class FileResource {
	private int curVersion;
	private ReentrantLock lock = new ReentrantLock();
	private Map<String, FileResourceUnit> allResource = Maps.newHashMap();
	private String filePath;
	private String versionPath;
	private FileResourceUnit versionFile = null;
	
	public FileResource(String path) {
		filePath = path;
		curVersion = -1;
	}
	
	public void loadResource(String path, int version) {
		lock.lock();
		try {
			curVersion = version;
			FileManager.traverseFolder(filePath);
			versionFile = allResource.get(versionPath);
		} finally {
			lock.unlock();
		}
	}
	
	public void addFile(File file, String path) throws IOException {
		if (!file.exists()) {
			throw new FormatException("file doesn't exist.path=%s", path);
		}
		var fileInfo = FileInfo.newBuilder();
		fileInfo.getBaseInfoBuilder().setLastModified(file.lastModified());
		var size = (int)file.length();
		fileInfo.setSize(size);
		var data = new byte[size];
		var access = new RandomAccessFile(file, "r");
		try {
			access.read(data);
		} finally {
			access.close();
		}
		allResource.put(path, new FileResourceUnit(fileInfo.build(), data));
	}

	public boolean checkIsSameResource(long lastModified) {
		lock.lock();
		try {
			return versionFile.getLastModified() == lastModified;
		} finally {
			lock.unlock();
		}
	}
	
	public List<FileResourceUnit> getUpdateList(Map<String, FileBaseInfo> req) {
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
		} finally {
			lock.unlock();
		}
	}
	
	public static class UpdateListResult() { 
		public List<FileResourceUnit> updateList;
		public int version;
		
		public UpdateListResult(List<FileResourceUnit>)
	}
}
