package bestan.common.net.nio;

import java.nio.channels.SocketChannel;


/**
 * This class represents a data event. It stores the socket associated and the
 * data that has been received.
 */
public class DataEvent {

	/** Associated socket channel */
	public SocketChannel channel;

	/** Data associated to the event */
	public byte[] data;

	/** Constructor
	 *
	 * @param socket SocketChannel
	 * @param data   the data of this event
	 */
	public DataEvent(SocketChannel socket, byte[] data) {
		this.channel = socket;
		this.data = data;
	}
}