package bestan.common.db.handler;

import java.util.List;

import org.rocksdb.Transaction;

import com.google.common.collect.Lists;
import com.google.protobuf.Message;
import com.google.protobuf.Message.Builder;

import bestan.common.db.IDBRpcServerHandler;
import bestan.common.db.Storage;
import bestan.common.db.StorageEnv;
import bestan.common.logic.FormatException;
import bestan.common.net.handler.NoteMessageHandler;
import bestan.common.protobuf.Proto.COMMON_DB_RETCODE;
import bestan.common.protobuf.Proto.CommonLoad;
import bestan.common.protobuf.Proto.CommonLoadRes;
import bestan.common.protobuf.Proto.RpcCommonLoadOp;
import bestan.common.protobuf.Proto.RpcCommonLoadOpRes;

/**
 * @author yeyouhuan
 *
 */
@NoteMessageHandler(messageName = "RpcCommonLoadOp")
public class RpcCommonLoadOpServerHandler implements IDBRpcServerHandler {
	@Override
	public void handleServer(Transaction txn, Message arg, Builder res) throws Exception {
		var loadArg = (RpcCommonLoadOp)arg;
		var loadRes = (RpcCommonLoadOpRes.Builder)res;
		
		List<String> tables = Lists.newArrayList();
		List<LoadOperation> ops = Lists.newArrayList();
		for (var it : loadArg.getLoadOpsList()) {
			var tableName = it.getTableName().toStringUtf8();
			tables.add(tableName);
			var storage = StorageEnv.getStorage(tableName);
			if (null == storage) {
				throw new FormatException("cannot find table={}", tableName);
			}
			ops.add(new LoadOperation(storage, it));
		}
		for (var op : ops) {
			var valueBuilder = CommonLoadRes.newBuilder();
			var storage = op.storage;
			var value = storage.get(txn, storage.getKeyObject(op.key));
			if (value != null) {
				valueBuilder.setValid(true);
				valueBuilder.setValue(storage.getValueProcess().convertPB(value));
			}
			loadRes.addValues(valueBuilder);
		}

		loadRes.setRetcode(COMMON_DB_RETCODE.SUCCESS);
	}

	static class LoadOperation {
		public Storage storage;
		public byte[] key;
		
		LoadOperation(Storage storage, CommonLoad op) {
			this.storage = storage;
			key = op.getKey().getData().toByteArray();
		}
	}
}
