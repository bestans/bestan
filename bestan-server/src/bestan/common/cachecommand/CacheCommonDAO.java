package bestan.common.cachecommand;

import java.util.Map;

import bestan.common.log.LogManager;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import bestan.common.cachecommand.ICachePipeline;

public class CacheCommonDAO {
	private static final bestan.common.log.Logger logger = LogManager.getLogger(CacheCommonDAO.class);
	
	public CacheCommonDAO() {
	}
	
	public void set(Jedis jedis, String key, String value) {
		if(null == jedis || null == key || null == value) {
			logger.error("CacheCommonDAO::set param is nil");
			return;
		}
		
		jedis.set(key, value);
	}
	
	public void set(Pipeline p, String key, String value) {
		if(null == p || null == key || null == value) {
			logger.error("CacheCommonDAO::set param is nil");
			return;
		}
		
		p.set(key, value);
	}
	
	public void setex(Jedis jedis, String key, String value, int seconds) {
		if(null == jedis || null == key || null == value || 0 >= seconds) {
			logger.error("CacheCommonDAO::setex param invalid");
			return;
		}
		
		jedis.setex(key, seconds, value);
	}
	
	public void setex(Pipeline p, String key, String value, int seconds) {
		if(null == p || null == key || null == value || 0 >= seconds) {
			logger.error("CacheCommonDAO::setex param invalid");
			return;
		}
		
		p.setex(key, seconds, value);
	}
	
	public void mset(Jedis jedis, String[] keyvalues) {
		if(null == jedis || null == keyvalues || 0 == keyvalues.length || 0 != (keyvalues.length % 2)) {
			logger.error("CacheCommonDAO::mset param is nil");
			return;
		}
		
		jedis.mset(keyvalues);
	}
	
	public void mset(Pipeline p, String[] keyvalues) {
		if(null == p || null == keyvalues || 0 == keyvalues.length || 0 != (keyvalues.length % 2)) {
			logger.error("CacheCommonDAO::mset param is nil");
			return;
		}
		
		p.mset(keyvalues);
	}
	
	public void hset(Jedis jedis, String key, String field, String value) {
		if(null == jedis || null == key || null == field || null == value) {
			logger.error("CacheCommonDAO::hset param is nil");
			return;
		}
		
		jedis.hset(key, field, value);
	}
	
	public void hset(Pipeline p, String key, String field, String value) {
		if(null == p || null == key || null == field || null == value) {
			logger.error("CacheCommonDAO::hset param is nil");
			return;
		}
		
		p.hset(key, field, value);
	}
	
	public void hmset(Jedis jedis, String key, Map<String, String> mapParam) {
		if(null == jedis || null == key || null == mapParam || 0 == mapParam.size()) {
			logger.error("CacheCommonDAO::hmset param is nil");
			return;
		}
		
		jedis.hmset(key, mapParam);
	}
	
	public void hmset(Pipeline p, String key, Map<String, String> mapParam) {
		if(null == p || null == key || null == mapParam || 0 == mapParam.size()) {
			logger.error("CacheCommonDAO::hmset param is nil");
			return;
		}
		
		p.hmset(key, mapParam);
	}
	
	public void zadd(Jedis jedis, String key, double sortValue, String value) {
		if(null == jedis || null == key || null == value) {
			logger.error("CacheCommonDAO::zadd param is nil");
			return;
		}
		
		jedis.zadd(key, sortValue, value);
	}
	
	public void zadd(Pipeline p, String key, double sortValue, String value) {
		if(null == p || null == key || null == value) {
			logger.error("CacheCommonDAO::zadd param is nil");
			return;
		}
		
		p.zadd(key, sortValue, value);
	}
	
	public void zrem(Jedis jedis, String key, String[] value) {
		if(null == jedis || null == key || null == value || 0 == value.length) {
			logger.error("CacheCommonDAO::zrem param is nil");
			return;
		}
		
		jedis.zrem(key, value);
	}
	
	public void zrem(Pipeline p, String key, String[] value) {
		if(null == p || null == key || null == value || 0 == value.length) {
			logger.error("CacheCommonDAO::zrem param is nil");
			return;
		}
		
		p.zrem(key, value);
	}
	
	public void pipelineOperation(Jedis jedis, ICachePipeline[] operationArray) {
		if(null == jedis || null == operationArray || 0 == operationArray.length) {
			logger.error("CacheCommonDAO::pipelineOperation param is nil");
			return;
		}
		
		Pipeline p = jedis.pipelined();
		if(null == p) {
			logger.error("CacheCommonDAO::pipelineOperation get pipeline is nil");
			return;
		}
		
		for(int i = 0; i < operationArray.length; i++) {
			ICachePipeline operation = operationArray[i];
			if(null == operation) {
				logger.error("CacheCommonDAO::pipelineOperation get opeartion is nil, index:" + i);
				continue;
			}
			
			operation.pipelineExcute(p);
		}
		p.sync();
	}
}
