package bestan.common.download;

import java.io.File;
import java.io.IOException;

import bestan.common.log.Glog;
import bestan.common.logic.BaseManager;
import bestan.common.module.IModule;
import bestan.common.net.server.BaseNetServerManager;
import bestan.common.protobuf.Proto.FileBaseInfo;
import bestan.common.protobuf.Proto.FileInfo;
import bestan.common.timer.BTimer;
import bestan.common.util.ExceptionUtil;

/**
 * @author yeyouhuan
 *
 */
public class FileManager extends BaseManager {
	private static class FileManagerHolder {
		private static FileManager INSTANCE = new FileManager();
	}
	
	public static FileManager getInstance() {
		return FileManagerHolder.INSTANCE;
	}
	
	private FileResource curResource;
	private long lastChangeTime = 0;
	private int version = 0;

	protected FileInfo versionFileInfo;
	protected FileResourceConfig config;
	private BaseNetServerManager netServerManager;
	
	public void startUp(FileResourceConfig config, BaseNetServerManager netServerManager) {
		this.config = config;
		this.netServerManager = netServerManager;

		BTimer.attach(this, config.tickInterval);
		loadFiles();
	}

	@Override
	public void Tick() {
		checkExpiredResource();
	}
	
	private String getResourcePath() {
		return config.resourceDir;
	}
	
	private void checkExpiredResource() {
		if (lastChangeTime == 0) {
			return;
		}
		var curTime = BTimer.getTime();
		if (curTime - lastChangeTime <= config.oldConnectionExpiredTime) {
			//尚未过期
			return;
		}
		lastChangeTime = 0;
		//关闭所有非当前资源的连接（假如不关闭，那么）
		netServerManager.closeChannels(new CloseChannelChecker(version));
	}
	
	//载入资源到内存
	private void loadFiles() {
		lockObject();
		try {
			//记录变化时间
			lastChangeTime = BTimer.getTime();
			++version;
			curResource.loadResource(getResourcePath(), version);
		} catch (Exception e) {
			Glog.debug("loadFiles failed:error={}", ExceptionUtil.getLog(e));
		} finally {
			unlockObject();
		}
	}
	
	public void checkLoad() throws IOException {
		checkExpiredResource();
		loadFiles();
	}

	private static void traverseFolder(File file, String partName, FileResource resource) throws IOException {
        if (file == null || !file.exists()) {
        	return;
        }

        partName += "/" + file.getName();
        if (!file.isDirectory()) {
        	resource.addFile(file, partName);
        	return;
        }
        for (var it : file.listFiles()) {
        	traverseFolder(it, partName, resource);
        }
    }
	public static void traverseFolder(String filePath, FileResource resource) throws IOException
	{
		traverseFolder(new File(filePath), ".", resource);
	}
	public static boolean isEqual(FileBaseInfo file1, FileBaseInfo file2) {
		return file1.getFileName().equals(file2.getFileName()) && 
				file1.getLastModified() == file2.getLastModified();
	}
	
	public FileResource getResource() {
		return curResource;
	}

	public static class FileManagerModule implements IModule {
		private FileResourceConfig config;
		private BaseNetServerManager netServerManager;
		
		public FileManagerModule(FileResourceConfig config, BaseNetServerManager netServerManager) {
			this.config = config;
			this.netServerManager = netServerManager;
		}

		@Override
		public void startup() throws Exception {
			FileManager.getInstance().startUp(config, netServerManager);
		}
	}
}
