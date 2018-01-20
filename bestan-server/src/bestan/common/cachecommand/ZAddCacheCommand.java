package bestan.common.cachecommand;

import java.io.IOException;

import bestan.common.cache.CacheCommand;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import bestan.common.cachecommand.CacheCommonDAO;
import bestan.common.dao.DAORegister;

public class ZAddCacheCommand extends CacheCommand implements ICachePipeline {
	public final String key;
	public final double scoreValue;
	public final String value;
	
	public ZAddCacheCommand(String key, double scoreValue, String value) {
		this.key = key;
		this.scoreValue = scoreValue;
		this.value = value;
	}
	
	public void pipelineExcute(Pipeline p) {
		DAORegister.get().get(CacheCommonDAO.class).zadd(p, key, scoreValue, value);
	}

	@Override
	public void execute(Jedis jedis) throws IOException {
		DAORegister.get().get(CacheCommonDAO.class).zadd(jedis, key, scoreValue, value);
	}

}
