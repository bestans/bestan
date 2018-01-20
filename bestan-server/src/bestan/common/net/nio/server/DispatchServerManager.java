package bestan.common.net.nio.server;

import java.nio.channels.SocketChannel;
import bestan.common.datastruct.Pair;
import bestan.common.log.LogManager;
import bestan.common.net.message.DelayEventManager;
import bestan.common.net.message.DelayedEventHandler;
import bestan.common.net.nio.MessagePack;

public final class DispatchServerManager extends Thread implements IDisconnectedListener {

	/** the logger instance. */
	private static final bestan.common.log.Logger logger = LogManager.getLogger(DispatchServerManager.class);

	/** We need network server manager to be able to send messages */
	private final INetworkServerManager netMan;

	/** The playerContainer handles all the player management */
//	private final Onlines playerContainer;

	/** The thread will be running while keepRunning is true */
	private boolean keepRunning;

	/** isFinished is true when the thread has really exited. */
	private boolean isfinished;
	
	/** process delayed events (eg: block operation) in this thread use by manager */
	private final DelayEventManager delayEventManager;
	
	/** handles disconnects */
//	private final DisconnectHandler disconnectHandler = new DisconnectHandler();
	
	/** server thread index */
	private int index;

	/**
	 * Constructor that initialize also the RPManager
	 *
	 * @param netMan
	 *            a NetworkServerManager instance.
	 * @throws Exception
	 *             is there is any problem.
	 */
	public DispatchServerManager(INetworkServerManager netMan, int index)
			throws Exception {
		super("GameServerManager"+index);
		keepRunning = true;
		isfinished = false;

		this.netMan = netMan;
		this.index = index;

		netMan.registerDisconnectedListener(this);

//		playerContainer = Onlines.getInstance();

		delayEventManager= new DelayEventManager();
	}

	/**
	 * This method request the active object to finish its execution and store
	 * all the players back to database.
	 */
	public void finish() {
		/* We store all the players when we are requested to exit */
		storeConnectedPlayers();

		keepRunning = false;

		interrupt();
		delayEventManager.setKeepRunning(false);

		while (isfinished == false) {
			Thread.yield();
		}
	}

	/*
	 * Disconnect any connected player and if the player has already login and
	 * was playing game it is stored back to database.
	 */
	private void storeConnectedPlayers() {
//		/*
//		 * We want to avoid concurrentComodification of playerContainer.
//		 */
//		List<Player> list=new LinkedList<Player>();
//		for (Player entry : playerContainer) {
//			list.add(entry);
//		}
//
//		/*
//		 * Now we iterate the list and remove characters.
//		 */
//		for (Player entry : list) {
//			logger.debug("STORING ("+entry.getUserName()+")");
//			/*
//			 * It may be a bit slower than disconnecting here, but server is
//			 * going down so there is no hurry.
//			 */
//			onDisconnect(null);//entry.getSocketChannel());
//		}
	}

	// ===========================================
	// to test
	public static int delayCount;
	public static long delayValue;
	
	public synchronized void doTest(long delay) {
		delayCount++;
		delayValue -= delay;
	}
	// ===========================================
	
	/**
	 * Runs the game glue logic. This class is responsible of receiving messages
	 * from clients and instruct RP about actions clients did.
	 * 
	 */
	@Override
	public void run() {
		try {
			logger.debug("GameServerManager Thread[" + index + "] start...");
			long start = System.nanoTime();
			long stop;
			long delay;
			long timeStart = 0;
			long[] timeEnds = new long[3];
			
			while (keepRunning) {
				stop = System.nanoTime();
				if(logger.isDebugEnabled()) {
					logger.debug("Turn time elapsed: " + ((stop - start) / 1000) + " microsecs");
				}
				
				delay = 1 - ((stop - start) / 1000000);  //millsecs
				if (delay < 0) {
					StringBuilder sb = new StringBuilder();
					for (long timeEnd : timeEnds) {
						sb.append(" " + (timeEnd - timeStart));
					}
//					doTest(delay);
//					logger.warn("Turn duration overflow by " + (-delay) + " ms: "
//					        + sb.toString());
				} else if (delay > 1) {
					logger.error("Delay bigger than Turn duration. [delay: " + delay
					        + "] [turnDuration:" + 1 + "]");
					delay = 0;
				}
				
				start = System.nanoTime();
				timeStart = System.currentTimeMillis();

				//===============================================
				timeEnds[0] = System.currentTimeMillis();
				MessagePack msgPack = netMan.getMessage(index);
				//===============================================
				
				//===============================================
				timeEnds[1] = System.currentTimeMillis();	
				if (msgPack != null) {
//					playerContainer.getLock().requestWriteLock();
//					if(msgPack.getChannel() != null) {
////						MessageDispatcher.getDispatcher().dispatchMessage(msgPack.getChannel(), msgPack);
//					} else {
						Pair<DelayedEventHandler, Object> entry = msgPack.getDelayHandle();
						entry.first().handleDelayedEvent(entry.second());
//					}
//					playerContainer.getLock().releaseLock();
				} 
				timeEnds[2] = System.currentTimeMillis();
				//===============================================
			}
		} catch (Throwable e) {
			logger.error("Unhandled exception, server index[" + index + "].", e);
		}

		isfinished = true;
	}

	/**
	 * This method is called by network manager when a client connection is lost
	 * or even when the client logout correctly.
	 *
	 * @param channel
	 *            the channel that was closed.
	 */
	public void onDisconnect(SocketChannel channel) {
//		logger.debug("GAME Disconnecting " + channel.socket().getRemoteSocketAddress());
//		Player removePlayer = null; //Onlines.getInstance().get(channel);
//		if(null == removePlayer || removePlayer.getLogicIdx() != this.index) {
//			logger.error("onDisconnect removePlayer is null or serverindex is invalid");
//			return;
//		}
		
//		Onlines.getInstance().remove(channel);
		
		//store player
//		delayEventManager.addDelayedEvent(disconnectHandler, channel);
	}
	
	public DelayEventManager getDelayEventManager() {
		return delayEventManager;
	}
}
