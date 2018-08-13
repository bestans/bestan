package bestan.common.net;

import com.google.protobuf.Message;

public interface INetSession {
	void send(Message message);
}
