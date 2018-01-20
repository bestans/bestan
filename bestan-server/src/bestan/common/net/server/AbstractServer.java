package bestan.common.net.server;

import bestan.common.datastruct.ServerOption;

public abstract class AbstractServer {
	protected ServerOption option;
	
	
	public AbstractServer(ServerOption option)
	{
		this.option = option;
	}
	
	public abstract void start() throws Exception;
	public abstract void stop() throws Exception;
}
