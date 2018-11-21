package com.robb.namespace.config;


import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

import com.robb.asm.LoadManager;
import com.robb.config.AutoControConfig;

public class RobbRootBeanDefinition extends RootBeanDefinition {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3961932610330334037L;

	public RobbRootBeanDefinition(Class beanClass) {
		super(beanClass);
	}
	
	@Override
	public Class<?> resolveBeanClass(ClassLoader classLoader)
			throws ClassNotFoundException {
		String className = getBeanClassName();
		if (className == null) {
			return null;
		}
		//修改class信息 移除相关注解
		if (AutoControConfig.checkBasePackages(className)) {
			return LoadManager.loadManagerByCache(className, classLoader);
		}
		return super.resolveBeanClass(classLoader);
	}
}
