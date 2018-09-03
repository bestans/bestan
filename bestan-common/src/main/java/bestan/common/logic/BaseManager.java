package bestan.common.logic;

import bestan.common.guid.Guid;
import bestan.common.guid.GuidFixedType;

public abstract class BaseManager extends BaseObject {
	public BaseManager() {
		super(null);
	}
	
	public void initManger() {
		int uniqueId = ObjectManager.getInstance().incrementAndGetManagerIndex();
		Guid guid = Guid.newGUID(Gmatrix.getInstance().getZoneID(),
				GuidFixedType.MANAGER.GetGUIDType(), uniqueId);
		this.guid = guid;
		ObjectManager.getInstance().putObject(this);
	}
}
