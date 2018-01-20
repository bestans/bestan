package bestan.common.dao;

import java.util.HashMap;
import java.util.Map;

import bestan.common.cachecommand.CacheCommonDAO;

/**
 * <p>Keeps track of the DAOs (data access objects) to use. They are registered 
 * using their class name:<br>
 * <code>register(AccountDAO.class, new CharacterDAO());</code></p>
 */
public class DAORegister {

	private Map<Class<?>, Object> register = new HashMap<Class<?>, Object>();
	private static DAORegister instance;

	private DAORegister() {
		// hide constructor, this is a Singleton
	}

	/**
	 * gets the singleton DAORegister instance
	 * 因为多线程使用所以在系统初始化时就需要初始化
	 *
	 * @return DAORegister
	 */
	public static DAORegister get() {
		if (instance == null) {
			instance = new DAORegister();
			instance.registerDAOs();
		}
		return instance;
	}

	/**
	 * 初始化
	 * @param agent
	 * @return
	 */
	public boolean init(IDAORegisterAgent agent) {
		if(null != agent) {
			agent.registerDAOs();
		}

		return true;
	}
	
	/**
	 * registers a DAO
	 *
	 * @param <T>   type of DOA
	 * @param clazz class of DOA
	 * @param object instance of DOA
	 */
	public <T> void register(Class<T> clazz, T object) {
		register.put(clazz, object);
	}

	/**
	 * gets the instance for the requested DAO
	 *
	 * @param <T>   type of DAO
	 * @param clazz class of DAP
	 * @return instance of DOA
	 * @throws IllegalArgumentException in case there is no instance registered for the specified class
	 */
	@SuppressWarnings("unchecked")
	public <T> T get(Class<T> clazz) throws IllegalArgumentException {
		T res = (T) register.get(clazz);
		if (res == null) {
			throw new IllegalArgumentException("No DAO registered for class " + clazz);
		}
		return res;
	}


	/**
	 * 注册所有数据访问对象
	 */
	private void registerDAOs() {
		register(CacheCommonDAO.class, new CacheCommonDAO());
	}
}
