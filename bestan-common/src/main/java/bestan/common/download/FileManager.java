package bestan.common.download;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import bestan.common.protobuf.Proto.FileBaseInfo;
import bestan.common.protobuf.Proto.FileInfo;

/**
 * @author yeyouhuan
 *
 */
public enum FileManager {
	INSTANCE;
	
	protected Map<String, FileInfo> allFiles;
	
	public static boolean isEqual(FileBaseInfo file1, FileBaseInfo file2) {
		return file1.getFileName().equals(file2.getFileName()) && 
				file1.getFileCode().equals(file2.getFileCode());
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
