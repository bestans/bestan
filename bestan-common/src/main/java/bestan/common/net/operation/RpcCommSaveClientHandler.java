package bestan.common.net.operation;

import com.google.protobuf.Message;

import bestan.common.message.IRpcClientHandler;
import bestan.common.net.AbstractProtocol;
import bestan.common.protobuf.Proto.RpcCommonSaveOp;
import bestan.common.protobuf.Proto.RpcCommonSaveOpRes;

/**
 * @author yeyouhuan
 *
 */
public class RpcCommSaveClientHandler implements IRpcClientHandler {

	@Override
	public void client(AbstractProtocol protocol, Message arg, Message res, Object param) {
		var saveArg = (RpcCommonSaveOp)arg;
		var saveRes = (RpcCommonSaveOpRes)res;
		var saveParam = (CommonSaveParam)param;
		
	}
}
