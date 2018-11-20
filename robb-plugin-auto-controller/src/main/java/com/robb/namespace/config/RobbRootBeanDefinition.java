package com.robb.namespace.config;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.support.RootBeanDefinition;

import com.robb.asm.LoadManager;
import com.robb.config.AutoControConfig;

public class RobbRootBeanDefinition extends RootBeanDefinition {

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
