package bestan.common.module;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 模块管理器，管理所有模块的启动和关闭
 * @author yeyouhuan
 * @date:   2018年8月3日 下午3:58:32 
 */
public class ModuleManager {
	private static List<IModule> modules = new ArrayList<>();
	private static Class<? extends IModule>[] closeOrders = null;
	private static Map<Class<? extends IModule>, IModule> moduleMap;
	
	/**
	 * 注册一个模块，另外startup将会按照注册顺序，依次启动模块
	 * @param module 注册的模块
	 */
	public static void register(IModule module) {
		modules.add(module);
		moduleMap.put(module.getClass(), module);
	}
	
	/**
	 * 注册多个模块，另外startup将会按照注册顺序，依次启动模块
	 * @param modules 多个模块
	 */
	public static void register(IModule[] modules) {
		for (var module : modules) {
			register(module);
		}
	}
	
	/**
	 * 注册模块关闭顺序
	 * @param moduleClassArr 模块关闭顺序
	 */
	public static void registeCloseOrder(Class<? extends IModule>[] moduleClassArr) {
		closeOrders = moduleClassArr;
	}
	
	/**
	 * 按照注册顺序依次启动各个模块
	 */
	public static void startup() {
		for (var module : modules) {
			module.startup();
		}
	}
	
	/**
	 * 关闭模块，如果指定了关闭顺序，按照指定顺序关闭；
	 * 否则按照启动倒序进行
	 */
	public static void close() {
		if (closeOrders == null) {
			//如果没有指定关闭顺序，那么按照启动倒序来关闭
			var it = modules.listIterator(modules.size());
			while (it.hasPrevious()) {
				it.previous().close();
			}
			return;
		}
		//按照指定的关闭顺序，逐一关闭各个模块
		for (var cls : closeOrders) {
			moduleMap.get(cls).close();
		}
	}
}
