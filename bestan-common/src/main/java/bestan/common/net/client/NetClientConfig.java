package bestan.common.net.client;

import com.google.protobuf.Message;

import bestan.common.lua.LuaAnnotation;
import bestan.common.lua.LuaParamAnnotation;
import bestan.common.lua.LuaParamAnnotation.LuaParamPolicy;
import bestan.common.thread.BExecutor;

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
	@LuaParamAnnotation(policy=LuaParamPolicy.OPTIONAL)
	public Message baseMessage;
	@LuaParamAnnotation(policy=LuaParamPolicy.OPTIONAL)
	public BExecutor workdExecutor;
}
