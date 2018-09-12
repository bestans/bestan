package bestan.common.net.operation;

import bestan.common.guid.Guid;
import bestan.common.logic.IObject;

/**
 * @author yeyouhuan
 *
 */
public class CommonSaveParam {
	public int opType;
	public Guid guid;
	
	public CommonSaveParam(int opType, Guid guid) {
		this.opType = opType;
		this.guid = guid;
	}
	public CommonSaveParam(int opType, IObject object) {
		this.opType = opType;
		this.guid = object.getGuid();
	}
	
	public Guid getGuid() {
		return guid;
	}
	
	public int getOpType() {
		return opType;
	}
}
