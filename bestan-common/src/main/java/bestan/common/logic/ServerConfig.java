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
	public BExecutor workExecutor = null;
	public int managerType = 0;
	public int playerTickInterval = 100;
	public String messagePackage;
	public String messageHandlerPackage;
}
