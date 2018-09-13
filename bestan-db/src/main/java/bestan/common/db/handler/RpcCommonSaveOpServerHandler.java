package bestan.common.db.handler;

import java.util.List;

import org.rocksdb.Transaction;

import com.google.common.collect.Lists;
import com.google.protobuf.Message;
import com.google.protobuf.Message.Builder;

import bestan.common.db.IDBRpcServerHandler;
import bestan.common.db.Storage;
import bestan.common.db.StorageEnv;
import bestan.common.log.Glog;
import bestan.common.logic.FormatException;
import bestan.common.net.handler.NoteMessageHandler;
import bestan.common.protobuf.Proto.CommonSave;
import bestan.common.protobuf.Proto.RpcCommonSaveOp;
import bestan.common.protobuf.Proto.RpcCommonSaveOpRes;
import bestan.common.protobuf.Proto.RpcCommonSaveOpRes.RETCODE;

/**
 * @author yeyouhuan
 *
 */
@NoteMessageHandler(messageName = "RpcCommonSaveOpRes")
public class RpcCommonSaveOpServerHandler implements IDBRpcServerHandler{
	@Override
	public void handleServer(Transaction txn, Message arg, Builder res) throws Exception {
		var saveArg = (RpcCommonSaveOp)arg;
		var saveRes = (RpcCommonSaveOpRes.Builder)res;

		saveRes.setRetcode(RETCODE.FAILED);
		List<SaveOperation> ops = Lists.newArrayList();
		List<String> tables = Lists.newArrayList();
		for (var it : saveArg.getSaveOpsList()) {
			var tableName = it.getTableName().toStringUtf8();
			tables.add(tableName);
			var storage = StorageEnv.getStorage(tableName);
			if (null == storage) {
				throw new FormatException("cannot find storage={}", tableName);
			}
			ops.add(new SaveOperation(storage, it));
		}
		for (var op : ops) {
			op.storage.put(txn, op.key, op.value);
		}
		saveRes.setRetcode(RETCODE.SUCCESS);
		Glog.debug("RpcCommonSaveOpServerHandler:success:opType={},tables={}", saveArg.getOpType(), tables);
	}
	
	@Override
	public void exceptionCatch(Message arg, Builder res, Throwable e) {
		var saveArg = (RpcCommonSaveOp)arg;

		Glog.debug("RpcCommonSaveOpServerHandler:failed:opType={},exception={}", saveArg.getOpType(), e.getStackTrace());
	}
	
	static class SaveOperation {
		public Storage storage;
		public byte[] key;
		public byte[] value;
		
		SaveOperation(Storage storage, CommonSave op) {
			this.storage = storage;
			key = op.getKey().getData().toByteArray();
			value = op.getValue().getData().toByteArray();
		}
	}
}
