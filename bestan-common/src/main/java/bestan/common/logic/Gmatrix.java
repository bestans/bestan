package bestan.common.logic;

import bestan.common.module.IModule;

public class Gmatrix {
	private ServerConfig config;
	
	private final static class GmatrixHolder {
		private static final Gmatrix INSTANCE = new Gmatrix();
	}
	public static final Gmatrix getInstance() {
		return GmatrixHolder.INSTANCE;
	}
	
	public ServerConfig getServerConfig() {
		return config;
	}
	
	public void setServerConfig(ServerConfig config) {
		this.config = config;
	}
	
	public int getZoneID() {
		return config.zoneId;
	}
	
	public static class GmatrixModule implements IModule {
		public GmatrixModule(ServerConfig config) {
			Gmatrix.getInstance().setServerConfig(config);
		}
		@Override
		public void startup() throws Exception {
		}
		
	}
}
