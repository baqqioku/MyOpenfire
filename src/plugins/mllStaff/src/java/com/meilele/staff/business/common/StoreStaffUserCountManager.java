package com.meilele.staff.business.common;

import com.meilele.staff.business.staff.StaffInfo;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.jivesoftware.util.jedis.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

public class StoreStaffUserCountManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(StoreStaffUserCountManager.class);
	private static final String STAFF_USER_COUNT_CACHE_PREFIX = "staff_user_count_";
	private static final String STORE_LOCK_CACHE_PREFIX = "store_lock_";
	private static final String STORE_LOCK_CACHE_VALUE = "TRUE";

	public static void addOneStaffUserCount(String staffUUID) {
		Jedis jedis = RedisUtil.getJedis();
		if (RedisUtil.checkJedisAvailable(jedis)) {
			try {
				long result = jedis.incr("staff_user_count_" + staffUUID).longValue();
				LOGGER.info("staff : {} add a user,result count :{}.", staffUUID, Long.valueOf(result));
			} finally {
				RedisUtil.returnResource(jedis);
			}
		} else {
			throw new IllegalStateException("jedis is unavaliable!");
		}
	}

	public static boolean lockAStore(String storeUUID) {
		Jedis jedis = RedisUtil.getJedis();
		if (RedisUtil.checkJedisAvailable(jedis)) {
			try {
				for (;;) {
					if (jedis.setnx("store_lock_" + storeUUID, "TRUE").longValue() == 1L) {
						return true;
					}
					Thread.sleep(3L, RandomUtils.nextInt(500));
				}
			} catch (Exception e) {
				LOGGER.error("lock store failed : " + storeUUID, e);
				return false;
			} finally {
				RedisUtil.returnResource(jedis);
			}

		}
		return false;

	}

	public static void unlockAStore(String storeUUID) {
		Jedis jedis = RedisUtil.getJedis();
		if (RedisUtil.checkJedisAvailable(jedis)) {
			try {
				jedis.del("store_lock_" + storeUUID);
			} catch (Exception e) {
				LOGGER.error("unlock store failed : " + storeUUID, e);
			} finally {
				RedisUtil.returnResource(jedis);
			}
		} else {
			throw new IllegalStateException("jedis is unavaliable!");
		}
	}

	public static List<StaffInfo> sortStaff(List<StaffInfo> staffInfos) {
		String[] staffUUIDsWithPrefix = new String[staffInfos.size()];
		for (int i = 0; i < staffInfos.size(); i++) {
			staffUUIDsWithPrefix[i] = ("staff_user_count_" + ((StaffInfo) staffInfos.get(i)).getSysUuid());
		}
		List<String> userCountList = getAllStaffUserCount(staffUUIDsWithPrefix);

		final Map<String, Integer> staffUserCountMap = new HashMap<String, Integer>(staffInfos.size());
		for (int i = 0; i < staffInfos.size(); i++) {
			staffUserCountMap.put(((StaffInfo) staffInfos.get(i)).getSysUuid(),
					Integer.valueOf(NumberUtils.toInt((String) userCountList.get(i))));
		}
		Collections.sort(staffInfos, new Comparator<StaffInfo>() {
			public int compare(StaffInfo staffInfo1, StaffInfo staffInfo2) {
				return ((Integer) staffUserCountMap.get(staffInfo1.getSysUuid())).intValue()
						- ((Integer) staffUserCountMap.get(staffInfo2.getSysUuid())).intValue();
			}
		});
		return staffInfos;
	}

	private static List<String> getAllStaffUserCount(String[] staffUUIDsWithPrefix) {
		Jedis jedis = RedisUtil.getJedis();
		if (RedisUtil.checkJedisAvailable(jedis)) {
			try {
				return jedis.mget(staffUUIDsWithPrefix);
			} finally {
				RedisUtil.returnResource(jedis);
			}
		}
		throw new IllegalStateException("jedis is unavaliable!");
	}
}
