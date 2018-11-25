package com.robb.namespace.config;


import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;

import com.robb.config.AutoControConfig;

public class BeanInstHandler extends InstantiationAwareBeanPostProcessorAdapter {


	@Override
	public Object postProcessBeforeInstantiation(Class<?> beanClass,
			String beanName) throws BeansException {
		if (AutoControConfig.checkBasePackages(beanClass.getName())) {
			return BeanUtils.instantiateClass(beanClass);
		}
		return null;
	}

}
