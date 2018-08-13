package bestan.common.logic;

import bestan.common.lua.BaseLuaConfig;
import bestan.common.lua.LuaParamAnnotation;
import bestan.common.lua.LuaParamAnnotation.LuaParamPolicy;

public class ServerConfig extends BaseLuaConfig{
	public int zoneId = -1;
	@LuaParamAnnotation(policy = LuaParamPolicy.OPTIONAL)
	public int managerType = 0;
}
