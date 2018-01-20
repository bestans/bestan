package bestan.common.net.nio.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import bestan.common.log.LogManager;
import bestan.common.net.nio.ChangeRequest;

public class NewNioServer extends Thread {
	@SuppressWarnings("unused")
	private static final int BACKLOG_WARNING_SIZE = 50;
	private static final int BACK_LOG = 128;

	/** the logger instance. */
	private static final bestan.common.log.Logger logger = LogManager.getLogger(NewNioServer.class);

	/** server 地址族 */
	private final InetAddress hostAddress;

	/** server 端口*/
	private final int port;

	/** Running Flag */
	private boolean keepRunning;

	/** isFinished is true when the thread has really exited. */
	private boolean isFinished;

	/** server socketchannel */
	private ServerSocketChannel serverChannel;

	/** Selector 网络模型 Reactor */
	private final Selector selector;

	/** 读取socket的缓存 */
	private final ByteBuffer readBuffer = ByteBuffer.allocate(8192);

	/**
	 * This is the slave associated with this master. As it is a simple thread,
	 * we only need one slave.
	 */
	private final IWorker worker;

	/** 改变队列 */
	@SuppressWarnings("unused")
	private final Map<SocketChannel, List<ByteBuffer>> pendingChanges = new HashMap<SocketChannel, List<ByteBuffer>>();

	/** 关闭队列 */
	private final List<ChangeRequest> pendingClosed;

	/** socket hashmap */
	private final Map<SocketChannel, List<ByteBuffer>> pendingData = new HashMap<SocketChannel, List<ByteBuffer>>();

	/** 关闭事件监听实例 */
	private final List<IDisconnectedListener> listeners;

	public NewNioServer(InetAddress hostAddress, int port, IWorker worker) throws IOException {
		super("NewNioServer");

		keepRunning = true;
		isFinished = false;

		this.hostAddress = hostAddress;
		this.port = port;
		this.selector = this.initSelector();
		this.worker = worker;
		this.worker.setNewServer(this);

		pendingClosed = new LinkedList<ChangeRequest>();
		listeners = new LinkedList<IDisconnectedListener>();
	}

	/**
	 * This method closes a channel. It also notify any listener about the
	 * event.
	 *
	 * @param channel
	 *            the channel to close.
	 */
	public void close(SocketChannel channel) {
		for (IDisconnectedListener listener : listeners) {
			listener.onDisconnect(channel);
		}


		pendingClosed.add(new ChangeRequest(channel, ChangeRequest.CLOSE, 0));

		/*
		 * Wake up to make the closure effective.
		 */
		selector.wakeup();
	}

