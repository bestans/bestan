package bestan.common.download;

import bestan.common.lua.BaseLuaConfig;
import bestan.common.lua.LuaException;

/**
 * @author yeyouhuan
 *
 */
public class FileResourceConfig extends BaseLuaConfig {
	public String versionFilePath;
	public String resourceDir;
	public String updateDir;
	/**
	 * 资源过期时间（秒）
	 */
	public int resourceExpiredTime;
	/**
	 * 资源更新最小变化时间（秒）
	 */
	public int updateChangeMinInterval;
	
	public String updatePath;
	
	@Override
	public void afterLoad() throws LuaException {
		updatePath = versionFilePath + updateDir;
	}
}
