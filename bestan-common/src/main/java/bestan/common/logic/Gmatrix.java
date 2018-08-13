package bestan.common.logic;


public class Gmatrix {
	
	private final static class GmatrixHolder {
		private static final Gmatrix INSTANCE = new Gmatrix();
	}
	public static final Gmatrix getInstance() {
		return GmatrixHolder.INSTANCE;
	}
	
	int getZoneID() {
		return 0;
	}
	
	int getManagerObjectType() {
		return 0;
	}
}
