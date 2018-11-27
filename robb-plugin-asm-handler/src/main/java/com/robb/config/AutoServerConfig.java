package com.robb.config;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import jdk.internal.org.objectweb.asm.ClassWriter;

public class AutoServerConfig {

	/**重写class数据缓存*/
	private static ConcurrentMap<String, Class> cache = new ConcurrentHashMap<String, Class>(30);
	
	
	
	
	
	
	public static void addCache(String className,Class clazz) {
		cache.put(className, clazz);
	}
	
	public static Class getCache(String className) {
		return cache.get(className);
	}
}
