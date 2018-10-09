package bestan.common.net.server;

import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import bestan.common.net.ICloseChannelChecker;
import io.netty.channel.Channel;

/**
 * @author yeyouhuan
 *
 */
public class ConnectionCollector {
	private ReentrantLock lock = new ReentrantLock();
	private Map<Long, Channel> allChannels = Maps.newHashMap();
	
	public void closeChannels(ICloseChannelChecker handler) {
		lock.lock();
		try {
			List<Channel> closeList = Lists.newArrayList();
			var it = allChannels.entrySet().iterator();
			while (it.hasNext()) {
				var entry = it.next();
				var channel = entry.getValue();
				if (handler.checkCanClose(channel)) {
					//需要关闭
					closeList.add(channel);
				}
			}
			for (var channel : closeList) {
				channel.close();
			}
		} finally {
			lock.unlock();
		}
	}
}
