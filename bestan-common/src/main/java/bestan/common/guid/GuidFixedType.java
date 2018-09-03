package bestan.common.guid;

/**
 * guid固定使用的类型，从-1开始编号
 * @author yeyouhuan
 *
 */
public enum GuidFixedType {
	INVALID,
	MANAGER;	//管理器
	
	private int guidType;
	
	public int GetGUIDType() {
		return guidType;
	}
	
	GuidFixedType() {
		guidType = this.ordinal() * -1;
	}
}
