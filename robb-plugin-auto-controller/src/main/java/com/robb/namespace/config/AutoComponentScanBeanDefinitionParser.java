package com.robb.namespace.config;

import java.util.Set;

import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
import org.springframework.beans.factory.xml.XmlReaderContext;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.annotation.ComponentScanBeanDefinitionParser;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.w3c.dom.Element;

import com.robb.annotation.AutoController;

public class AutoComponentScanBeanDefinitionParser extends ComponentScanBeanDefinitionParser{

	
	private static final String BASE_CONTROLLER = "base-controller";//eg:com.lyc.credit.controllers.BaseController
	
	private static final String ANNOTATION_CONFIG_ATTRIBUTE = "annotation-config";
	
	
	@Override
	protected void registerComponents(XmlReaderContext readerContext,
			Set<BeanDefinitionHolder> beanDefinitions, Element element) {
		// TODO Auto-generated method stub
//		super.registerComponents(readerContext, beanDefinitions, element);
		
		Object source = readerContext.extractSource(element);
		CompositeComponentDefinition compositeDef = new CompositeComponentDefinition(element.getTagName(), source);

		for (BeanDefinitionHolder beanDefHolder : beanDefinitions) {
			System.out.println("----"+beanDefHolder.getBeanDefinition().getBeanClassName());
			System.out.println("----"+beanDefHolder.getBeanDefinition().getSource());
			((ScannedGenericBeanDefinition)beanDefHolder.getBeanDefinition()).getMetadata();
			for (  String aa :((ScannedGenericBeanDefinition)beanDefHolder.getBeanDefinition()).getMetadata().getAnnotationTypes()) {
				System.out.println("------"+aa);

			}
			if (((ScannedGenericBeanDefinition)beanDefHolder.getBeanDefinition()).getMetadata().getAnnotationTypes().contains(AutoController.class.getName())) {
				System.out.println("------"+beanDefHolder.getBeanDefinition().getBeanClassName()+" is a "+AutoController.class.getSimpleName());
			}
			compositeDef.addNestedComponent(new BeanComponentDefinition(beanDefHolder));
		}

		// Register annotation config processors, if necessary.
		boolean annotationConfig = true;
		if (element.hasAttribute(ANNOTATION_CONFIG_ATTRIBUTE)) {
			annotationConfig = Boolean.valueOf(element.getAttribute(ANNOTATION_CONFIG_ATTRIBUTE));
		}
		if (annotationConfig) {
			Set<BeanDefinitionHolder> processorDefinitions =
					AnnotationConfigUtils.registerAnnotationConfigProcessors(readerContext.getRegistry(), source);
			for (BeanDefinitionHolder processorDefinition : processorDefinitions) {
				compositeDef.addNestedComponent(new BeanComponentDefinition(processorDefinition));
			}
		}

		readerContext.fireComponentRegistered(compositeDef);
	}
}
