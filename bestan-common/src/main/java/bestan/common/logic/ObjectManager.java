package bestan.common.logic;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.protobuf.Message;

import bestan.common.guid.Guid;
import bestan.common.message.pack.MessagePack;
import bestan.common.thread.BExecutor;

public class ObjectManager {
	private ConcurrentHashMap <Guid, IObject> objectMap = new ConcurrentHashMap<>();
	private AtomicInteger managerIndex = new AtomicInteger();
	private BExecutor executor;
	
	private static class ObjectManagerHolder{
		private static final ObjectManager INSTANCE = new ObjectManager();
	}
	public static final ObjectManager getInstance() {
		return ObjectManagerHolder.INSTANCE;
	}
	public IObject getObject(Guid guid) {
		return objectMap.get(guid);
	}
	
	public void removeObject(Guid guid) {
		objectMap.remove(guid);
	}
	
	public void putObject(IObject obj) {
		objectMap.put(obj.getGuid(), obj);
	}
	
	public int incrementAndGetManagerIndex() {
		return managerIndex.incrementAndGet();
	}
	
	public void sendMessage(IObject object, Message message) {
		executor.execute(new MessagePack(object, message));
	}
	
	public void sendMessage(Guid guid, Message message) {
		executor.execute(new MessagePack(guid, message));
	}
}
