package bestan.common.net.client;

import bestan.common.lua.LuaAnnotation;

/**
 * @author yeyouhuan
 *
 */
@LuaAnnotation(load = false)
public class NetClientConfig {
	public String clientName;
	public int bossGroupThreadCount = 1;
	public int workerGroupThreadCount = 1;
	public String serverIP;
	public int serverPort;
}
