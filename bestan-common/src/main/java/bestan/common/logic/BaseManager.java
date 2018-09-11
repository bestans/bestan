package bestan.common.logic;

import bestan.common.guid.Guid;
import bestan.common.guid.GuidFixedType;

public abstract class BaseManager extends BaseObject {
	public BaseManager() {
		super(MakeGuid());
	}
	
	private static Guid MakeGuid() {
		int uniqueId = ObjectManager.getInstance().incrementAndGetManagerIndex();
		Guid guid = Guid.newGUID(Gmatrix.getInstance().getZoneID(),
				GuidFixedType.MANAGER.GetGUIDType(), uniqueId);
		return guid;
	}
	
	@Override
	public OBJECT_TYPE getObjectType() {
		return OBJECT_TYPE.MANAGER;
	}
}
