package bestan.common.module;

public interface IModule {
	/**
	 * 启动模块
	 */
	void startup() throws Exception;

	/**
	 * 关闭模块
	 */
	default void close() throws Exception { }
}
