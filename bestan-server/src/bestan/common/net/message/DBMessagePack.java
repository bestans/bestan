package bestan.common.net.message;

import bestan.common.db.command.DBCommandWithCallback;

public class DBMessagePack implements IMessagePack {
	private final DelayedEventHandler delayedEventHandler;
	private final DBCommandWithCallback dbCommand; 
	
	/**
	 * @return the delayedEventHandler
	 */
	public DelayedEventHandler getDelayedEventHandler() {
		return delayedEventHandler;
	}

	/**
	 * @return the dbCommand
	 */
	public DBCommandWithCallback getDbCommand() {
		return dbCommand;
	}

	/**
	 * Constructor
	 * 
	 * @param handler
	 * @param data
	 */
	public DBMessagePack(DelayedEventHandler handler, DBCommandWithCallback data) {
		this.delayedEventHandler = handler;
		this.dbCommand = data;
	}
	
}
