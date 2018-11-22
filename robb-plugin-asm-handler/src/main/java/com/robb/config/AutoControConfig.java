package com.robb.config;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import jdk.internal.org.objectweb.asm.ClassWriter;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.Cache;

/**
 * 注解过滤配置
 * */
public class AutoControConfig {

	/**类注解过滤*/
	private static Set<String> nClassAnntoFilter = new HashSet<String>();
	/**方法注解过滤*/
	private static Set<String> nMethodAnntoFilter = new HashSet<String>();
	/**baseController*/
	private static String baseController ;
	/**扫描路径*/
	private static Set<String> baseScanPackages = new HashSet<String>();
	/**重写class数据缓存*/
	private static ConcurrentMap<String, ClassWriter> cache = new ConcurrentHashMap<String, ClassWriter>(30);
	
	/**原class类注解过滤*/
	private static Set<String> oClassAnntoFilter = new HashSet<String>();
	/**原class方法注解过滤*/
	private static Set<String> oMethodAnntoFilter = new HashSet<String>();
	
	/**
	 * 添加过滤条件
	 * @param anntoDesc 
	 * 		注解描述 eg:Lorg/springframework/stereotype/Component;
	 * */
	public static void addClassAnntoFilter(String anntoDesc) {
		nClassAnntoFilter.add(anntoDesc);
	}
	
	/**
	 * 添加过滤条件
	 * @param anntoDesc 
	 * 		注解描述 eg:Lorg/springframework/stereotype/Component;
	 * */
	public static void addMethodAnntoFilter(String anntoDesc) {
		nMethodAnntoFilter.add(anntoDesc);
	}
	
	/**
	 * 添加过滤条件
	 * @param anntoDesc 
	 * 		注解描述 eg:Lorg/springframework/stereotype/Component;
	 * */
	public static void addOClassAnntoFilter(String anntoDesc) {
		oClassAnntoFilter.add(anntoDesc);
	}
	
	/**
	 * 添加过滤条件
	 * @param anntoDesc 
	 * 		注解描述 eg:Lorg/springframework/stereotype/Component;
	 * */
	public static void addOMethodAnntoFilter(String anntoDesc) {
		oMethodAnntoFilter.add(anntoDesc);
	}
	
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
	 *校验过滤条件
	 * @param anntoDesc 
	 * 		注解描述 eg:Lorg/springframework/stereotype/Component;
	 * */
	public static boolean checkClassAnntoFilter(String anntoDesc) {
		return nClassAnntoFilter.contains(anntoDesc);
	}
	
	/**
	 * 校验过滤条件
	 * @param anntoDesc 
	 * 		注解描述 eg:Lorg/springframework/stereotype/Component;
	 * */
	public static boolean checkMethodAnntoFilter(String anntoDesc) {
		return nMethodAnntoFilter.contains(anntoDesc);
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
	
	/**
	 *校验过滤条件
	 * @param anntoDesc 
	 * 		注解描述 eg:Lorg/springframework/stereotype/Component;
	 * */
	public static boolean checkOClassAnntoFilter(String anntoDesc) {
		return oClassAnntoFilter.contains(anntoDesc);
	}
	/**
	 * 校验过滤条件
	 * @param anntoDesc 
	 * 		注解描述 eg:Lorg/springframework/stereotype/Component;
	 * */
	public static boolean checkOMethodAnntoFilter(String anntoDesc) {
		return oMethodAnntoFilter.contains(anntoDesc);
	}
	public static Set<String> getOClassAnntoFilter() {
		return oClassAnntoFilter;
	}
	
	public static Set<String> getOMethodAnntoFilter() {
		return oMethodAnntoFilter;
	}
	
	public static String getBaseController() {
		return baseController;
	}

	public static void setBaseController(String baseController) {
		AutoControConfig.baseController = baseController;
	}
	
	public static void addCache(String className,ClassWriter bs) {
		cache.put(className, bs);
	}
	
	public static ClassWriter getCache(String className) {
		return cache.get(className);
	}
}
