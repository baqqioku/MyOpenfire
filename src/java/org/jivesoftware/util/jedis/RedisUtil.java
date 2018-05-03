package org.jivesoftware.util.jedis;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.XMLProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public final class RedisUtil {

	private RedisUtil() {
	}
	private static final Logger LOGGER = LoggerFactory.getLogger(RedisUtil.class);
	private static final Jedis UNAVAILABLE_JEDIS = new Jedis();
	private static String HOST_NAME = "host";
	private static String PORT_NAME = "port";
	private static String AUTH_NAME = "auth";
	private static String MAX_ACTIVE_NAME = "max-active";
	private static int MAX_ACTIVE_DEFAULT = 1024;
	private static String MAX_IDLE_NAME = "max-idle";
	private static int MAX_IDLE_DEFAULT = 200;
	private static String MAX_WAIT_NAME = "max-wait";
	private static long MAX_WAIT_DEFAULT = 10000L;
	private static String TIMEOUT_NAME = "timeout";
	private static int TIMEOUT_DEFAULT = 10000;
	private static String  TEST_ON_BORROW_NAME = "test-on-borrow";
	private static boolean TEST_ON_BORROW_DEFAULT = true;

	private static JedisPool jedisPool = null;

	private static final String REDIS_CONFIG_FILENAME = "conf" + File.separator + "redis.xml";


	static {
		try {
			XMLProperties properties = loadRedisConfig();
			if (properties == null) {
				jedisPool = null;
			} else {
				JedisPoolConfig config = createJedisPoolConfig(properties);
				jedisPool = createJedisPool(properties, config);
			}
		} catch (Exception e) {
			LOGGER.error("initialize jedisPoll failed!", e);
			jedisPool = null;
		}
	}

	/**
	 * 
	 * @return
	 */
	private static XMLProperties loadRedisConfig() {
		String openfireHome = JiveGlobals.getHomeDirectory();
		String redisConfigPath = openfireHome + File.separator + REDIS_CONFIG_FILENAME;
		LOGGER.info("redis config path : {}", redisConfigPath);
		try {
			XMLProperties properties = new XMLProperties(redisConfigPath);
			return properties;
		} catch (IOException e) {
			LOGGER.error("load redis config failed!", e);
			return null;
		}
	}

	/**
	 * 
	 * @param properties
	 * @return
	 */
	private static JedisPoolConfig createJedisPoolConfig(XMLProperties properties) {
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxTotal(NumberUtils.toInt(properties.getProperty(MAX_ACTIVE_NAME).trim(), MAX_ACTIVE_DEFAULT));
		config.setMaxIdle(NumberUtils.toInt(properties.getProperty(MAX_IDLE_NAME).trim(), MAX_IDLE_DEFAULT));
		config.setMaxWaitMillis(NumberUtils.toLong(properties.getProperty(MAX_WAIT_NAME), MAX_WAIT_DEFAULT));
		config.setTestOnBorrow(properties.getProperty(TEST_ON_BORROW_NAME) == null ? TEST_ON_BORROW_DEFAULT : BooleanUtils.toBoolean(properties.getProperty(TEST_ON_BORROW_NAME).trim()));
		return config;
	}

	/**
	 * 
	 * @param properties
	 * @param config
	 * @return
	 */
	private static JedisPool createJedisPool(XMLProperties properties, JedisPoolConfig config) {
		//Redis服务器IP
		String host = properties.getProperty(HOST_NAME).trim();
		//Redis的端口号
		int port = NumberUtils.toInt(properties.getProperty(PORT_NAME).trim());
		//访问密码
		String auth = properties.getProperty(AUTH_NAME).trim();
		int timeout = NumberUtils.toInt(properties.getProperty(TIMEOUT_NAME), TIMEOUT_DEFAULT);

		if (StringUtils.isBlank(host) || port <= 0 || StringUtils.isBlank(auth)) {
			LOGGER.error("createJedisPool failed of host : {},port : {},auth : {}", host, port, auth);
			return null;
		} else {
			return new JedisPool(config, host, port, timeout, auth);
		}
	}

	/**
	 *获取Jedis实例
	 * 
	 * @return
	 */
	public synchronized static Jedis getJedis() {
		try {
			if (jedisPool != null) {
				Jedis resource = jedisPool.getResource();
				return resource;
			} else {
				return UNAVAILABLE_JEDIS;
			}
		} catch (Exception e) {
			LOGGER.error("getJedis failed!", e);
			return UNAVAILABLE_JEDIS;
		}
	}

	/**
	 *校验当前jedis是否可用,本校验不能保证jedis连接可用
	 * @param jedis
	 * @return
	 */
	public static boolean checkJedisAvailable(Jedis jedis) {
		return jedis != null && jedis != UNAVAILABLE_JEDIS;
	}

	/**
	 *释放jedis资源
	 * @param jedis
	 */
	public static void returnResource(final Jedis jedis) {
		if (checkJedisAvailable(jedis)) {
			jedis.close();
		}
	}

}