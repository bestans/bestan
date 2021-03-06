package bestan.common.db;

import org.rocksdb.Transaction;

import bestan.common.log.Glog;
import bestan.common.net.AbstractProtocol;
import bestan.common.net.handler.IMessageHandler;

/**
 * @author yeyouhuan
 *
 */
public interface IDBMessageHandler extends IMessageHandler {
	@Override
	default void processProtocol(AbstractProtocol protocol) throws Exception {
		Transaction txn = null;
		try {
			txn = StorageEnv.start();
			handleProcess(txn, protocol);
			txn.commit();
		} catch (DBException e) {
			Glog.debug("IDBMessageHandler:{}:DBException:errcode={},message={}", getClass().getSimpleName(), e.getErrorCodeMessage(), e.getMessage());
			StorageEnv.rollback(txn);
		} catch (Exception e) {
			Glog.debug("IDBMessageHandler:{}:Exception:message={}", getClass().getSimpleName(), e.getMessage());
			StorageEnv.rollback(txn);
		} finally {
			StorageEnv.end();
		}
	}

	void handleProcess(Transaction txn, AbstractProtocol protocol);
}
