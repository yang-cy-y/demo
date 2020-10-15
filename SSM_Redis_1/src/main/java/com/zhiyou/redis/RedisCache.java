package com.zhiyou.redis;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.ibatis.cache.Cache;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

public class RedisCache implements Cache{
	//定义一个ID
	private final String id;
	//创建读写锁对象
	private final ReadWriteLock rwl=new ReentrantReadWriteLock();
	private static JedisConnectionFactory jedisConnectionFactory;
	
	//为类中注入JedisConnectionFactory
	public static void setJedisConnectionFactory(JedisConnectionFactory jedisConnectionFactory){
		RedisCache.jedisConnectionFactory=jedisConnectionFactory;
	}
	
	
	//通过构造方法给id赋值
	public RedisCache(String id) {
		this.id=id;
	}
	
	//这个id实际上就是缓存的key
	public String getId() {
		return id;
	}

	//将数据写入到缓存中（redis）
	public void putObject(Object key, Object value) {
		rwl.writeLock().lock();	//写数据的时候加锁，防止自己写的时候别人也写
		RedisConnection redis = jedisConnectionFactory.getConnection();	//获取到redis连接
		RedisSerializer<Object> ser=new JdkSerializationRedisSerializer();	//用于序列化数据(kye,valus)
		redis.set(ser.serialize(key), ser.serialize(value));	//将key与value序列化后存入到redis中
		System.out.println("添加二级缓存成功key:"+key+"value"+value);
		redis.close();
		
		rwl.writeLock().unlock();	//写入完成后，放开锁
		
	}

	//从缓存中取出数据
	public Object getObject(Object key) {
		rwl.readLock().lock();
		RedisConnection redis = jedisConnectionFactory.getConnection();	//获取到redis连接
		RedisSerializer<Object> ser=new JdkSerializationRedisSerializer();	//用于序列化数据(kye,valus)
		//根据序列化之后的key，从redis中取出对应的值，再将其反序列化
		Object object = ser.deserialize(redis.get(ser.serialize(key)));
		System.out.println("命中二级缓存成功：value："+object);
		redis.close();
		rwl.readLock().unlock();
		
		
		return object;
	}

	//从缓存中删除数据
	public Object removeObject(Object key) {
		rwl.writeLock().lock();
		RedisConnection redis = jedisConnectionFactory.getConnection();	//获取到redis连接
		RedisSerializer<Object> ser=new JdkSerializationRedisSerializer();	//用于序列化数据(kye,valus)
		Boolean expire = redis.expire(ser.serialize(key), 0);
		redis.close();
		rwl.readLock().unlock();
		return expire;
	}

	//清空缓存
	public void clear() {
		rwl.readLock().lock();
		RedisConnection redis = jedisConnectionFactory.getConnection();	//获取到redis连接
		redis.flushDb();
		redis.flushAll();
		redis.close();
		rwl.readLock().unlock();
		
		
	}

	//获得缓存中数据量
	public int getSize() {
		RedisConnection redis = jedisConnectionFactory.getConnection();	//获取到redis连接
		Integer size = Integer.valueOf(redis.dbSize().toString());
		redis.close();
		return size;
	}

	
	public ReadWriteLock getReadWriteLock() {
		return rwl;
	}

}
