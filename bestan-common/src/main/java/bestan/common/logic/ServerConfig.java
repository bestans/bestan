package bestan.common.logic;

import bestan.common.lua.BaseLuaConfig;
import bestan.common.lua.LuaAnnotation;
import bestan.common.thread.BExecutor;

/**
 * @author yeyouhuan
 *
 */
@LuaAnnotation(optional = true)
public class ServerConfig extends BaseLuaConfig{
	public int zoneId = -1;
	public long tickInterval = 1000;
	public int playerTickInterval = 100;
	public String messagePackage;
	public String messageHandlerPackage;

	/**以下是运行是设置的 **/
	
	/**
	 * 工作线程
	 */
	public BExecutor workExecutor;
	/**
	 * 消息索引
	 */
	@SuppressWarnings("rawtypes")
	public Class<? extends Enum> messageIndex;
}
