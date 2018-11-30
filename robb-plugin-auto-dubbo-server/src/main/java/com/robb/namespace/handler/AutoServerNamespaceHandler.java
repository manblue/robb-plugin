package com.robb.namespace.handler;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

import com.robb.namespace.config.AutoServerComponentScanBeanDefinitionParser;

public class AutoServerNamespaceHandler extends NamespaceHandlerSupport{

	@Override
	public void init() {
		// TODO Auto-generated method stub
		registerBeanDefinitionParser("component-scan", new AutoServerComponentScanBeanDefinitionParser());
	}
}
