package bestan.common.logic;

import bestan.common.guid.Guid;

public abstract class BaseManager extends BaseObject {
	public BaseManager() {
		super(null);
	}
	
	public void initManger() {
		int uniqueId = ObjectManager.getInstance().incrementAndGetManagerIndex();
		Guid guid = Guid.newGUID(Gmatrix.getInstance().getZoneID(),
				Gmatrix.getInstance().getManagerObjectType(), uniqueId);
		this.guid = guid;
		ObjectManager.getInstance().putObject(this);
	}
}
