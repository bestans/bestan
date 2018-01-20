package bestan.common.cachecommand;

import java.io.IOException;

import com.google.protobuf.Message;

import bestan.common.cache.CacheCommand;
import bestan.common.util.Base64;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import bestan.common.cachecommand.CacheCommonDAO;
import bestan.common.dao.DAORegister;

public class MessageSetCacheCommand extends CacheCommand implements ICachePipeline {
	public final  String key;
	public final  Message message;
	
	public MessageSetCacheCommand(String key, Message message) {
		this.key = key;
		this.message = message;
	}
	
	@Override
	public void execute(Jedis jedis) throws IOException {
		if(null == message) {
			return;
		}
		
		String value = new String(Base64.encodeBase64(message.toByteArray()));
		DAORegister.get().get(CacheCommonDAO.class).set(jedis, key, value);
	}

	public void pipelineExcute(Pipeline p) {
		if(null == message) {
			return;
		}
		
		String value = new String(Base64.encodeBase64(message.toByteArray()));
		DAORegister.get().get(CacheCommonDAO.class).set(p, key, value);
	}
	
}
