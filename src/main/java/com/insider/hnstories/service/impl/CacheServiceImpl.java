package com.insider.hnstories.service.impl;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.insider.hnstories.service.CacheService;

import lombok.extern.log4j.Log4j2;


@Log4j2
@Service
public class CacheServiceImpl implements CacheService {

	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	public void set(String key, String value) {
		redisTemplate.opsForValue().set(key, value, 10, TimeUnit.MINUTES);
	}

	
	public Object get(String key) {
		try {
			return redisTemplate.opsForValue().get(key);
		} catch (Exception e) {
			log.error(e.toString());
		}
		return null;
	}

}
