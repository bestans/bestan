package bestan.common.message.pack;

import bestan.common.event.IEvent;
import bestan.common.logic.IObject;
import bestan.common.logic.ObjectManager;
import bestan.common.net.operation.CommonSaveParam;
import bestan.common.protobuf.Proto.RpcCommonSaveOp;

/**
 * @author yeyouhuan
 *
 */
public class CommonSaveReplyPack implements IEvent {
	private RpcCommonSaveOp arg;
	private CommonSaveRetcode retcode;
	private CommonSaveParam param;
	
	public CommonSaveReplyPack(RpcCommonSaveOp arg, CommonSaveRetcode retcode, CommonSaveParam param) {
		this.arg = arg;
		this.retcode = retcode;
		this.param = param;
	}

	@Override
	public void run() {
		if (param.object != null) {
			onExecute(param.object);
			return;
		}

		var obj = ObjectManager.getInstance().getObject(param.guid);
		if (obj != null) {
			onExecute(obj);
		}
	}
	
	private void onExecute(IObject obj) {
		obj.lockObject();
		try {
			obj.commonSaveReply(arg, param.opType, retcode);
		} finally {
			obj.unlockObject();
		}
	}
}
