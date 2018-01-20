package bestan.common.hotdeploy;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;

import bestan.common.db.command.DBCommandWithCallback;
import bestan.common.net.message.AbstractMessageHandler;
import bestan.common.reload.IHotdeploy;
import bestan.common.reload.ReloadResult;
import bestan.common.tab.CommonTableDB;
import bestan.common.tab.TableHotDeployConfig;
import bestan.common.util.Global;
import bestan.log.GLog;

public class HotDeployReload implements IHotdeploy {
	private static final Logger logger = GLog.log;
	private static boolean hasNewProcedureClass = false;
	private static boolean hasNewProtocolClass = false;
	private static Map<String, Class<?>> classes = new HashMap<String, Class<?>>();
	private static HotDeployReload instance;
	private CommonTableDB tableDB;
	
	private String hotDeployJar; 
	
	protected HotDeployReload() {
		hotDeployJar = Global.RELOAD_GS_JAR;
	}
	
	public static HotDeployReload getInstance() {
		if(null == instance) {
			instance = new HotDeployReload();
		}
		
		return instance;
	}
    
	public ReloadResult loadClassFromCache() throws ClassNotFoundException {
		if(null == tableDB.hotDeployConfigTable) {
			return new ReloadResult(false, "param TableHotDeployConfig is nil");
		}
		
    	classes.clear();

        String jarfilename = this.hotDeployJar;
        File basedir = new File("");
        
        HotDeployClassLoader clzloader = null;
		try {
			// check if exist jar
			StringBuffer sb = new StringBuffer(basedir.getCanonicalPath());
			sb.append(File.separator + jarfilename);
			if (!new File(sb.toString()).exists()) {
				return new ReloadResult(false, "no " + jarfilename + " in path:" + basedir.getCanonicalPath());
			}
			
			// clazz loader
			clzloader = new HotDeployClassLoader(basedir.getCanonicalPath(), jarfilename);
		} catch (Exception e) {
			e.printStackTrace();
			return new ReloadResult(false, e.getMessage());
		}

		for(int i = 0; i < tableDB.hotDeployConfigTable.rowCount(); i++) {
			TableHotDeployConfig config = tableDB.hotDeployConfigTable.getRowByIndex(i);
			if(null == config || TableHotDeployConfig.HOTUPDATE_TYPE_CLAZZ != config.type) {
				continue;
			}
			
			if (clzloader.loadByMe(config.content)) {
            	Class<?> newClass = clzloader.loadClass(config.content);
            	if (DBCommandWithCallback.class.isAssignableFrom(newClass)) {
            		hasNewProcedureClass = true;
            	} else if (AbstractMessageHandler.class.isAssignableFrom(newClass)) {
            		hasNewProtocolClass = true;
            	}
            		
                classes.put(config.content,  newClass);
            } else {
                return new ReloadResult(false, "no " + config.content + " in the jar");
            }
		}
		
		return new ReloadResult(true);
    }
	
	public boolean init(CommonTableDB tableDB, String hotDeployJar) {
		try {
			if(null == tableDB || null == tableDB.hotDeployConfigTable || null == hotDeployJar || hotDeployJar.isEmpty()) {
				return false;
			}
			this.tableDB = tableDB;
			this.hotDeployJar = hotDeployJar;
			loadClassFromCache();
			return true;
		} catch (ClassNotFoundException e) {
			logger.error("init failed");
			return false;
		}
	}
	
	public ReloadResult reload() throws Exception {
		ReloadResult result = null;
	    try {
			result = loadClassFromCache();
		} catch (ClassNotFoundException e) {
			logger.error("reload failed");
		}
		 	
		return result;
	}
	
//	public static <T> T getHotdeployDAO(Class<T> clazz, String className) throws Exception {
//		if(null == className) {
//    		return null;
//    	}
//		@SuppressWarnings("unchecked")
//		Class<?> newDAOClass = (Class<?>) classes.get(className);
//        if(newDAOClass != null){
//            Constructor<?> constructor = newDAOClass.getConstructor();
//            return (T) constructor.newInstance();
//        }
//        return null;
//    }
    
    public static AbstractMessageHandler getHotdeployProtocol(String className) throws Exception{
    	if(null == className) {
    		return null;
    	}
    	@SuppressWarnings("unchecked")
		Class<? extends AbstractMessageHandler> newProtocolClass = (Class<? extends AbstractMessageHandler>) classes.get(className);
    	if(newProtocolClass != null){
    		Constructor<? extends AbstractMessageHandler> constructor = newProtocolClass.getConstructor();
    		return constructor.newInstance();
    	}
    	return null;
    }
    
    public static boolean hasNewDAOClass(){
    	return hasNewProcedureClass;
    }
    public static boolean hasNewProtocolClass(){
    	return hasNewProtocolClass;
    }
}
