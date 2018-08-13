package bestan.common.logic;

import java.util.concurrent.ExecutorService;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import bestan.common.net.AbstractProtocol;

public class ProtocolManager {
	private static class ProtocolManagerHolder{
		private static final ProtocolManager instance = new ProtocolManager();
	}
	
	private ListeningExecutorService threadPoolService = null;
	
	private ProtocolManager() {
		
	}
	
	public static final ProtocolManager getInstance() {
		return ProtocolManagerHolder.instance;
	}
	
	public boolean init(ExecutorService executorService) {
		if (executorService == null)
			return false;
		
		threadPoolService = MoreExecutors.listeningDecorator(executorService);
		return true;
	}
	
	public void sendCallback(IObject src, IObject dst, AbstractProtocol arg) {
		var callback = new CommonCallback(src, dst, arg);
		Futures.addCallback(threadPoolService.submit(callback), callback, threadPoolService);
	}
}
