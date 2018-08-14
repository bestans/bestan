package bestan.common.module;

import bestan.common.logic.ServerConfig;

public interface IModule {
	/**
	 * 启动模块
	 */
	void startup(ServerConfig config);

	/**
	 * 关闭模块
	 */
	default void close() { }
}
