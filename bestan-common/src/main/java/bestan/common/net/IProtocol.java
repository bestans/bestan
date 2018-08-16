package bestan.common.net;

import bestan.common.event.IEvent;

public interface IProtocol extends IEvent{
	byte[] encode();
}
