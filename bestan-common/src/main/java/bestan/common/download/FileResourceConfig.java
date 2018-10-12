package bestan.common.download;

import bestan.common.lua.BaseLuaConfig;

/**
 * @author yeyouhuan
 *
 */
public class FileResourceConfig extends BaseLuaConfig {
	public String versionFile;
	public String resourceDir;
	
	/**
	 * 资源过期时间（秒）
	 */
	public int resourceExpiredTime;

	/**
	 * 资源更新最小变化时间（秒）
	 */
	public int updateChangeMinInterval;
}
