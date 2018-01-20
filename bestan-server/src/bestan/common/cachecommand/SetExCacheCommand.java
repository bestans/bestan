package bestan.common.cachecommand;

import java.io.IOException;

import bestan.common.cache.CacheCommand;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import bestan.common.cachecommand.CacheCommonDAO;
import bestan.common.dao.DAORegister;

public class SetExCacheCommand extends CacheCommand implements ICachePipeline {
	public final String key;
	public final String value;
	public final int seconds;
	
	public SetExCacheCommand(String key, String value, int seconds) {
		this.key = key;
		this.value = value;
		this.seconds = seconds;
	}

	@Override
	public void execute(Jedis jedis) throws IOException {
		DAORegister.get().get(CacheCommonDAO.class).setex(jedis, key, value, seconds);
	}

	public void pipelineExcute(Pipeline p) {
		DAORegister.get().get(CacheCommonDAO.class).setex(p, key, value, seconds);
	}
}