	/**
	 * This method is used to send data on a socket.
	 *
	 * @param socket
	 *            the socketchannel to use.
	 * @param data
	 *            a byte array of data to send
	 */
	public void send(SocketChannel socket, byte[] data) {
			// And queue the data we want written
//		synchronized(this.pendingChanges) {
//				List<ByteBuffer> queue = this.pendingChanges.get(socket);
//				if (queue == null) {
//					queue = new ArrayList<ByteBuffer>();
//					this.pendingChanges.put(socket, queue);
//				}
//				queue.add(ByteBuffer.wrap(data));
//				if (queue.size() > BACKLOG_WARNING_SIZE) {
//					logger.debug(socket + ": " + queue.size());
//				}
//		}
//		// Finally, wake up our selecting thread so it can make the required
//		// changes
//		this.selector.wakeup();
		try {
			int len = socket.write(ByteBuffer.wrap(data));
			
			if (len < 0) {
				// ... or the socket's buffer fills up
				logger.error("Send Send Send Send failed");
			}
			
			if (len == 0) {
				logger.error("helllo helllo helllo");
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * Finish this thread in a correct way.
	 */
	public void finish() {
		keepRunning = false;

		selector.wakeup();

		while (isFinished == false) {
			Thread.yield();
		}

		try {
			selector.close();
		} catch (IOException e) {
			// We really don't care about the exception.
		}
	}

	@Override
	public void run() {
		while (keepRunning) {
			try {
					Iterator<?> it = pendingClosed.iterator();
					while (it.hasNext()) {
						ChangeRequest change = (ChangeRequest) it.next();
						if (change.socket.isConnected()) {
							if (change.type == ChangeRequest.CLOSE) {
								try {
									// Force data to be sent if there is data waiting.
									if (pendingData.containsKey(change.socket)) {
										SelectionKey key = change.socket.keyFor(selector);
										if (key.isValid()) {
											write(key);
										}
									}

									// Close the socket
									change.socket.close();
								} catch (Exception e) {
									logger.debug("Exception happened when closing socket", e);
								}
								break;
							}
						} else {
							logger.debug("Closing a not connected socket");
						}
					}
					pendingClosed.clear();

				// Wait for an event one of the registered channels
				this.selector.select();

				// Iterate over the set of keys for which events are available
				Iterator<?> selectedKeys = this.selector.selectedKeys().iterator();
				while (selectedKeys.hasNext()) {
					SelectionKey key = (SelectionKey) selectedKeys.next();
					selectedKeys.remove();

					if (!key.isValid()) {
						continue;
					}

					// Check what event is available and deal with it
					if (key.isAcceptable()) {
						this.accept(key);
					} 
					if (key.isReadable()) {
						this.read(key);
					} 
//					if (key.isWritable()) {
//						this.write(key);
//					}
				}
			} catch (IOException e) {
				logger.error("Error on NIOServer", e);
			} catch (RuntimeException e) {
				logger.error("Error on NIOServer", e);
			}
		}

		isFinished = true;
	}

	private void accept(SelectionKey key) throws IOException {
		// For an accept to be pending the channel must be a server socket
		// channel.
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

		// Accept the connection and make it non-blocking
		SocketChannel socketChannel = serverSocketChannel.accept();
		socketChannel.configureBlocking(false);

		// Register the new SocketChannel with our Selector, indicating
		// we'd like to be notified when there's data waiting to be read
		socketChannel.register(this.selector, SelectionKey.OP_READ);

		worker.onConnect(socketChannel);
	}

	private void read(SelectionKey key) {
		SocketChannel socketChannel = (SocketChannel) key.channel();

		// Clear out our read buffer so it's ready for new data
		this.readBuffer.clear();

		// Attempt to read off the channel
		int numRead;
		try {
			numRead = socketChannel.read(this.readBuffer);
		} catch (IOException e) {
			// The remote forcibly closed the connection, cancel
			// the selection key and close the channel.
			if(logger.isDebugEnabled()) {
				logger.debug("Remote closed connnection", e);
			}
			key.cancel();

			close(socketChannel);

			return;
		}

		if (numRead == -1) {
			// Remote entity shut the socket down cleanly. Do the
			// same from our end and cancel the channel.
			logger.debug("Remote closed connnection cleanly");
			close((SocketChannel) key.channel());

			key.cancel();
			return;
		}

		// Hand the data off to our worker thread
		this.worker.onData(this, socketChannel, this.readBuffer.array(), numRead);
	}

	private void write(SelectionKey key) {
		SocketChannel socketChannel = (SocketChannel) key.channel();

			List<ByteBuffer> queue = this.pendingData.get(socketChannel);

			try {
				// Write until there's not more data ...
				while (!queue.isEmpty()) {
					ByteBuffer buf = queue.get(0);
					socketChannel.write(buf);

					if (buf.remaining() > 0) {
						// ... or the socket's buffer fills up
						break;
					}
					queue.remove(0);
				}

				if (queue.isEmpty()) {
					// We wrote away all data, so we're no longer interested
					// in writing on this socket. Switch back to waiting for
					// data.
					key.interestOps(SelectionKey.OP_READ);
				}
			} catch (IOException e) {
				// The remote forcibly closed the connection, cancel
				// the selection key and close the channel.
				logger.debug("Remote closed connnection", e);
				queue.clear();
				key.cancel();

				close(socketChannel);

				return;
			}
	}


	private Selector initSelector() throws IOException {
		// Create a new selector
		Selector socketSelector = SelectorProvider.provider().openSelector();

		// Create a new non-blocking server socket channel
		this.serverChannel = ServerSocketChannel.open();
		serverChannel.configureBlocking(false);

		// Bind the server socket to the specified address and port
		InetSocketAddress isa = new InetSocketAddress(this.hostAddress, this.port);
		serverChannel.socket().bind(isa, BACK_LOG);
		serverChannel.socket().setPerformancePreferences(0, 2, 1);
		
		serverChannel.socket().setReuseAddress(true);
		serverChannel.socket().setReceiveBufferSize(4096*1024);
		
		// Register the server socket channel, indicating an interest in
		// accepting new connections
		serverChannel.register(socketSelector, SelectionKey.OP_ACCEPT);

		return socketSelector;
	}

	/**
	 * Register a listener to notify about disconnected events
	 *
	 * @param listener listener to add
	 */
	public void registerDisconnectedListener(IDisconnectedListener listener) {
		this.listeners.add(listener);
	}
}
