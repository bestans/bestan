package bestan.common.logic;

import bestan.common.lua.BaseLuaConfig;
import bestan.common.lua.LuaParamAnnotation;
import bestan.common.lua.LuaParamAnnotation.LuaParamPolicy;
import bestan.common.thread.BExecutor;

public class ServerConfig extends BaseLuaConfig{
	public int zoneId = -1;
	@LuaParamAnnotation(policy = LuaParamPolicy.OPTIONAL)
	public int managerType = 0;
	public long tickInterval = 1000;
	@LuaParamAnnotation(policy = LuaParamPolicy.OPTIONAL)
	public BExecutor workExecutor = null;
}
