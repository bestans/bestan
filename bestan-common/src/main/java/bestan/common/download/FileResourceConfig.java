package bestan.common.download;

import bestan.common.lua.BaseLuaConfig;
import bestan.common.lua.LuaException;
import bestan.common.lua.LuaParamAnnotation;
import bestan.common.lua.LuaParamAnnotation.LuaParamPolicy;

/**
 * @author yeyouhuan
 *
 */
public class FileResourceConfig extends BaseLuaConfig {
	public String versionFile;
	public String resourceDir;
	
	/**
	 * 旧资源链接过期时间（秒）
	 */
	public int oldConnectionExpiredTime;

	/**
	 * 资源管理器tick间隔时间（毫秒）
	 */
	public int tickInterval;
	
	@LuaParamAnnotation(policy=LuaParamPolicy.OPTIONAL)
	public String versionFullPath;
	
	@Override
	public void afterLoad() throws LuaException {
		versionFullPath = resourceDir + versionFile;
	}
}
