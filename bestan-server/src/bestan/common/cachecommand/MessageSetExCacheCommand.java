package bestan.common.cachecommand;

import java.io.IOException;

import bestan.common.cache.CacheCommand;
import bestan.common.util.Base64;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import com.google.protobuf.Message;

import bestan.common.cachecommand.CacheCommonDAO;
import bestan.common.dao.DAORegister;

public class MessageSetExCacheCommand extends CacheCommand implements ICachePipeline {
	public final String key;
	public final Message message;
	public final int seconds;
	
	public MessageSetExCacheCommand(String key, Message message, int seconds) {
		this.key = key;
		this.message = message;
		this.seconds = seconds;
	}
	
	@Override
	public void execute(Jedis jedis) throws IOException {
		if(null == message || 0 >= seconds) {
			return;
		}
		
		String value = new String(Base64.encodeBase64(message.toByteArray()));
		DAORegister.get().get(CacheCommonDAO.class).setex(jedis, key, value, seconds);
	}

	public void pipelineExcute(Pipeline p) {
		if(null == message || 0 >= seconds) {
			return;
		}
		
		String value = new String(Base64.encodeBase64(message.toByteArray()));
		DAORegister.get().get(CacheCommonDAO.class).setex(p, key, value, seconds);
	}
		
}
