package com.robb.namespace.handler;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

import com.robb.namespace.config.AutoComponentScanBeanDefinitionParser;

public class AutoControNamespaceHandler extends NamespaceHandlerSupport{

	public void init() {
		// TODO Auto-generated method stub
		registerBeanDefinitionParser("component-scan", new AutoComponentScanBeanDefinitionParser());
	}

}
