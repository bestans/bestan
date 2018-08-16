package bestan.common.net.server;

import com.google.protobuf.Message;

import bestan.common.lua.BaseLuaConfig;
import bestan.common.lua.LuaAnnotation;
import bestan.common.lua.LuaParamAnnotation;
import bestan.common.lua.LuaParamAnnotation.LuaParamPolicy;
import bestan.common.thread.BExecutor;

/**
 * @author yeyouhuan
 *
 */
@LuaAnnotation(load = false)
public class NetServerConfig extends BaseLuaConfig {
	public String serverName;
	public int bossGroupThreadCount = 1;
	public int workerGroupThreadCount = 1;
	public String serverIP;
	public int serverPort;
	/**
	 * serverchanel(用来监听和接受连接)的接收缓冲区大小
	 */
	public int optionRcvbuf;
	/**
	 * serverchanel(用来监听和接受连接)的发送缓冲区大小
	 */
	public int optionSndbuf;
	/**
	 * client channel（每一个建立连接）的接收缓冲区大小
	 */
	public int childOptionRcvbuf;
	/**
	 * client channel（每一个建立连接）的发送缓冲区大小
	 */
	public int childOptionSndbuf;

	@LuaParamAnnotation(policy=LuaParamPolicy.OPTIONAL)
	public BExecutor workdExecutor;
	@LuaParamAnnotation(policy=LuaParamPolicy.OPTIONAL)
	public Message baseMessage;
}
