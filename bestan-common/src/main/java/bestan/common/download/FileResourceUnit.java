package bestan.common.download;

import bestan.common.protobuf.Proto.FileInfo;

/**
 * @author yeyouhuan
 *
 */
public class FileResourceUnit {
	private FileInfo fileInfo;
	private byte[] fileData;
	
	public FileResourceUnit(FileInfo fileInfo, byte[] fileData) {
		this.fileInfo = fileInfo;
		this.fileData = fileData;
	}
	
	public byte[] getFileData() {
		return fileData;
	}
	
	public FileInfo getFileInfo() {
		return fileInfo;
	}
	
	public long getLastModified() {
		return fileInfo.getBaseInfo().getLastModified();
	}
}
