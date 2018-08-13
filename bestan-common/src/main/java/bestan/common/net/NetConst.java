package bestan.common.net;

import bestan.common.guid.Guid;
import io.netty.util.AttributeKey;

public class NetConst {
	/**
	 * channel属性guid的索引
	 */
	public static final AttributeKey<Guid> GUID_ATTR_INDEX = AttributeKey.newInstance("guid_key");
}
