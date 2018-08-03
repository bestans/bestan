package bestan.common.module;

public interface IModule {
	/**
	 * 启动模块
	 */
	default void startup() {}

	/**
	 * 关闭模块
	 */
	default void close() { }
}
