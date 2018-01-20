/***************************************************************************
 *                   (C) Copyright 2003-2009 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package bestan.common.net.validator;

import java.net.InetAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;

import bestan.common.log.LogManager;
import bestan.common.net.message.DelayedEventHandler;
import bestan.log.GLog;

/**
 * The ConnectionValidator validates the ariving connections, currently it can
 * only check if the address is banned.
 * <p>
 * There are two types of bans:
 * <ul>
 * <li>Permanent bans<br>
 * That are stored at database and that we offer no interface.
 * <li>Temportal bans<br>
 * That are not stored but that has a interface for adding, removing and
 * querying bans.
 * </ul>
 */
public class ConnectionValidator implements Iterable<InetAddressMask>, DelayedEventHandler {

	/** the logger instance. */
	private static final Logger logger = GLog.log;

	/** Permanent bans are stored inside the database. ���ý��Ŷ��� */
	private List<InetAddressMask> permanentBans;

	/**
	 * Temporal bans are added using the API and are lost on each server reset.
	 * Consider using Database for a permanent ban
	 */
	List<InetAddressMask> temporalBans;

	/** A timer to remove ban when it is done. */
	private Timer timer;

	/* timestamp of last reload */
	@SuppressWarnings("unused")
	private long lastLoadTS;

	/* How often do we reload ban information from database? Each 5 minutes */
	@SuppressWarnings("unused")
	private final static long RELOAD_PERMANENT_BANS = 5 * 60 * 1000;

	/**
	 * Constructor. It loads permanent bans from database.
	 */
	public ConnectionValidator() {
		permanentBans = Collections.synchronizedList(new LinkedList<InetAddressMask>());
		temporalBans = Collections.synchronizedList(new LinkedList<InetAddressMask>());
		timer = new Timer();
	}

	/**
	 * Request connection validator to stop all the activity, and stop checking
	 * if any ban needs to be removed.
	 *
	 */
	public void finish() {
		timer.cancel();
	}

	/**
	 * This class extend timer task to remove bans when the time is reached.
	 *
	 * @author miguel
	 *
	 */
	private class RemoveBan extends TimerTask {

		private InetAddressMask mask;

		/**
		 * Constructor
		 *
		 * @param mask
		 */
		public RemoveBan(InetAddressMask mask) {
			this.mask = mask;
		}

		@Override
		public void run() {
			temporalBans.remove(mask);
		}
	}

	/**
	 * Adds a ban just for this ip address for i seconds.
	 *
	 * @param channel
	 *            the channel whose IP we are going to ban.
	 * @param time
	 *            how many seconds to ban.
	 */
	public void addBan(SocketChannel channel, int time) {
		addBan(channel.socket().getInetAddress().getHostAddress(), "255.255.255.255", time);

	}

	/**
	 * This adds a temporal ban.
	 *
	 * @param address
	 *            the address to ban
	 * @param mask
	 *            mask to apply to the address
	 * @param time
	 *            how many seconds should the ban apply.
	 */
	public void addBan(String address, String mask, long time) {
		InetAddressMask inetmask = new InetAddressMask(address, mask);

		timer.schedule(new RemoveBan(inetmask), time);
		temporalBans.add(inetmask);
	}

	/**
	 * Removes one of the added temporal bans.
	 *
	 * @param address
	 *            the address to remove
	 * @param mask
	 *            the mask used.
	 * @return true if it has been removed.
	 */
	public boolean removeBan(String address, String mask) {
		return temporalBans.remove(new InetAddressMask(address, mask));
	}

	/**
	 * Returns an iterator over the temporal bans. To access permanent bans, use
	 * database facility.
	 */
	public Iterator<InetAddressMask> iterator() {
		return temporalBans.iterator();
	}

	/**
	 * Is the source ip-address banned?
	 *
	 * by lxw �˺������̰߳�ȫ��֧��ͬ�� ����ֻ��һ���߳�ȥ�����������Ƿ���Ҫ��synchronizedȥ��
	 * @param address
	 *            the InetAddress of the source
	 * @return true if the source ip is banned
	 */
	public synchronized boolean checkBanned(InetAddress address) {
		checkReload();

		for (InetAddressMask iam : temporalBans) {
			if (iam.matches(address)) {
				logger.debug("Address " + address + " is banned by " + iam);
				return true;
			}
		}

		for (InetAddressMask iam : permanentBans) {
			if (iam.matches(address)) {
				logger.debug("Address " + address + " is permanently banned by " + iam);
				return true;
			}
		}

		return false;
	}

	/**
	 * Check if a socket that has a InetAddress associated is banned.
	 * 
	 * @param socket
	 *            the socket we want to check if it is banned or not.
	 * @return true if it is banned.
	 */
	public synchronized boolean checkBanned(Socket socket) {
		InetAddress address = socket.getInetAddress();
		return checkBanned(address);
	}

	/**
	 * DelayEvent
	 *
	 * @param rpMan
	 *            the socket we want to check if it is banned or not.
	 * @param data
	 */
	public void handleDelayedEvent(Object data) {
//		LoadBanListCommand cmd = (LoadBanListCommand) data;
//		permanentBans.clear();
//		permanentBans.addAll(cmd.getPermanentBans());
//		lastLoadTS = System.currentTimeMillis();
	}

	/**
	 * checks if reload is necessary and performs it
	 */
	public synchronized void checkReload() {
//		if (System.currentTimeMillis() - lastLoadTS >= RELOAD_PERMANENT_BANS) {
//			DBCommandQueue.get().enqueue(new LoadBanListCommand(this));
//		}
	}

}
