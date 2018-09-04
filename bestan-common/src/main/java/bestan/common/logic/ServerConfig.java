package bestan.common.logic;

import bestan.common.lua.BaseLuaConfig;
import bestan.common.lua.LuaAnnotation;

/**
 * @author yeyouhuan
 *
 */
@LuaAnnotation(optional = true)
public final class ServerConfig extends BaseLuaConfig{
	public int zoneId = -1;
	public int playerTickInterval = 100;
}
