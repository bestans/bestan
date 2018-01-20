package bestan.common.hotdeploy;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.slf4j.Logger;

import bestan.log.GLog;

/**
 * 自定义类装载器
 * 如果不太了解请看 升入java虚拟机 第8章
 */
public class HotDeployClassLoader extends ClassLoader 
{
	private static final Logger logger = GLog.log;

	private String basedir; // 需要该类加载器直接加载的jar文件的基目录

	private Set<String> dynaclazns; // 需要由该类加载器直接加载的类名

	public HotDeployClassLoader(String basedir, String clazns) {
		super(null); // 指定父类加载器为 null
		this.basedir = basedir;
		dynaclazns = new HashSet<String>();
		loadClassByMe(clazns);
	}

	private void loadClassByMe(String clazns) {
		loadDirectly(clazns);
	}

	private Class<?> loadDirectly(String name) {
		Class<?> cls = null;
		StringBuffer sb = new StringBuffer(basedir);
		sb.append(File.separator + name);
		JarFile jarFile = null;
		try {
			if (!new File(sb.toString()).exists()) {
				return cls;
			}
			jarFile = new JarFile(sb.toString());
			JarEntry entry ;
			Enumeration<JarEntry> enums = jarFile.entries();
			while (enums.hasMoreElements()) {
				entry = enums.nextElement();
				if (entry.getName().endsWith(".class")){
					String className = entry.getName().replaceAll("/", ".");
					className = className.substring(0,className.lastIndexOf('.'));
					if(
						      (!className.startsWith("dotata.gameserver.messagehandler") && !className.startsWith("dotata.gameserver.db.")) 
							|| className.startsWith("dotata.gameserver.messagehandler.gm.CSGmCmdHandler")
							|| className.startsWith("dotata.gameserver.messagehandler.gm.WSGmCmdHandler")
							|| className.startsWith("dotata.gameserver.messagehandler.intensify.CSIntensifyEquipHandler")
							|| className.startsWith("dotata.gameserver.messagehandler.randomcard.CSRandomCardHandler")
							|| className.startsWith("dotata.gameserver.messagehandler.mine.CSMineOperationHandler")
							|| className.startsWith("dotata.gameserver.db.PlayerDAO")
					) {
						continue;
					}
					
					logger.debug("[1]will load:" + className);
					try {
						if(dynaclazns.contains(className)) {
							continue;
						}
						InputStream inputStream = jarFile.getInputStream(entry);
						cls = instantiateClass(className, inputStream, inputStream.available());
						dynaclazns.add(className);
						logger.debug("222222222222222222222222222222");
					} catch(Exception e) {
						logger.debug("reload failed:" + className);
					}
				}
			}
			jarFile.close();
		} catch (Exception e) {
			try{
				if(jarFile != null)
					jarFile.close();
			} catch (Exception e1){
			}
			logger.error("hotdeploy class error", e);
		}
		return cls;
	}

	private Class<?> instantiateClass(String name, InputStream fin, long len) {
		byte[] raw = new byte[(int) len];
		try {
			fin.read(raw);
			fin.close();
		} catch (IOException e) {
			logger.error("hotdeploy class error", e);
		}
		
		logger.debug("[3]instantiate class name:" + name + " len:" + len);
		return defineClass(name, raw, 0, raw.length);
	}

	@Override
	public synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {

		logger.debug("[3]load class name:" + name);
		
		Class<?> cls = null;
		cls = findLoadedClass(name);
		if(null != cls) {
			logger.debug("[3]find classname:" + name + " -> " + cls.getClassLoader());
		}
		
		if (!this.dynaclazns.contains(name) && cls == null) {
			cls = getSystemClassLoader().loadClass(name);
			if(name.startsWith("common") || name.startsWith("dotata") || name.startsWith("gm") || name.startsWith("net") || name.startsWith("test")) {
				dynaclazns.add(name);
			}
			logger.debug("[3]XXX name:" + name + " -> " + cls.getClassLoader());
		}
			
		if (cls == null) {
			throw new ClassNotFoundException(name);
		}
			
		if (resolve) {
			logger.debug("[3] resolve:true");
			resolveClass(cls);
		}
			
		return cls;
	}

    public boolean loadByMe(String className){
        return this.dynaclazns.contains(className);
    }
    
}