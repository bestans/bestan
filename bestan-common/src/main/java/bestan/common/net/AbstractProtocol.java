package bestan.common.net;

import bestan.common.guid.Guid;

public abstract class AbstractProtocol implements IProtocol {
	protected ProtocolHeader header;
	
	public AbstractProtocol(ProtocolHeader header) {
		this.header = header;
	}
	
	public Guid getGUID() {
		return header.getGuid();
	}
	
	@Override
	public long getThreadIndex() {
		if (getGUID() != null) {
			return getGUID().getValue();
		}
		
		return 0;
	}
	
}
