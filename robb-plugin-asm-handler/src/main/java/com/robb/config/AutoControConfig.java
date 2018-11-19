package com.robb.config;

import java.util.HashSet;
import java.util.Set;

/**
 * 注解过滤配置
 * */
public class AutoControConfig {

	/**类注解过滤*/
	private static Set<String> classAnntoFilter = new HashSet<String>();
	/**方法注解过滤*/
	private static Set<String> methodAnntoFilter = new HashSet<String>();
	/**baseController*/
	private static String baseController ;
	/**
	 * 添加过滤条件
	 * @param anntoDesc 
	 * 		注解描述 eg:Lorg/springframework/stereotype/Component;
	 * */
	public static void addClassAnntoFilter(String anntoDesc) {
		classAnntoFilter.add(anntoDesc);
	}
	
	/**
	 * 添加过滤条件
	 * @param anntoDesc 
	 * 		注解描述 eg:Lorg/springframework/stereotype/Component;
	 * */
	public static void addMethodAnntoFilter(String anntoDesc) {
		methodAnntoFilter.add(anntoDesc);
	}
	
	/**
	 *校验过滤条件
	 * @param anntoDesc 
	 * 		注解描述 eg:Lorg/springframework/stereotype/Component;
	 * */
	public static boolean checkClassAnntoFilter(String anntoDesc) {
		return classAnntoFilter.contains(anntoDesc);
	}
	
	/**
	 * 校验过滤条件
	 * @param anntoDesc 
	 * 		注解描述 eg:Lorg/springframework/stereotype/Component;
	 * */
	public static boolean checkMethodAnntoFilter(String anntoDesc) {
		return methodAnntoFilter.contains(anntoDesc);
	}

	public static String getBaseController() {
		return baseController;
	}

	public static void setBaseController(String baseController) {
		AutoControConfig.baseController = baseController;
	}
	
	
}
