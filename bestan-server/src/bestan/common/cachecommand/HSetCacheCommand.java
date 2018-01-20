package bestan.common.cachecommand;

import java.io.IOException;

import bestan.common.cache.CacheCommand;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import bestan.common.cachecommand.CacheCommonDAO;
import bestan.common.dao.DAORegister;

public class HSetCacheCommand  extends CacheCommand implements ICachePipeline {
	public final String key;
	public final String field;
	public final String value;
	
	public HSetCacheCommand(String key, String field, String value) {
		this.key = key;
		this.field = field;
		this.value = value;
	}
	
	@Override
	public void execute(Jedis jedis) throws IOException {
		DAORegister.get().get(CacheCommonDAO.class).hset(jedis, key, field, value);
	}

	public void pipelineExcute(Pipeline p) {
		DAORegister.get().get(CacheCommonDAO.class).hset(p, key, field, value);
	}
	
}