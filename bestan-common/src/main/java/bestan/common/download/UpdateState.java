package bestan.common.download;

import java.util.List;

import bestan.common.protobuf.Proto.FileInfo;

/**
 * @author yeyouhuan
 *
 */
public class UpdateState {
	public enum STATE {
		REQ,			//请求下载
		WAIT_PREPARE,	//等待客户端准备好下载
		IN_DOWNLOAD,	//正在下载
		FINISH,
	}
	
	private STATE state = STATE.REQ;
	private int version;
	private List<FileInfo> updateList = null;
	private FileResource resource = null;
	
	public UpdateState(int version) {
		this.version = version;
	}
	
	public STATE getState() {
		return state;
	}

	public void setState(STATE state) {
		this.state = state;
	}
	
	public int getVersion() {
		return version;
	}
	
	public void setResource(FileResource resource) {
		this.resource = resource;
	}
	
	public FileResource getResource() {
		return resource;
	}
	
	public void setUpdateList(List<FileInfo> updateList) {
		this.updateList = updateList;
	}
	
	public List<FileInfo> getUpdateList() {
		return updateList;
	}
}
