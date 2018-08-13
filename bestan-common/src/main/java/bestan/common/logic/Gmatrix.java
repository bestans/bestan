package bestan.common.logic;

public class Gmatrix {
	private int zoneId = -1;
	private int managerType = 0;
	
	private final static class GmatrixHolder {
		private static final Gmatrix INSTANCE = new Gmatrix();
	}
	public static final Gmatrix getInstance() {
		return GmatrixHolder.INSTANCE;
	}
	
	public boolean init(ServerConfig config) {
		zoneId = config.zoneId;
		managerType = config.managerType;
		return true;
	}
	
	public int getZoneID() {
		return zoneId;
	}
	
	public int getManagerObjectType() {
		return managerType;
	}
}
