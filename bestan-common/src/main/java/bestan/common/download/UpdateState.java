package bestan.common.download;

import java.util.List;

import bestan.common.util.PairData;

/**
 * @author yeyouhuan
 *
 */
public class UpdateState {
	public enum STATE {
		REQ,			//请求下载
		WAIT_PREPARE,	//等待客户端准备好下载
		IN_DOWNLOAD,	//正在下载
		WAIT_FINISH,	//等待客户端下载完成
		FINISH,
	}
	
	private STATE state = STATE.REQ;
	private PairData<List<FileResourceUnit>, Integer> updateList;
	
	public UpdateState(PairData<List<FileResourceUnit>, Integer> updateList) {
		this.updateList = updateList;
	}
	
	public STATE getState() {
		return state;
	}

	public void setState(STATE state) {
		this.state = state;
	}
	
	public int getVersion() {
		return updateList.second;
	}
	
	public List<FileResourceUnit> getUpdateList() {
		return updateList.first;
	}
}
