package bestan.common.net.operation;

import bestan.common.guid.Guid;
import bestan.common.logic.IObject;
import bestan.common.net.NetConst;

/**
 * @author yeyouhuan
 *
 */
public class CommonDBParam {
	private int opType;
	private Guid guid;
	private Object param;
	private int timeout = NetConst.RPC_TIMEOUT;
	
	public CommonDBParam(int opType, Guid guid) {
		this(opType, guid, null);
	}
	public CommonDBParam(int opType, IObject object) {
		this(opType, object.getGuid(), null);	
	}
	public CommonDBParam(int opType, Guid guid, Object param) {
		this.opType = opType;
		this.guid = guid;
		this.param = param;
	}
	public CommonDBParam(int opType, Guid guid, Object param, int timeout) {
		this.opType = opType;
		this.guid = guid;
		this.param = param;
		this.timeout = timeout;
	}
	public CommonDBParam(int opType, IObject object, Object param) {
		this.opType = opType;
		this.guid = object.getGuid();
		this.param = param;
	}
	public Guid getGuid() {
		return guid;
	}
	
	public int getOpType() {
		return opType;
	}
	
	public Object getParam() {
		return param;
	}
	
	public int getTimeout() {
		return timeout;
	}
}
