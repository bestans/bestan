package bestan.common.logic;

public class Gmatrix {
	private ServerConfig config;
	private int zoneId = -1;
	
	private final static class GmatrixHolder {
		private static final Gmatrix INSTANCE = new Gmatrix();
	}
	public static final Gmatrix getInstance() {
		return GmatrixHolder.INSTANCE;
	}
	
	public boolean init(ServerConfig config) {
		zoneId = config.zoneId;
		return true;
	}
	
	public ServerConfig getServerConfig() {
		return config;
	}
	
	public int getZoneID() {
		return config.zoneId;
	}
}
