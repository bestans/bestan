package bestan.common.net.client;

import bestan.common.lua.BaseLuaConfig;
import bestan.common.lua.LuaAnnotation;
import bestan.common.lua.LuaParamAnnotation;
import bestan.common.lua.LuaParamAnnotation.LuaParamPolicy;
import bestan.common.net.IProtocol;
import bestan.common.thread.BExecutor;

/**
 * @author yeyouhuan
 *
 */
@LuaAnnotation(load = false)
public class NetClientConfig extends BaseLuaConfig {
	public String clientName;
	public int bossGroupThreadCount = 1;
	public int workerGroupThreadCount = 1;
	public String serverIP;
	public int serverPort;
	@LuaParamAnnotation(policy=LuaParamPolicy.OPTIONAL)
	public IProtocol baseProtocol;
	@LuaParamAnnotation(policy=LuaParamPolicy.OPTIONAL)
	public BExecutor workdExecutor;
}
