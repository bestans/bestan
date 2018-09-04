package bestan.test.server;

import bestan.common.net.server.BaseNetServerManager;
import bestan.common.net.server.NetServerConfig;

/**
 * @author yeyouhuan
 *
 */
public class NetServer extends BaseNetServerManager {
	public NetServer(NetServerConfig config) {
		super(config, config.workdExecutor, config.baseProtocol);
	}
}
