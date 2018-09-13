package bestan.common.module;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import bestan.common.log.Glog;
import bestan.common.logic.ServerConfig;

/**
 * 模块管理器，管理所有模块的启动和关闭
 * @author yeyouhuan
 * @date:   2018年8月3日 下午3:58:32 
 */
public class ModuleManager {
	private static List<IModule> modules = null;
	private static List<IModule> closeModules = null;
	private static Map<IModule, Boolean> openMap = Maps.newHashMap();

	/**
	 * 注册多个模块，另外startup将会按照注册顺序，依次启动模块
	 * @param modules 多个模块
	 */
	public static void register(IModule[] startupModulesArg, IModule[] closeModulesArg) {
		modules = Lists.newArrayList(startupModulesArg);
		closeModules = Lists.newArrayList(closeModulesArg);
	}
	
	/**
	 * 按照注册顺序依次启动各个模块
	 */
	public static boolean startup(ServerConfig config) {
		if (modules == null)
			return false;
		
		for (var module : modules) {
			try {
				Glog.debug("begin startup module({})", module.getClass().getSimpleName());
				module.startup();
				openMap.put(module, true);
				Glog.debug("finish startup module({})", module.getClass().getSimpleName());
			} catch (Exception e) {
				Glog.error("startup failed:module={},error_clas={},error={}",
						module.getClass().getSimpleName(), e.getClass().getSimpleName(), e.getMessage());
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 关闭模块，如果指定了关闭顺序，按照指定顺序关闭；
	 * 否则按照启动倒序进行
	 */
	public static void close() {
		if (closeModules == null) return;
		
		//按照指定的关闭顺序，逐一关闭各个模块
		for (var module : closeModules) {
			var isOpen = openMap.get(module);
			if (isOpen == null || !isOpen) {
				//没有开过
				continue;
			}
			try {
				Glog.debug("start close module({})", module.getClass().getSimpleName());
				module.close();
				Glog.debug("finish close module({})", module.getClass().getSimpleName());
			} catch (Exception e) {
				Glog.error("close module failed:module={},error_clas={},error={}",
						module.getClass().getSimpleName(), e.getClass().getSimpleName(), e.getMessage());
			}
		}

		Glog.debug("finish close all modules");
	}
}
