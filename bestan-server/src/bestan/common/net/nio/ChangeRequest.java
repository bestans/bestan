package bestan.common.net.nio;

import java.nio.channels.SocketChannel;

/**
 * This class notify the NIO Server about a change request on one of the
 * sockets.
 */
public class ChangeRequest {

	public static final int REGISTER = 1;

	public static final int CHANGEOPS = 2;

	public static final int CLOSE = 3;

	/** Associated socket channel for this request. */
	public SocketChannel socket;

	/** Type of request */
	public int type;

	/** Extra params */
	public int ops;

	/** Constructor */
	public ChangeRequest(SocketChannel socket, int type, int ops) {
		this.socket = socket;
		this.type = type;
		this.ops = ops;
	}
}
