package bestan.common.redis;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSON;
import com.google.protobuf.Message;

import bestan.common.net.message.MessageFactory;
import bestan.common.util.Global;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.BinaryClient.LIST_POSITION;
import redis.clients.jedis.Tuple;

public class RedisUtil {
	//===============================================================================
	// string
	/**
	 * <p>通过key获取储存在redis中的value</p>
	 * <p>并释放连接</p>
	 * @param key
	 * @return 成功返回value 失败返回null
	 */
	public static String get(String key) {
		Jedis jedis = null;
		String value = null;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			value = jedis.get(key);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}

		return value;
	}

	/**
	 * <p>向redis存入key和value,并释放连接资源</p>
	 * <p>如果key已经存在 则覆盖</p>
	 * @param key
	 * @param value
	 * @return 成功 返回OK 失败返回 0
	 */
	public static String set(String key, String value) {
		Jedis jedis = null;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			return jedis.set(key, value);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
			return "0";
		} finally {
			returnResource(jedis);
		}
	}

	/**
	 * <p>删除指定的key,也可以传入一个包含key的数组</p>
	 * @param keys 一个key  也可以使 string 数组
	 * @return 返回删除成功的个数 
	 */
	public static long del(String... keys) {
		Jedis jedis = null;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			return jedis.del(keys);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
			return 0L;
		} finally {
			returnResource(jedis);
		}
	}

	/**
	 * <p>通过key向指定的value值追加值</p>
	 * @param key 
	 * @param str 
	 * @return 成功返回 添加后value的长度 失败 返回 添加的 value 的长度  异常返回0L
	 */
	public static long append(String key, String str) {
		Jedis jedis = null;
		long res = 0L;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.append(key, str);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}

		return res;
	}

	/**
	 * <p>判断key是否存在</p>
	 * @param key
	 * @return true OR false
	 */
	public static boolean exists(String key) {
		Jedis jedis = null;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			return jedis.exists(key);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
			return false;
		} finally {
			returnResource(jedis);
		}
	}

	/**
	 * <p>设置key value,如果key已经存在则返回0,nx==> not exist</p>
	 * @param key
	 * @param value
	 * @return 成功返回1 如果存在 和 发生异常 返回 0
	 */
	public static long setnx(String key, String value) {
		Jedis jedis = null;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			return jedis.setnx(key, value);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
			return 0L;
		} finally {
			returnResource(jedis);
		}
	}

	/**
	 * <p>设置key value并制定这个键值的有效期</p>
	 * @param key
	 * @param value
	 * @param seconds 单位:秒
	 * @return 成功返回OK 失败和异常返回null
	 */
	public static String setex(String key, String value, int seconds) {
		Jedis jedis = null;
		String res = null;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.setex(key, seconds, value);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}

		return res;
	}

	/**
	 * <p>通过key 和offset 从指定的位置开始将原先value替换</p>
	 * <p>下标从0开始,offset表示从offset下标开始替换</p>
	 * <p>如果替换的字符串长度过小则会这样</p>
	 * <p>example:</p>
	 * <p>value : bigsea@zto.cn</p>
	 * <p>str : abc </p>
	 * <P>从下标7开始替换  则结果为</p>
	 * <p>RES : bigsea.abc.cn</p>
	 * @param key
	 * @param str
	 * @param offset 下标位置
	 * @return 返回替换后  value 的长度
	 */
	public static long setrange(String key, String str, int offset) {
		Jedis jedis = null;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			return jedis.setrange(key, offset, str);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
			return 0L;
		} finally {
			returnResource(jedis);
		}
	}

	/**
	 * <p>通过批量的key获取批量的value</p>
	 * @param keys string数组 也可以是一个key
	 * @return 成功返回value的集合, 失败返回null的集合 ,异常返回空
	 */
	public static List<String> mget(String... keys) {
		Jedis jedis = null;
		List<String> values = null;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			values = jedis.mget(keys);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return values;
	}

	/**
	 * <p>批量的设置key:value,可以一个</p>
	 * <p>example:</p>
	 * <p>  obj.mset(new String[]{"key2","value1","key2","value2"})</p>
	 * @param keysvalues
	 * @return 成功返回OK 失败 异常 返回 null
	 * 
	 */
	public static String mset(String... keysvalues) {
		Jedis jedis = null;
		String res = null;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.mset(keysvalues);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}

	/**
	 * <p>批量的设置key:value,可以一个,如果key已经存在则会失败,操作会回滚</p>
	 * <p>example:</p>
	 * <p>  obj.msetnx(new String[]{"key2","value1","key2","value2"})</p>
	 * @param keysvalues 
	 * @return 成功返回1 失败返回0 
	 */
	public static long msetnx(String... keysvalues) {
		Jedis jedis = null;
		long res = 0L;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.msetnx(keysvalues);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}

	/**
	 * <p>设置key的值,并返回一个旧值</p>
	 * @param key
	 * @param value
	 * @return 旧值 如果key不存在 则返回null
	 */
	public static String getset(String key, String value) {
		Jedis jedis = null;
		String res = null;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.getSet(key, value);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}

	/**
	 * <p>通过下标 和key 获取指定下标位置的 value</p>
	 * @param key
	 * @param startOffset 开始位置 从0 开始 负数表示从右边开始截取
	 * @param endOffset 
	 * @return 如果没有返回null 
	 */
	public static String getrange(String key, int startOffset, int endOffset) {
		Jedis jedis = null;
		String res = null;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.getrange(key, startOffset, endOffset);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}

	/**
	 * <p>通过key 对value进行加值+1操作,当value不是int类型时会返回错误,当key不存在是则value为1</p>
	 * @param key
	 * @return 加值后的结果
	 */
	public static long incr(String key) {
		Jedis jedis = null;
		long res = 0L;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.incr(key);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}

	/**
	 * <p>通过key给指定的value加值,如果key不存在,则这是value为该值</p>
	 * @param key
	 * @param integer
	 * @return
	 */
	public static long incrBy(String key, long integer) {
		Jedis jedis = null;
		long res = 0L;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.incrBy(key, integer);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}

	/**
	 * <p>对key的值做减减操作,如果key不存在,则设置key为-1</p>
	 * @param key
	 * @return
	 */
	public static long decr(String key) {
		Jedis jedis = null;
		long res = 0L;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.decr(key);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}

	/**
	 * <p>减去指定的值</p>
	 * @param key
	 * @param integer
	 * @return
	 */
	public static long decrBy(String key, long integer) {
		Jedis jedis = null;
		long res = 0L;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.decrBy(key, integer);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}

	/**
	 * <p>通过key获取value值的长度</p>
	 * @param key
	 * @return 失败返回null 
	 */
	public static long serlen(String key) {
		Jedis jedis = null;
		long res = 0L;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.strlen(key);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}
	//===============================================================================

	//===============================================================================
	// hash -> map
	/**
	 * <p>通过key给field设置指定的值,如果key不存在,则先创建</p>
	 * @param key
	 * @param field 字段
	 * @param value
	 * @return 如果存在返回0 异常返回null
	 */
	public static long hset(String key, String field, String value) {
		Jedis jedis = null;
		long res = 0L;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.hset(key, field, value);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}

	/**
	 * <p>通过key给field设置指定的值,如果key不存在则先创建,如果field已经存在,返回0</p>
	 * @param key
	 * @param field
	 * @param value
	 * @return
	 */
	public static long hsetnx(String key, String field, String value) {
		Jedis jedis = null;
		long res = 0L;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.hsetnx(key, field, value);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}

	/**
	 * <p>通过key同时设置 hash的多个field</p>
	 * @param key
	 * @param hash
	 * @return 返回OK 异常返回null
	 */
	public static String hmset(String key, Map<String, String> hash) {
		Jedis jedis = null;
		String res = null;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.hmset(key, hash);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}

	/**
	 * <p>通过key 和 field 获取指定的 value</p>
	 * @param key
	 * @param field
	 * @return 没有返回null
	 */
	public static String hget(String key, String field) {
		Jedis jedis = null;
		String res = null;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.hget(key, field);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}

	/**
	 * <p>通过key 和 fields 获取指定的value 如果没有对应的value则返回null</p>
	 * @param key
	 * @param fields 可以使 一个String 也可以是 String数组
	 * @return 
	 */
	public static List<String> hmget(String key, String... fields) {
		Jedis jedis = null;
		List<String> res = null;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.hmget(key, fields);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}

	/**
	 * <p>通过key给指定的field的value加上给定的值</p>
	 * @param key
	 * @param field
	 * @param value 
	 * @return
	 */
	public static long hincrby(String key, String field, long value) {
		Jedis jedis = null;
		long res = 0L;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.hincrBy(key, field, value);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}

	/**
	 * <p>通过key和field判断是否有指定的value存在</p>
	 * @param key
	 * @param field
	 * @return
	 */
	public static boolean hexists(String key, String field) {
		Jedis jedis = null;
		boolean res = false;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.hexists(key, field);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}

	/**
	 * <p>通过key返回field的数量</p>
	 * @param key
	 * @return
	 */
	public static long hlen(String key) {
		Jedis jedis = null;
		long res = 0L;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.hlen(key);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;

	}

	/**
	 * <p>通过key 删除指定的 field </p>
	 * @param key
	 * @param fields 可以是 一个 field 也可以是 一个数组
	 * @return
	 */
	public static long hdel(String key, String... fields) {
		Jedis jedis = null;
		long res = 0L;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.hdel(key, fields);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}

	/**
	 * <p>通过key返回所有的field</p>
	 * @param key
	 * @return
	 */
	public static Set<String> hkeys(String key) {
		Jedis jedis = null;
		Set<String> res = null;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.hkeys(key);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}

	/**
	 * <p>通过key返回所有和key有关的value</p>
	 * @param key
	 * @return
	 */
	public static List<String> hvals(String key) {
		Jedis jedis = null;
		List<String> res = null;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.hvals(key);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}

	/**
	 * <p>通过key获取所有的field和value</p>
	 * @param key
	 * @return
	 */
	public static Map<String, String> hgetall(String key) {
		Jedis jedis = null;
		Map<String, String> res = null;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.hgetAll(key);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}

	/**
	 * <p>通过key向list头部添加字符串</p>
	 * @param key
	 * @param strs 可以使一个string 也可以使string数组
	 * @return 返回list的value个数
	 */
	public static long lpush(String key, String... strs) {
		Jedis jedis = null;
		long res = 0L;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.lpush(key, strs);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}

	/**
	 * <p>通过key向list尾部添加字符串</p>
	 * @param key
	 * @param strs 可以使一个string 也可以使string数组
	 * @return 返回list的value个数
	 */
	public static long rpush(String key, String... strs) {
		Jedis jedis = null;
		long res = 0L;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.rpush(key, strs);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}
	//===============================================================================
	
	//===============================================================================
	// list
	/**
	 * <p>通过key在list指定的位置之前或者之后 添加字符串元素</p>
	 * @param key 
	 * @param where LIST_POSITION枚举类型
	 * @param pivot list里面的value
	 * @param value 添加的value
	 * @return
	 */
	public static long linsert(String key, LIST_POSITION where, String pivot, String value) {
		Jedis jedis = null;
		long res = 0L;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.linsert(key, where, pivot, value);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}

	/**
	 * <p>通过key设置list指定下标位置的value</p>
	 * <p>如果下标超过list里面value的个数则报错</p>
	 * @param key 
	 * @param index 从0开始
	 * @param value
	 * @return 成功返回OK
	 */
	public static String lset(String key, Long index, String value) {
		Jedis jedis = null;
		String res = null;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.lset(key, index, value);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}

	/**
	 * <p>通过key从对应的list中删除指定的count个 和 value相同的元素</p>
	 * @param key 
	 * @param count 当count为0时删除全部
	 * @param value 
	 * @return 返回被删除的个数
	 */
	public static long lrem(String key, long count, String value) {
		Jedis jedis = null;
		long res = 0L;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.lrem(key, count, value);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}

	/**
	 * <p>通过key保留list中从strat下标开始到end下标结束的value值</p>
	 * @param key
	 * @param start
	 * @param end
	 * @return 成功返回OK
	 */
	public static String ltrim(String key, long start, long end) {
		Jedis jedis = null;
		String res = null;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.ltrim(key, start, end);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}

	/**
	 * <p>通过key从list的头部删除一个value,并返回该value</p>
	 * @param key
	 * @return 
	 */
	public static String lpop(String key) {
		Jedis jedis = null;
		String res = null;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.lpop(key);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}

	/**
	 * <p>通过key从list尾部删除一个value,并返回该元素</p>
	 * @param key
	 * @return
	 */
	public static String rpop(String key) {
		Jedis jedis = null;
		String res = null;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.rpop(key);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}

	/**
	 * <p>通过key从一个list的尾部删除一个value并添加到另一个list的头部,并返回该value</p>
	 * <p>如果第一个list为空或者不存在则返回null</p>
	 * @param srckey
	 * @param dstkey
	 * @return
	 */
	public static String rpoplpush(String srckey, String dstkey) {
		Jedis jedis = null;
		String res = null;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.rpoplpush(srckey, dstkey);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}

	/**
	 * <p>通过key获取list中指定下标位置的value</p>
	 * @param key
	 * @param index
	 * @return 如果没有返回null
	 */
	public static String lindex(String key, long index) {
		Jedis jedis = null;
		String res = null;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.lindex(key, index);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}

	/**
	 * <p>通过key返回list的长度</p>
	 * @param key
	 * @return
	 */
	public static long llen(String key) {
		Jedis jedis = null;
		long res = Global.INVALID_VALUE;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.llen(key);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}

	/**
	 * <p>通过key获取list指定下标位置的value</p>
	 * <p>如果start 为 0 end 为 -1 则返回全部的list中的value</p>
	 * @param key 
	 * @param start
	 * @param end
	 * @return
	 */
	public static List<String> lrange(String key, long start, long end) {
		Jedis jedis = null;
		List<String> res = null;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.lrange(key, start, end);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}
	//===============================================================================

	//===============================================================================
	// set
	/**
	 * <p>通过key向指定的set中添加value</p>
	 * @param key
	 * @param members 可以是一个String 也可以是一个String数组
	 * @return 添加成功的个数
	 */
	public static long sadd(String key, String... members) {
		Jedis jedis = null;
		long res = 0L;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.sadd(key, members);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}

	/**
	 * <p>通过key删除set中对应的value值</p>
	 * @param key
	 * @param members 可以是一个String 也可以是一个String数组
	 * @return 删除的个数
	 */
	public static long srem(String key, String... members) {
		Jedis jedis = null;
		long res = 0L;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.srem(key, members);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}

	/**
	 * <p>通过key随机删除一个set中的value并返回该值</p>
	 * @param key
	 * @return
	 */
	public static String spop(String key) {
		Jedis jedis = null;
		String res = null;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.spop(key);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}

	/**
	 * <p>通过key获取set中的差集</p>
	 * <p>以第一个set为标准</p>
	 * @param keys 可以使一个string 则返回set中所有的value 也可以是string数组
	 * @return 
	 */
	public static Set<String> sdiff(String... keys) {
		Jedis jedis = null;
		Set<String> res = null;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.sdiff(keys);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}

	/**
	 * <p>通过key获取set中的差集并存入到另一个key中</p>
	 * <p>以第一个set为标准</p>
	 * @param dstkey 差集存入的key
	 * @param keys 可以使一个string 则返回set中所有的value 也可以是string数组
	 * @return 
	 */
	public static long sdiffstore(String dstkey, String... keys) {
		Jedis jedis = null;
		long res = Global.INVALID_VALUE;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.sdiffstore(dstkey, keys);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}

	/**
	 * <p>通过key获取指定set中的交集</p>
	 * @param keys 可以使一个string 也可以是一个string数组
	 * @return
	 */
	public static Set<String> sinter(String... keys) {
		Jedis jedis = null;
		Set<String> res = null;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.sinter(keys);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}

	/**
	 * <p>通过key获取指定set中的交集 并将结果存入新的set中</p>
	 * @param dstkey
	 * @param keys 可以使一个string 也可以是一个string数组
	 * @return
	 */
	public static long sinterstore(String dstkey, String... keys) {
		Jedis jedis = null;
		long res = Global.INVALID_VALUE;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.sinterstore(dstkey, keys);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}

	/**
	 * <p>通过key返回所有set的并集</p>
	 * @param keys 可以使一个string 也可以是一个string数组
	 * @return
	 */
	public static Set<String> sunion(String... keys) {
		Jedis jedis = null;
		Set<String> res = null;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.sunion(keys);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}

	/**
	 * <p>通过key返回所有set的并集,并存入到新的set中</p>
	 * @param dstkey 
	 * @param keys 可以使一个string 也可以是一个string数组
	 * @return
	 */
	public static long sunionstore(String dstkey, String... keys) {
		Jedis jedis = null;
		long res = Global.INVALID_VALUE;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.sunionstore(dstkey, keys);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}

	/**
	 * <p>通过key将set中的value移除并添加到第二个set中</p>
	 * @param srckey 需要移除的
	 * @param dstkey 添加的
	 * @param member set中的value
	 * @return
	 */
	public static long smove(String srckey, String dstkey, String member) {
		Jedis jedis = null;
		long res = 0L;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.smove(srckey, dstkey, member);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}

	/**
	 * <p>通过key获取set中value的个数</p>
	 * @param key
	 * @return
	 */
	public static long scard(String key) {
		Jedis jedis = null;
		long res = Global.INVALID_VALUE;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.scard(key);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}

	/**
	 * <p>通过key判断value是否是set中的元素</p>
	 * @param key
	 * @param member
	 * @return
	 */
	public static boolean sismember(String key, String member) {
		Jedis jedis = null;
		boolean res = false;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.sismember(key, member);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}

	/**
	 * <p>通过key获取set中随机的value,不删除元素</p>
	 * @param key
	 * @return
	 */
	public static String srandmember(String key) {
		Jedis jedis = null;
		String res = null;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.srandmember(key);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}

	/**
	 * <p>通过key获取set中所有的value</p>
	 * @param key
	 * @return
	 */
	public static Set<String> smembers(String key) {
		Jedis jedis = null;
		Set<String> res = null;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.smembers(key);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}
	//===============================================================================
	
	//===============================================================================
	// sorted set -> skip list
	/**
	 * <p>通过key向zset中添加value,score,其中score就是用来排序的</p>
	 * <p>如果该value已经存在则根据score更新元素</p>
	 * @param key
	 * @param scoreMembers 
	 * @return
	 */
	public static long zadd(String key, Map<String, Double> scoreMembers) {
		Jedis jedis = null;
		long res = Global.INVALID_VALUE;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.zadd(key, scoreMembers);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}

	/**
	 * <p>通过key向zset中添加value,score,其中score就是用来排序的</p>
	 * <p>如果该value已经存在则根据score更新元素</p>
	 * @param key
	 * @param score
	 * @param member
	 * @return
	 */
	public static long zadd(String key, double score, String member) {
		Jedis jedis = null;
		long res = Global.INVALID_VALUE;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.zadd(key, score, member);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}

	/**
	 * <p>通过key删除在zset中指定的value</p>
	 * @param key
	 * @param members string
	 * @return
	 */
	public static long zremOne(String key, String member) {
		String[] tmpArr = new String[1];
		tmpArr[0] = member;
		return zrem(key, tmpArr);
	}
	
	/**
	 * <p>通过key删除在zset中指定的value</p>
	 * @param key
	 * @param members 可以使一个string 也可以是一个string数组
	 * @return
	 */
	public static long zrem(String key, String... members) {
		Jedis jedis = null;
		long res = Global.INVALID_VALUE;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.zrem(key, members);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}

	/**
	 * <p>通过key增加该zset中value的score的值</p>
	 * @param key
	 * @param score 
	 * @param member 
	 * @return
	 */
	public static double zincrby(String key, double score, String member) {
		Jedis jedis = null;
		double res = 0F;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.zincrby(key, score, member);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}

	/**
	 * <p>通过key返回zset中value的排名</p>
	 * <p>下标从小到大排序</p>
	 * @param key
	 * @param member
	 * @return
	 */
	public static long zrank(String key, String member) {
		Jedis jedis = null;
		long res = Global.INVALID_VALUE;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.zrank(key, member);
		} catch (Exception e) {
//			returnBrokenResource(jedis);
//			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}

	/**
	 * <p>通过key返回zset中value的排名</p>
	 * <p>下标从大到小排序</p>
	 * @param key
	 * @param member
	 * @return
	 */
	public static long zrevrank(String key, String member) {
		Jedis jedis = null;
		long res = Global.INVALID_VALUE;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.zrevrank(key, member);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}

	/**
	 * <p>通过key将获取score从start到end中zset的value</p>
	 * <p>socre从大到小排序</p>
	 * <p>当start为0 end为-1时返回全部</p>
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 */
	public static Set<String> zrevrange(String key, long start, long end) {
		Jedis jedis = null;
		Set<String> res = null;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.zrevrange(key, start, end);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}
	
	/**
	 * <p>通过key将获取score从start到end中zset的value</p>
	 * <p>socre从大到小排序</p>
	 * <p>当start为0 end为-1时返回全部</p>
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 */
	public static Set<Tuple> zrevrangeWithScore(String key, long start, long end) {
		Jedis jedis = null;
		Set<Tuple> res = null;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.zrevrangeWithScores(key, start, end);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}
	
	/**
	 * <p>通过key将获取score从start到end中zset的value</p>
	 * <p>socre从大到小排序</p>
	 * <p>当start为0 end为-1时返回全部</p>
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 */
	public static Set<Tuple> zrangeWithScore(String key, long start, long end) {
		Jedis jedis = null;
		Set<Tuple> res = null;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.zrangeWithScores(key, start, end);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}
	
	/**
	 * <p>通过key将获取score从start到end中zset的value</p>
	 * <p>socre从大到小排序</p>
	 * <p>当start为0 end为-1时返回全部</p>
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 */
	public static Set<String> zrange(String key, long start, long end) {
		Jedis jedis = null;
		Set<String> res = null;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.zrange(key, start, end);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}

	/**
	 * <p>通过key返回指定score内zset中的value</p>
	 * @param key 
	 * @param max 
	 * @param min 
	 * @return
	 */
	public static Set<String> zrangebyscore(String key, String max, String min) {
		Jedis jedis = null;
		Set<String> res = null;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.zrevrangeByScore(key, max, min);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}

	/**
	 * <p>通过key返回指定score内zset中的value</p>
	 * @param key 
	 * @param max  
	 * @param min 
	 * @return
	 */
	public static Set<String> zrangeByScore(String key, double max, double min) {
		Jedis jedis = null;
		Set<String> res = null;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.zrevrangeByScore(key, max, min);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}
	
	/**
	 * <p>通过key返回指定score内zset中的value</p>
	 * @param key 
	 * @param max  
	 * @param min 
	 * @return
	 */
	public static Set<String> zrangeByScore(String key, double max, double min, int offset, int count) {
		Jedis jedis = null;
		Set<String> res = null;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.zrevrangeByScore(key, max, min, offset, count);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}

	/**
	 * <p>返回指定区间内zset中value的数量</p>
	 * @param key
	 * @param min
	 * @param max
	 * @return
	 */
	public static long zcount(String key, double min, double max) {
		Jedis jedis = null;
		long res = Global.INVALID_VALUE;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.zcount(key, min, max);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}

	/**
	 * <p>通过key返回zset中的value个数</p>
	 * @param key
	 * @return
	 */
	public static long zcard(String key) {
		Jedis jedis = null;
		long res = Global.INVALID_VALUE;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.zcard(key);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}

	/**
	 * <p>通过key获取zset中value的score值</p>
	 * @param key
	 * @param member
	 * @return
	 */
	public static double zscore(String key, String member) {
		Jedis jedis = null;
		double res = 0.0F;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.zscore(key, member);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}

	/**
	 * <p>通过key删除给定区间内的元素</p>
	 * @param key 
	 * @param start 
	 * @param end
	 * @return
	 */
	public static long zremrangeByRank(String key, long start, long end) {
		Jedis jedis = null;
		long res = Global.INVALID_VALUE;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.zremrangeByRank(key, start, end);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}

	/**
	 * <p>通过key删除指定score内的元素</p>
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 */
	public static long zremrangeByScore(String key, double start, double end) {
		Jedis jedis = null;
		long res = Global.INVALID_VALUE;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.zremrangeByScore(key, start, end);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}
	//===============================================================================
	
	//===============================================================================
	// key
	/**
	 * <p>返回满足pattern表达式的所有key</p>
	 * <p>keys(*)</p>
	 * <p>返回所有的key</p>
	 * @param pattern
	 * @return
	 */
	public static Set<String> keys(String pattern) {
		Jedis jedis = null;
		Set<String> res = null;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.keys(pattern);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}

	/**
	 * <p>通过key判断值得类型</p>
	 * @param key
	 * @return
	 */
	public static String type(String key) {
		Jedis jedis = null;
		String res = null;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			res = jedis.type(key);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return res;
	}
	//===============================================================================
	
	/**
	 * <p>返还到连接池</p>
	 * 
	 * @param pool
	 * @param redis
	 */
	public static void returnResource(Jedis jedis) {
		if (jedis != null) {
			JedisUtil.getInstance().returnResource(jedis);
		}
	}
	
	/**
	 * <p>释放redis对象</p>
	 * 
	 * @param pool
	 * @param redis
	 */
	public static void returnBrokenResource(Jedis jedis) {
		if (jedis != null) {
			JedisUtil.getInstance().returnBrokenResource(jedis);
		}
	}
	
	/**
	 * <p>密码验证</p>
	 * @param authToken
	 */
	public static void auth(String authToken) {
		if (authToken == null) {
			return;
        }
		
		Jedis jedis = null;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			jedis.auth(authToken);
		} catch (Exception e) {
			returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
    }
	
	/**
	 * <p>ttl</p>
	 * @param authToken
	 */
	public static long ttl(String key) {
        Jedis jedis = null; 
        try {
        	jedis = JedisUtil.getInstance().getJedis();
            return jedis.ttl(key);
        } catch (Exception e) {
        	returnBrokenResource(jedis);
            return Global.INVALID_VALUE;
        } finally {
            returnResource(jedis);
        }
    }
	
	/**
	 * 清空缓存 请慎用
	 */
	public static void flushDB() {
		Jedis jedis = null; 
        try {
        	jedis = JedisUtil.getInstance().getJedis();
            jedis.flushDB();
        } catch (Exception e) {
        	returnBrokenResource(jedis);
            return ;
        } finally {
            returnResource(jedis);
        }
	}
	
	/**
     * set key object（使用fastjson序列化成String保存）
     *
     * @param key
     * @param object
     */
    public static void setJSONObject(String key, Object object) {
        set(key, JSON.toJSONString(object));
    }

    /**
     * get key，获得object序列化的值再使用fastjson进行反序列化，进而返回clazz类型的对象
     *
     * @param key
     * @param clazz
     * @return
     */
    public static <T> T getJSONObject(String key, Class<T> clazz) {
        return JSON.parseObject(get(key), clazz);
    }
    
    /**
     * set key proto obj 
     * 
     * @param key
     * @param obj
     */
    public static void setProtoObject(String key, Message obj) {
    	set(key, obj.toByteArray().toString());
    }
    
    /**
     * get proto object
     * 
     * @param key
     * @param clazz
     * @return
     */
    public static Message getProtoObject(String key, Class<?> clazz) {
    	int msgId = MessageFactory.getFactory().getMsgIdByMessageClass(clazz);
    	if(Global.INVALID_VALUE == msgId) {
    		return null;
    	}
    	
    	try {
			return MessageFactory.getFactory().getMessage(msgId, bestan.common.util.Base64.decodeBase64(key.getBytes()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
    }
}
