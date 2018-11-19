package com.robb.namespace.config;

import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Set;

import jdk.internal.org.objectweb.asm.Type;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.beans.factory.xml.XmlReaderContext;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ComponentScanBeanDefinitionParser;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.w3c.dom.Element;

import com.robb.annotation.AutoController;
import com.robb.asm.DefaultManager2Controller;
import com.robb.asm.Manager2Controller4JdkNode;
import com.robb.config.AutoControConfig;

public class AutoComponentScanBeanDefinitionParser extends ComponentScanBeanDefinitionParser{

	
	private static final String BASE_CONTROLLER = "base-controller";//eg:com.lyc.credit.controllers.BaseController
	
	private static final String ANNOTATION_CONFIG_ATTRIBUTE = "annotation-config";
	
	private static final String ANNOTATION_AUTO_CONTROLLER = AutoController.class.getName();	
	
	@Override
	public BeanDefinition parse(Element element, ParserContext parserContext) {
		Object source = parserContext.extractSource(element);
		
		String baseController = element.getAttribute(BASE_CONTROLLER);
		AutoControConfig.setBaseController(baseController);
		AutoControConfig.addClassAnntoFilter(Type.getDescriptor(AutoController.class));
		AutoControConfig.addClassAnntoFilter(Type.getDescriptor(Component.class));
		ClassPathBeanDefinitionScanner scanner = configureScanner(parserContext, element);

		return super.parse(element, parserContext);
	}
	
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
			
			//生成manager对应的controller
			if (((ScannedGenericBeanDefinition)beanDefHolder.getBeanDefinition()).getMetadata().getAnnotationTypes().contains(ANNOTATION_AUTO_CONTROLLER)) {
				System.out.println("------"+beanDefHolder.getBeanDefinition().getBeanClassName()+" is a "+AutoController.class.getSimpleName());
				
				try {
					Class managerClass = Class.forName(beanDefHolder.getBeanDefinition().getBeanClassName(), false, beanDefHolder.getBeanDefinition().getClass().getClassLoader());;
					Class controClass = Manager2Controller4JdkNode.buildControClass(managerClass,((FileSystemResource)beanDefHolder.getBeanDefinition().getSource()).getInputStream());
		
					System.out.println("----"+controClass);
					//移除注解
					((ScannedGenericBeanDefinition)beanDefHolder.getBeanDefinition()).getMetadata().getAnnotationTypes().remove(RequestMapping.class.getName());
					BeanDefinitionHolder nBeanDefHolder = registerBeanDefinition(readerContext.getRegistry(), source, controClass);
					compositeDef.addNestedComponent(new BeanComponentDefinition(nBeanDefHolder));
			
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
	
	
	private BeanDefinitionHolder registerBeanDefinition(
			BeanDefinitionRegistry registry, Object source,Class clazz) {

		String beanName = StringUtils.lowerCase(clazz.getSimpleName().substring(0, 1)).concat(clazz.getSimpleName().substring(1));
		DefaultListableBeanFactory beanFactory = unwrapDefaultListableBeanFactory(registry);
//		if (beanFactory != null) {
//			if (!(beanFactory.getDependencyComparator() instanceof AnnotationAwareOrderComparator)) {
//				beanFactory.setDependencyComparator(AnnotationAwareOrderComparator.INSTANCE);
//			}
//			if (!(beanFactory.getAutowireCandidateResolver() instanceof ContextAnnotationAutowireCandidateResolver)) {
//				beanFactory.setAutowireCandidateResolver(new ContextAnnotationAutowireCandidateResolver());
//			}
//		}


		if (!registry.containsBeanDefinition(beanName)) {
			RootBeanDefinition def = new RootBeanDefinition(clazz);
			def.setSource(source);
			def.setScope(BeanDefinition.SCOPE_SINGLETON);
			return register(registry, def, beanName);
		}

		return null;
	}
	
	private static DefaultListableBeanFactory unwrapDefaultListableBeanFactory(BeanDefinitionRegistry registry) {
		if (registry instanceof DefaultListableBeanFactory) {
			return (DefaultListableBeanFactory) registry;
		}
		else if (registry instanceof GenericApplicationContext) {
			return ((GenericApplicationContext) registry).getDefaultListableBeanFactory();
		}
		else {
			return null;
		}
	}
	
	private static BeanDefinitionHolder register(
			BeanDefinitionRegistry registry, RootBeanDefinition definition, String beanName) {

		definition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
		registry.registerBeanDefinition(beanName, definition);
		return new BeanDefinitionHolder(definition, beanName);
	}
}
