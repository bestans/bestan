/***************************************************************************
 *                   (C) Copyright 2003-2012 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package bestan.common.net.nio.server;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

import bestan.common.config.ServerConfig;
import bestan.common.datastruct.Pair;
import bestan.common.net.FloodCheck;
import bestan.common.net.NetConst;
import bestan.common.net.flood.FloodValidator;
import bestan.common.net.flood.IFloodCheck;
import bestan.common.net.message.DelayedEventHandler;
import bestan.common.net.message.MessageFactory;
import bestan.common.net.nio.DataEvent;
import bestan.common.net.nio.Decoder;
import bestan.common.net.nio.Encoder;
import bestan.common.net.nio.MessagePack;
import bestan.common.net.validator.ConnectionValidator;
import bestan.common.util.Utility;
import bestan.log.GLog;

import com.google.protobuf.GeneratedMessage;

/**
 * This is the implementation of a worker that sends messages, receives them,
 * This class also handles validation of connection and disconnection events
 *
 * @author miguel
 */
public final class NIONetworkServerManager extends Thread implements IWorker, IDisconnectedListener,
        INetworkServerManager {

	/** the logger instance. */
	private static final Logger logger = GLog.log;
	
	private static NIONetworkServerManager instance;

	/** We store the server for sending stuff. */
//	private NioServer server;
	
	private NewNioServer server;

	/** While keepRunning is true, we keep receiving messages */
	private boolean keepRunning;

	/** isFinished is true when the thread has really exited. */
	private boolean isFinished;

	/** A List of Message objects: List<Message> */
	@SuppressWarnings("rawtypes")
	private final BlockingQueue[] messages;

//	/** Statistics */
//	private final Statistics stats;

	/** checks if the ip-address is banned */
	private final ConnectionValidator connectionValidator;

	/** Checks if a connection is flooding the server */
	private final FloodValidator floodValidator;

	/** We queue here the data events. */
	private final BlockingQueue<DataEvent> queue;

	/** encoder is in charge of getting a Message and creating a stream of bytes. */
	private final Encoder encoder;

	/** decoder takes a stream of bytes and create a message */
	private final Decoder decoder;
	
	private int index;
	
	private int processCount;
	
	private Map<SocketChannel, Integer> inThreadMap;
	
	@SuppressWarnings("unused")
	private List<Pair<SocketChannel, Integer>> connectList;
	
	public static NIONetworkServerManager getInstance() {
		return instance;
	}
	
	public Integer getThreadIdxByChannel(SocketChannel channel) {
		return inThreadMap.get(channel);
	}
	
	/**
	 * Constructor
	 *
	 * @throws IOException
	 *             if there any exception when starting the socket server.
	 */
	@SuppressWarnings("deprecation")
	public NIONetworkServerManager() throws IOException {
		super("NetworkServerManager");
		/*
		 * init the packet validator (which can now only check if the address is
		 * banned)
		 */
		connectionValidator = new ConnectionValidator();

		/* create a flood check on connections */
		IFloodCheck check = new FloodCheck(this);
		floodValidator = new FloodValidator(check);

		keepRunning = true;
		isFinished = false;

		encoder = Encoder.get();
		decoder = Decoder.get();

		index = 0;
		/*
		 * Because we access the list from several places we create a
		 * synchronized list.
		 */
		processCount = ServerConfig.getInstance().logicThreadNum;
		messages = new BlockingQueue[processCount];
		for(int i = 0; i < processCount; i++) {
			messages[i] = new LinkedBlockingQueue<MessagePack>();
		}
		
//		stats = Statistics.getStatistics();
		queue = new LinkedBlockingQueue<DataEvent>();

		logger.debug("NetworkServerManager started successfully");

		server = new NewNioServer(null, NetConst.tcpPort, this);
		server.start();
		/*
		 * Register network listener for get disconnection events.
		 */
		server.registerDisconnectedListener(this);
		server.registerDisconnectedListener(floodValidator);
		
		inThreadMap = new HashMap<SocketChannel, Integer>();
		
		connectList = new LinkedList<Pair<SocketChannel,Integer>>();
		
		instance = this;
	}

	/**
	 * Associate this object with a server. This model a master-slave approach
	 * for managing network messages.
	 *
	 * @param server
	 *            the master server.
	 */
	public void setServer(NioServer server) {
//		this.server = server;
	}
	
	public void setNewServer(NewNioServer newServer) {
		this.server = newServer;
	}

	/**
	 * This method notifies the thread to finish the execution
	 */
	public void finish() {
		logger.debug("shutting down NetworkServerManager");
		keepRunning = false;

		connectionValidator.finish();
		server.finish();
		interrupt();

		while (isFinished == false) {
			Thread.yield();
		}

		logger.debug("NetworkServerManager is down");
	}

	/**
	 * This method blocks until a message is available
	 *
	 * @return a MessagePack
	 */
	public MessagePack getMessage(int idx) {
		try {
			return (MessagePack)messages[idx].take();
		} catch (InterruptedException e) {
			/* If interrupted while waiting we just return null */
			return null;
		}
	}
	
	public MessagePack getMessageNotBlock(int idx) {
		try {
			return (MessagePack)messages[idx].poll(1, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * This method blocks until a message is available
	 *
	 * @return a MessagePack
	 */
	public int getQueueSize(int idx) {
		return messages[idx].size();
	}

	/**
	 * We check that this socket is not banned. We do it just on connect so we
	 * save lots of queries.
	 */
	public void onConnect(SocketChannel channel) {
		Socket socket = channel.socket();
		doPatchThread(channel);
		logger.debug("Connected from " + socket.getRemoteSocketAddress());
		//TODO: check banned ip
	}

	private void doPatchThread(SocketChannel channel) {
		if(inThreadMap.containsKey(channel)) {
			logger.error("may be error");
		} else {
			index = (index + 1) % processCount;
			inThreadMap.put(channel, index);
		}
	}
	
	
	/**
	 * This method is called when new data is received on server from channel.
	 *
	 * @param server
	 *            the master server
	 * @param channel
	 *            socket channel associated to the event
	 * @param data
	 *            the data received
	 * @param count
	 *            the amount of data received.
	 */
	public void onData(NioServer server, SocketChannel channel, byte[] data, int count) {
		logger.debug("Received from channel:"+channel+" "+count+" bytes");

//		stats.add("Bytes recv", count);
//		stats.add("Message recv", 1);

		/*
		 * We check the connection in case it is trying to flood server.
		 */
		if (floodValidator.isFlooding(channel, count)) {
			/*
			 * If it is flooding, let the validator decide what to do.
			 */
			logger.warn("Channel: "+channel+" is flooding");
			floodValidator.onFlood(channel);
		} else {
			/*
			 * If it is not flooding, just queue the message.
			 */
			logger.debug("queueing message");

			byte[] dataCopy = new byte[count];
			System.arraycopy(data, 0, dataCopy, 0, count);
			try {
				queue.put(new DataEvent(channel, dataCopy));
			} catch (InterruptedException e) {
				/* This is never going to happen */
				logger.error("Not expected",e);
			}
		}
	}
	
	public void onData(NewNioServer newServer, SocketChannel channel, byte[] data, int count) {
		logger.debug("Received from channel:"+channel+" "+count+" bytes");

//		stats.add("Bytes recv", count);
//		stats.add("Message recv", 1);

		/*
		 * We check the connection in case it is trying to flood server.
		 */
		if (floodValidator.isFlooding(channel, count)) {
			/*
			 * If it is flooding, let the validator decide what to do.
			 */
			logger.warn("Channel: "+channel+" is flooding");
			floodValidator.onFlood(channel);
		} else {
			/*
			 * If it is not flooding, just queue the message.
			 */
			logger.debug("queueing message");

			byte[] dataCopy = new byte[count];
			System.arraycopy(data, 0, dataCopy, 0, count);
			try {
				queue.put(new DataEvent(channel, dataCopy));
			} catch (InterruptedException e) {
				/* This is never going to happen */
				logger.error("Not expected",e);
			}
		}
	}
	
	public void sendMessage(GeneratedMessage msg, SocketChannel channel) {
		try {
			int nMsgId = MessageFactory.getFactory().getMsgIdByMessageClass(msg.getClass());
			if(-1 == nMsgId) {
				logger.error("sendMessage is not have this message class:" + msg.getClass().toString());
				return;
			}
			
			byte operationType = 0x00;
			byte[] data = encoder.encode(msg, nMsgId, operationType);

//			stats.add("Bytes send", data.length);
//			stats.add("Message send", 1);

			server.send(channel, data);
		} catch (Exception e) {
			e.printStackTrace();
			/**
			 * I am not interested in the exception. NioServer will detect this
			 * and close connection
			 */
		}
	}

	/**
	 * This method disconnect a socket.
	 *
	 * @param channel
	 *            the socket channel to close
	 */
	public void disconnectClient(SocketChannel channel) {
		try {
			server.close(channel);
		} catch (Exception e) {
			logger.error("Unable to disconnect a client " + channel.socket(), e);
		}

	}

	/**
	 * Returns a instance of the connection validator
	 * {@link ConnectionValidator} so that other layers can manipulate it for
	 * banning IP.
	 *
	 * @return the Connection validator instance
	 */
	public ConnectionValidator getValidator() {
		return connectionValidator;
	}

	/**
	 * Register a listener for disconnection events.
	 *
	 * @param listener
	 *            a listener for disconnection events.
	 */
	public void registerDisconnectedListener(IDisconnectedListener listener) {
		server.registerDisconnectedListener(listener);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		try {
			while (keepRunning) {
				DataEvent event = queue.take();

				try {	
					List<MessagePack> recvMessagePacks = decoder.decode(event.channel, event.data);
					if (recvMessagePacks != null) {
						Integer tmpInteger = inThreadMap.get(event.channel);
						if(null == tmpInteger) {
							logger.error("run msg chnnel is error");
						} else {
							for (MessagePack msgPack : recvMessagePacks) {
								if (logger.isDebugEnabled()) {
									logger.debug("recv message(type=" + msgPack.getMessageId() + ") from "
									        + event.channel.socket().getRemoteSocketAddress() + " full [" + msgPack.getMessage().toString() + "]");
								}
								
								messages[tmpInteger.intValue()].add(msgPack);
							}
						}
					}
				} catch (IOException e) {
					logger.warn("IOException while building message:\n" + Utility.dumpByteArray(event.data), e);
					logger.warn("sender was: " + event.channel.socket().getRemoteSocketAddress());
				} catch (RuntimeException e) {
					logger.warn("RuntimeException while building message:\n" + Utility.dumpByteArray(event.data), e);
					logger.warn("sender was: " + event.channel.socket().getRemoteSocketAddress());
					e.printStackTrace();
				}
			}
		} catch (InterruptedException e) {
			logger.warn(getName()+" interrupted. Finishing network layer.");
			keepRunning = false;
		}

		isFinished = true;
	}

	/**
	 * Removes stored parts of message for this channel at the decoder.
	 *
	 * @param channel
	 *            the channel to clear
	 */
	public void onDisconnect(SocketChannel channel) {
		logger.debug("NET Disconnecting " + channel.socket().getRemoteSocketAddress());
		decoder.clear(channel);
	}
	
	public int getProcessCount() {
		return processCount;
	}
	
	@SuppressWarnings("unchecked")
	public void addMessagePack(int idx, MessagePack msgPack) {
		if(0 <= idx && processCount > idx) {
			messages[idx].add(msgPack);
		} else {
			logger.error("idx is invalid");
		}
	}
	
	@SuppressWarnings("unchecked")
	public void addMessagePack(int idx, DelayedEventHandler handler, Object data) {
		if(0 <= idx && processCount > idx) {
			messages[idx].add(new MessagePack(handler, data));
		} else {
			logger.error("idx is invalid");
		}
	}
}
