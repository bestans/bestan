package bestan.common.db;

import org.rocksdb.Transaction;

import com.google.protobuf.Message;
import com.google.protobuf.Message.Builder;

import bestan.common.log.Glog;
import bestan.common.net.AbstractProtocol;
import bestan.common.net.handler.IRpcServerHandler;
import bestan.common.util.ExceptionUtil;

/**
 * @author yeyouhuan
 *
 */
public interface IDBRpcServerHandler extends IRpcServerHandler {
	@Override
	default void server(AbstractProtocol protocol, Message arg, Builder res) {
		Transaction txn = null;
		try {
			txn = StorageEnv.start();
			handleServer(txn, arg, res);
			txn.commit();
		} catch (Exception e) {
			StorageEnv.rollback(txn);
			exceptionCatch(arg, res, e);
		} finally {
			StorageEnv.end();
		}
	}
	
	void handleServer(Transaction txn, Message arg, Builder res) throws Exception;
	
	default void exceptionCatch(Message arg, Builder res, Throwable e) {
		Glog.debug("IDBRpcServerHandler:{}:Exception:message={},exception={},trace={}",
				getClass().getSimpleName(), e.getMessage().getClass().getSimpleName(), ExceptionUtil.getLog(e));
	}
}
