package bestan.common.redis;

import bestan.common.config.RedisConfig;
import bestan.common.log.LogManager;
import bestan.common.log.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/** 
 * Redis工具类,用于获取JedisPool. 
 * 参考官网说明如下： 
 * You shouldn't use the same instance from different threads because you'll have strange errors. 
 * And sometimes creating lots of Jedis instances is not good enough because it means lots of sockets and connections, 
 * which leads to strange errors as well. A single Jedis instance is not threadsafe! 
 * To avoid these problems, you should use JedisPool, which is a threadsafe pool of network connections. 
 * This way you can overcome those strange errors and achieve great performance. 
 * To use it, init a pool: 
 *  JedisPool pool = new JedisPool(new JedisPoolConfig(), "localhost"); 
 *  You can store the pool somewhere statically, it is thread-safe. 
 *  JedisPoolConfig includes a number of helpful Redis-specific connection pooling defaults. 
 *  For example, Jedis with JedisPoolConfig will close a connection after 300 seconds if it has not been returned. 
 */  
public class JedisUtil  {  
	private static final Logger logger = LogManager.getLogger(JedisUtil.class);  
      
    /** 
     * 私有构造器. 
     */  
    private JedisUtil() {  
    	initialPool();
    }  
    
    private JedisPool pool  = null;  
      
    /**
     * 初始化jedisPool
     */
    private void initialPool() {
        String host = RedisConfig.getInstance().ip;
        int port = RedisConfig.getInstance().port;

        try {
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxIdle(RedisConfig.getInstance().pool_max_idle);
            config.setMaxWaitMillis(RedisConfig.getInstance().pool_max_wait);
            config.setTestOnBorrow(RedisConfig.getInstance().pool_test_on_borrow);
            int timeout = RedisConfig.getInstance().timeOut;

            pool = new JedisPool(config, host, port, timeout, RedisConfig.getInstance().passwd, RedisConfig.getInstance().database);
        } catch (Exception e) {
        	logger.error("initialPool-failed:" + host + ":" + port, e);
        }
    }
  
    /** 
     *类级的内部类，也就是静态的成员式内部类，该内部类的实例与外部类的实例 
     *没有绑定关系，而且只有被调用到时才会装载，从而实现了延迟加载。 
     */  
    private static class JedisUtilHolder{  
        /** 
         * 静态初始化器，由JVM来保证线程安全 
         */  
        private static JedisUtil instance = new JedisUtil();  
    }  
  
    /** 
     *当getInstance方法第一次被调用的时候，它第一次读取 
     *RedisUtilHolder.instance，导致类得到初RedisUtilHolder始化；而这个类在装载并被初始化的时候，会初始化它的静 
     *态域，从而创建RedisUtil的实例，由于是静态的域，因此只会在虚拟机装载类的时候初始化一次，并由虚拟机来保证它的线程安全性。 
     *这个模式的优势在于，getInstance方法并没有被同步，并且只是执行一个域的访问，因此延迟初始化并没有增加任何访问成本。 
     */  
    public static JedisUtil getInstance() {  
        return JedisUtilHolder.instance;  
    }  
      
    /** 
     * 获取Redis实例. 
     * @return Redis工具类实例 
     */  
    public Jedis getJedis() {  
        Jedis jedis  = null;  
        int count =0;  
        do{  
            try{   
                jedis = pool.getResource();   
            } catch (Exception e) {  
                logger.error("get redis master1 failed!", e);  
                 // 销毁对象    
                pool.returnBrokenResource(jedis);    
            }  
            count++;  
        } while(jedis == null && count < 3);   //count<BaseConfig.getRetryNum()
        
        return jedis;  
    }   
    
    public JedisPool getPool() {
    	return pool;
    }
    
    public void returnBrokenResource(Jedis jedis) {
    	if(null == pool || null == jedis) {
    		return;
    	}
    	
    	pool.returnBrokenResource(jedis);
    }
    
    public void returnResource(Jedis jedis) {
    	if(null == pool || null == jedis) {
    		return;
    	}
    	
    	pool.returnResource(jedis);
    }
    
    public void destory() {
    	if(null == pool) {
    		return;
    	}
    	
    	pool.destroy();
    }
}  
