package bestan.common.db;

import org.rocksdb.Transaction;

import com.google.protobuf.Message;
import com.google.protobuf.Message.Builder;

import bestan.common.log.Glog;
import bestan.common.message.IRpcServerHandler;
import bestan.common.net.AbstractProtocol;

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
			handleServer(txn, protocol, arg, res);
			txn.commit();
		} catch (DBException e) {
			Glog.debug("IDBRpcServerHandler:{}:DBException:errcode={},message={}", getClass().getSimpleName(), e.getErrorCodeMessage(), e.getMessage());
			StorageEnv.rollback(txn);
		} catch (Exception e) {
			Glog.debug("IDBRpcServerHandler:{}:Exception:message={}", getClass().getSimpleName(), e.getMessage());
			StorageEnv.rollback(txn);
		} finally {
			StorageEnv.end();
		}
	}
	
	void handleServer(Transaction txn, AbstractProtocol protocol, Message arg, Builder res);
}
