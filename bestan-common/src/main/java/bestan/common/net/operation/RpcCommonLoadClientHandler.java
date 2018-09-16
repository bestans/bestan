package bestan.common.net.operation;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.protobuf.Message;

import bestan.common.log.Glog;
import bestan.common.logic.ObjectManager;
import bestan.common.net.AbstractProtocol;
import bestan.common.net.RpcManager.RpcObject;
import bestan.common.net.handler.IRpcClientHandler;
import bestan.common.protobuf.Proto.COMMON_DB_RETCODE;
import bestan.common.protobuf.Proto.RpcCommonLoadOp;
import bestan.common.protobuf.Proto.RpcCommonLoadOpRes;

/**
 * @author yeyouhuan
 *
 */
public class RpcCommonLoadClientHandler implements IRpcClientHandler {

	@Override
	public void client(AbstractProtocol protocol, Message arg, Message res, Object param) {
		// TODO Auto-generated method stub
		var loadArg = (RpcCommonLoadOp)arg;
		var loadRes = (RpcCommonLoadOpRes)res;
		var loadParam = (CommonLoadParam)param;
		var object = ObjectManager.getInstance().getObject(loadParam.getGuid());
		if (object == null) {
			Glog.debug("RpcCommonLoadOpClientHandler:client:cannot find object:op={},guid={}", loadParam.getOpType(), loadParam.getGuid());
			return;
		}
		List<Object> values = Lists.newArrayList();
		COMMON_DB_RETCODE retcode = COMMON_DB_RETCODE.SUCCESS;
		for (var it : loadRes.getValuesList()) {
			if (!it.getValid()) {
				values.add(null);
				continue;
			}
			var tempValue = TableDataType.convertObject(it.getValue());
			if (null == tempValue) {
				retcode = COMMON_DB_RETCODE.DATA_EXCEPTION;
				break;
			}
			values.add(tempValue);
		}
		if (values.size() != loadArg.getLoadOpsCount() || values.size() <= 0) {
			retcode = COMMON_DB_RETCODE.DATA_SIZE_EXCEPTION;
		}
		object.lockObject();
		try {
			if (retcode == COMMON_DB_RETCODE.SUCCESS) {
				object.rpcCommonLoadSuccess(loadArg, values.get(0), values, loadParam.getOpType());	
			} else {
				object.rpcCommonLoadFailed(loadArg, loadParam.getOpType(), retcode);
			}
		} finally {
			object.unlockObject();
		}
	}

	@Override
	public void OnTimeout(RpcObject rpc, Message arg, Object param) {
		var loadArg = (RpcCommonLoadOp)arg;
		var loadParam = (CommonLoadParam)param;

		var object = ObjectManager.getInstance().getObject(loadParam.getGuid());
		if (object == null) {
			Glog.debug("RpcCommSaveClientHandler:timeout:cannot find object:op={},guid={}", loadParam.getOpType(), loadParam.getGuid());
			return;
		}
		object.lockObject();
		try {
			object.rpcCommonLoadFailed(loadArg, loadParam.getOpType(), COMMON_DB_RETCODE.TIMEOUT);
		} finally {
			object.unlockObject();
		}
	}
}
