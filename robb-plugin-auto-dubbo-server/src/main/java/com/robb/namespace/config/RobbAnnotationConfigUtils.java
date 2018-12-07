package com.robb.namespace.config;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.annotation.ScopeMetadata;

public class RobbAnnotationConfigUtils extends AnnotationConfigUtils {

	static BeanDefinitionHolder applyScopedProxyMode(
			ScopeMetadata metadata, BeanDefinitionHolder definition, BeanDefinitionRegistry registry) {

		try {
			Method method = AnnotationConfigUtils.class.getDeclaredMethod("applyScopedProxyMode", 
						new Class[]{ScopeMetadata.class,BeanDefinitionHolder.class,BeanDefinitionRegistry.class});
			method.setAccessible(true);
			return (BeanDefinitionHolder) method.invoke(null, new Object[]{metadata,definition,registry});
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException |NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
//		ScopedProxyMode scopedProxyMode = metadata.getScopedProxyMode();
//		if (scopedProxyMode.equals(ScopedProxyMode.NO)) {
//			return definition;
//		}
//		boolean proxyTargetClass = scopedProxyMode.equals(ScopedProxyMode.TARGET_CLASS);
//		return ScopedProxyCreator.createScopedProxy(definition, registry, proxyTargetClass);
	}
}
