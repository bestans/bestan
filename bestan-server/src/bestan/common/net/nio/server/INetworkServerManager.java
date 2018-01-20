package bestan.common.net.nio.server;

import java.nio.channels.SocketChannel;

import bestan.common.net.nio.MessagePack;
import bestan.common.net.validator.ConnectionValidator;

import com.google.protobuf.GeneratedMessage;

/**
 * A Network Server Manager is an active object ( a thread ) that send and
 * receive messages from clients. There is not transport or technology imposed.
 * <p>
 * The Network Manager is our router that sends and receives messages to and
 * from the network. The manager exposes the interfaces that allow:
 * <ul>
 * <li>Reading a message from the network
 * <li>Sending a message to the network
 * <li>Finalizing the manager
 * </ul>
 * 
 * Now lets get back to the interface as exposed to other objects.<br>
 * 
 * The Write method is immediate, just call it with the message to send, making
 * sure that you have correctly filled SourceAddress and ClientID. The message
 * will then be sent to the Client.
 * 
 * The Read method is blocking, when you call the Read method it either returns
 * a message from the queue or if the queue is empty the thread blocks (sleeps)
 * until one arrives.
 * 
 * @author miguel
 */
public interface INetworkServerManager {

	/**
	 * Register a listener that will be called when a disconnected event
	 * happens. It is up to the implementer if this call add or replace the
	 * actual listener.
	 * 
	 * @param listener
	 *            a listener of disconnection events.
	 */
	public abstract void registerDisconnectedListener(IDisconnectedListener listener);

	/**
	 * This method provides the connection validator object. You can use it to
	 * ban connection IP.
	 * 
	 * @return validator.
	 */
	public abstract ConnectionValidator getValidator();

	/**
	 * This method blocks until a message is available
	 * 
	 * @return a Message
	 */
	public abstract MessagePack getMessage(int idx);
	
	/**
	 * This method not blocks until a message is available
	 * 
	 * @return a Message
	 */
	public abstract MessagePack getMessageNotBlock(int idx);
	
	/**
	 * This method get Queue size
	 * 
	 * @return a Message
	 */
	public abstract int getQueueSize(int idx);


	public abstract void sendMessage(GeneratedMessage msg, SocketChannel channel);
	
	/**
	 * This method disconnect a client or silently fails if client doesn't
	 * exists.
	 * 
	 * @param channel
	 */
	public abstract void disconnectClient(SocketChannel channel);

	/**
	 * This method inits the active object
	 */
	public abstract void start();

	/**
	 * This method notify the active object to finish it execution
	 */
	public abstract void finish();
	
	/**
	 * XXX get Logic Count
	 */
	public abstract int getProcessCount();

}