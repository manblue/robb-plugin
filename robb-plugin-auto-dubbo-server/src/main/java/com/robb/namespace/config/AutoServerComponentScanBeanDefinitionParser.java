package com.robb.namespace.config;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.beans.factory.xml.XmlReaderContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ComponentScanBeanDefinitionParser;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.w3c.dom.Element;

import com.robb.asm.Server2DubboServer;
import com.robb.config.AutoServerConfig;


public class AutoServerComponentScanBeanDefinitionParser extends
		ComponentScanBeanDefinitionParser {

	private static final String BASE_PACKAGE_ATTRIBUTE = "base-package";
	private static final String ANNOTATION_CONFIG_ATTRIBUTE = "annotation-config";
	private static final String ANNOTATION_AUTO_SERVER = Service.class.getName();	
	
	@Override
	public BeanDefinition parse(Element element, ParserContext parserContext) {
		// TODO Auto-generated method stub
		Object source = parserContext.extractSource(element);
		
		String basePackage = element.getAttribute(BASE_PACKAGE_ATTRIBUTE);
		basePackage = parserContext.getReaderContext().getEnvironment().resolvePlaceholders(basePackage);
		String[] basePackages = StringUtils.split(basePackage,
				ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS);
		AutoServerConfig.addBasePackages(basePackages);
		System.out.println(Thread.currentThread().getContextClassLoader());
		return super.parse(element, parserContext);
	}
	
	@Override
	protected void registerComponents(XmlReaderContext readerContext,
			Set<BeanDefinitionHolder> beanDefinitions, Element element) {
		// TODO Auto-generated method stub
		Object source = readerContext.extractSource(element);
		CompositeComponentDefinition compositeDef = new CompositeComponentDefinition(element.getTagName(), source);
		Server2DubboServer handler = Server2DubboServer.getHandler();
		for (BeanDefinitionHolder beanDefinitionHolder : beanDefinitions) {
			
			//server 自动生成接口层代码
			if (((ScannedGenericBeanDefinition)beanDefinitionHolder.getBeanDefinition()).getMetadata().getAnnotationTypes().contains(ANNOTATION_AUTO_SERVER)) {
				
				try {
					Class dubboServerImplClass = handler.buildDubboServerClass(((Resource)beanDefinitionHolder.getBeanDefinition().getSource()).getInputStream());
//					
//					BeanDefinitionHolder nBeanDefHolder = registerBeanDefinition(readerContext.getRegistry(), source, dubboServerImplClass);
//					compositeDef.addNestedComponent(new BeanComponentDefinition(nBeanDefHolder));
					
					Class serverImplClass = AutoServerConfig.removeServiceImplClassCache(beanDefinitionHolder.getBeanDefinition().getBeanClassName());
 
					BeanDefinitionReaderUtils.registerBeanDefinition(beanDefinitionHolder, readerContext.getRegistry());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			compositeDef.addNestedComponent(new BeanComponentDefinition(beanDefinitionHolder));
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
	
	@Override
	protected ClassPathBeanDefinitionScanner createScanner(XmlReaderContext readerContext, boolean useDefaultFilters) {
		// TODO Auto-generated method stub
//		return super.createScanner(readerContext, useDefaultFilters);
		return new RobbClassPathBeanDefinitionScanner(readerContext.getRegistry(), useDefaultFilters, 
				readerContext.getEnvironment(), readerContext.getResourceLoader());
	}
	
	private BeanDefinitionHolder registerBeanDefinition(
			BeanDefinitionRegistry registry, Object source,Class clazz) {

		String beanName = StringUtils.lowerCase(clazz.getSimpleName().substring(0, 1)).concat(clazz.getSimpleName().substring(1));

		if (!registry.containsBeanDefinition(beanName)) {
//			RootBeanDefinition rootBeanDefinition = new RootBeanDefinition(beanClassName, cargs, pvs)
			RootBeanDefinition def = new RootBeanDefinition(clazz);
			def.setSource(source);
			def.setScope(BeanDefinition.SCOPE_SINGLETON);
			return register(registry, def, beanName);
		}

		if (AutoServerConfig.checkBasePackages(clazz.getName())) {
			RootBeanDefinition def = new RootBeanDefinition(clazz);
			def.setSource(source);
			def.setScope(BeanDefinition.SCOPE_SINGLETON);
			return register(registry, def, beanName);
		}
		return null;
	}
	
	
	private static BeanDefinitionHolder register(
			BeanDefinitionRegistry registry, RootBeanDefinition definition, String beanName) {

		definition.setRole(BeanDefinition.ROLE_APPLICATION);
		registry.registerBeanDefinition(beanName, definition);
		return new BeanDefinitionHolder(definition, beanName);
	}
}
