package bestan.common.logic;

import bestan.common.guid.Guid;

public interface IManager extends IObject {
	/**
	 * 给当前manager分配一个GUID
	 */
	default void initManger() {
		int uniqueId = ObjectManager.getInstance().incrementAndGetManagerIndex();
		Guid guid = Guid.newGUID(Gmatrix.getInstance().getZoneID(),
				Gmatrix.getInstance().getManagerObjectType(), uniqueId);
		setGuid(guid);
		ObjectManager.getInstance().putObject(this);
	}
	
	void setGuid(Guid guid);
}
