package com.robb.config;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import jdk.internal.org.objectweb.asm.ClassWriter;

public class AutoServerConfig {

	/**类注解过滤*/
	private static Set<String> nClassAnntoFilter = new HashSet<String>();
	/**类注解保留*/
	private static Set<String> nClassAnntoCopy = new HashSet<String>();
	/**扫描路径*/
	private static Set<String> baseScanPackages = new HashSet<String>();
	
	/**重写class数据缓存*/
	private static ConcurrentMap<String, Class> cache = new ConcurrentHashMap<String, Class>(30);
	
	
	
	
	/**
	 * 添加扫描路径
	 * @param basePackage 
	 * 		扫描路径 eg:org.springframework.stereotype
	 * */
	public static void addBasePackages(String... basePackages) {
		if (ArrayUtils.isEmpty(basePackages)) {
			return;
		}
		for (String basePackage : basePackages) {
			baseScanPackages.add(basePackage);
		}
	}
	
	/**
	 * 校验扫描路径
	 * @param basePackage 
	 * 		扫描路径 eg:org.springframework.stereotype
	 * */
	public static boolean checkBasePackages(String path) {
		if (StringUtils.isBlank(path)) {
			return false;
		}
		for (String scanPackage : baseScanPackages) {
			if (path.startsWith(scanPackage)) {
				return true;
			}
		}
		return false;
	}
	
	public static void addCache(String className,Class clazz) {
		cache.put(className, clazz);
	}
	
	public static Class getCache(String className) {
		return cache.get(className);
	}
}
