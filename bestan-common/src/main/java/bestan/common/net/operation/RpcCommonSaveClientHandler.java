package bestan.common.net.operation;

import com.google.protobuf.Message;

import bestan.common.log.Glog;
import bestan.common.logic.ObjectManager;
import bestan.common.net.AbstractProtocol;
import bestan.common.net.RpcManager.RpcObject;
import bestan.common.net.handler.IRpcClientHandler;
import bestan.common.protobuf.Proto.COMMON_DB_RETCODE;
import bestan.common.protobuf.Proto.RpcCommonSaveOp;
import bestan.common.protobuf.Proto.RpcCommonSaveOpRes;

/**
 * @author yeyouhuan
 *
 */
public class RpcCommonSaveClientHandler implements IRpcClientHandler {
	@Override
	public void client(AbstractProtocol protocol, Message arg, Message res, Object param) {
		var saveArg = (RpcCommonSaveOp)arg;
		var saveRes = (RpcCommonSaveOpRes)res;
		var saveParam = (CommonSaveParam)param;
		
		var object = ObjectManager.getInstance().getObject(saveParam.getGuid());
		if (object == null) {
			Glog.debug("RpcCommSaveClientHandler:client:cannot find object:op={},guid={}", saveParam.getOpType(), saveParam.getGuid());
			return;
		}
		object.lockObject();
		try {
			if (saveRes.getRetcode() == COMMON_DB_RETCODE.SUCCESS) {
				object.rpcCommonSaveSuccess(saveArg, saveRes, saveParam.getOpType());
			} else {
				object.rpcCommonSaveFailed(saveArg, saveParam.getOpType(), saveRes.getRetcode());
			}
		} finally {
			object.unlockObject();
		}
	}
	
	@Override
	public void OnTimeout(RpcObject rpc, Message arg, Object param) {
		var saveArg = (RpcCommonSaveOp)arg;
		var saveParam = (CommonSaveParam)param;

		var object = ObjectManager.getInstance().getObject(saveParam.getGuid());
		if (object == null) {
			Glog.debug("RpcCommSaveClientHandler:timeout:cannot find object:op={},guid={}", saveParam.getOpType(), saveParam.getGuid());
			return;
		}
		object.lockObject();
		try {
			object.rpcCommonSaveFailed(saveArg, saveParam.getOpType(), COMMON_DB_RETCODE.TIMEOUT);
		} finally {
			object.unlockObject();
		}
	}
}
