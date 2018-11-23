package com.robb.namespace.config;

import java.beans.PropertyDescriptor;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;

import com.robb.config.AutoControConfig;

public class BeanInstHandler implements InstantiationAwareBeanPostProcessor {

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName)
			throws BeansException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName)
			throws BeansException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object postProcessBeforeInstantiation(Class<?> beanClass,
			String beanName) throws BeansException {
		if (AutoControConfig.checkBasePackages(beanClass.getName())) {
			return BeanUtils.instantiateClass(beanClass);
		}
		return null;
	}

	@Override
	public boolean postProcessAfterInstantiation(Object bean, String beanName)
			throws BeansException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public PropertyValues postProcessPropertyValues(PropertyValues pvs,
			PropertyDescriptor[] pds, Object bean, String beanName)
			throws BeansException {
		// TODO Auto-generated method stub
		return null;
	}

}
