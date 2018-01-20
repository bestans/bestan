package bestan.common.cachecommand;

import java.io.IOException;
import java.util.Map;

import bestan.common.cache.CacheCommand;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import bestan.common.cachecommand.CacheCommonDAO;
import bestan.common.dao.DAORegister;

public class HMSetCacheCommand extends CacheCommand implements ICachePipeline {
	public final String key;
	public final Map<String, String> paramMap;
	
	public HMSetCacheCommand(String key, Map<String, String> paramMap) {
		this.key = key;
		this.paramMap = paramMap;
	}
	
	@Override
	public void execute(Jedis jedis) throws IOException {
		DAORegister.get().get(CacheCommonDAO.class).hmset(jedis, key, paramMap);
	}

	public void pipelineExcute(Pipeline p) {
		DAORegister.get().get(CacheCommonDAO.class).hmset(p, key, paramMap);
	}
	
}