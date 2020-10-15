package com.zhiyou.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.stereotype.Component;

//静态注入中间类
@Component
public class RedisUtil {

	/*
	 * 由于RedisCache类不能被Spring直接管理，所以无法直接在里面获得JedisConnectionFactory对象，所以需要通过这个类，让这个类被IOC容器管理，
	 * 然后从容器中取出
	 */
	
	@Autowired
	public void setJedisConnectionFactory(JedisConnectionFactory jedisConnectionFactory){
		RedisCache.setJedisConnectionFactory(jedisConnectionFactory);
	}
	
}
