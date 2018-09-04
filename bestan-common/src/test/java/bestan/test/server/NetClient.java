package bestan.test.server;

import bestan.common.net.client.BaseNetClientManager;
import bestan.common.net.client.NetClientConfig;

/**
 * @author yeyouhuan
 *
 */
public class NetClient extends BaseNetClientManager {
	public NetClient(NetClientConfig config) {
		super(config, config.workdExecutor, config.baseProtocol);
	}
}
