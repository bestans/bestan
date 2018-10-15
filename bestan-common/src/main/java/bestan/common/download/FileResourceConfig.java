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
	 * 旧资源链接过期时间（秒）
	 */
	public int oldConnectionExpiredTime;

	/**
	 * 资源管理器tick间隔时间（毫秒）
	 */
	public int tickInterval;
}
