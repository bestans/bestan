package bestan.common.download;

import java.util.List;
import java.util.Map;

import bestan.common.protobuf.Proto.FileBaseInfo;
import bestan.common.protobuf.Proto.FileInfo;

/**
 * @author yeyouhuan
 *
 */
public class FileResource {
	private int curVersion;

	public FileResource(String path) {
		
	}
	public void loadResource(String path) {
		
	}
	public boolean checkIsSameResource(FileInfo fileInfo) {
		return true;
	}
	
	public List<FileInfo> getUpdateList(Map<String, FileBaseInfo> req) {
		return null;
	}
	
	public int getVersion() {
		return curVersion;
	}
}
