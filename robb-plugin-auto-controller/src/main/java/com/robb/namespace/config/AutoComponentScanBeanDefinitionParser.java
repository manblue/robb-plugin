package com.robb.namespace.config;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

import jdk.internal.org.objectweb.asm.Type;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.beans.factory.xml.XmlReaderContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.annotation.ComponentScanBeanDefinitionParser;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.w3c.dom.Element;

import com.robb.annotation.AutoController;
import com.robb.asm.LoadManager;
import com.robb.asm.Manager2Controller4JdkNode;
import com.robb.config.AutoControConfig;

public class AutoComponentScanBeanDefinitionParser extends ComponentScanBeanDefinitionParser{

	private static final String BASE_PACKAGE_ATTRIBUTE = "base-package";
	private static final String BASE_CONTROLLER = "base-controller";//eg:com.lyc.credit.controllers.BaseController
	
	private static final String ANNOTATION_CONFIG_ATTRIBUTE = "annotation-config";
	
	private static final String ANNOTATION_AUTO_CONTROLLER = AutoController.class.getName();	
	
	@Override
	public BeanDefinition parse(Element element, ParserContext parserContext) {
		Object source = parserContext.extractSource(element);
		
		String basePackage = element.getAttribute(BASE_PACKAGE_ATTRIBUTE);
		basePackage = parserContext.getReaderContext().getEnvironment().resolvePlaceholders(basePackage);
		String[] basePackages = StringUtils.split(basePackage,
				ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS);
		AutoControConfig.addBasePackages(basePackages);
		String baseController = element.getAttribute(BASE_CONTROLLER);
		AutoControConfig.setBaseController(baseController);
		AutoControConfig.addClassAnntoFilter(Type.getDescriptor(AutoController.class));
		AutoControConfig.addClassAnntoFilter(Type.getDescriptor(Component.class));
		AutoControConfig.addOClassAnntoFilter(Type.getDescriptor(AutoController.class));
		AutoControConfig.addOClassAnntoFilter(Type.getDescriptor(RequestMapping.class));
		AutoControConfig.addOMethodAnntoFilter(Type.getDescriptor(RequestMapping.class));

//		Set<BeanDefinitionHolder> processorDefinitions = 
//				AutoComponentConfigUtils.registerAnnotationConfigProcessors(parserContext.getRegistry(), source);
//		// Nest the concrete beans in the surrounding component.
//		for (BeanDefinitionHolder processorDefinition : processorDefinitions) {
//			parserContext.registerComponent(new BeanComponentDefinition(processorDefinition));
//		}
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
			//TODO 重写roobeandefinition
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
//					Class managerClass = Class.forName(beanDefHolder.getBeanDefinition().getBeanClassName(), false, beanDefHolder.getBeanDefinition().getClass().getClassLoader());;
					Class controClass = Manager2Controller4JdkNode.buildControClass(((FileSystemResource)beanDefHolder.getBeanDefinition().getSource()).getInputStream());
		
//					Object object1 = controClass.getConstructor(null).newInstance(null);
					System.out.println("----"+controClass);
					//移除注解
					((ScannedGenericBeanDefinition)beanDefHolder.getBeanDefinition()).getMetadata().getAnnotationTypes().remove(RequestMapping.class.getName());
					BeanDefinitionHolder nBeanDefHolder = registerBeanDefinition(readerContext.getRegistry(), source, controClass);
					compositeDef.addNestedComponent(new BeanComponentDefinition(nBeanDefHolder));
					Class managerClass = LoadManager.loadManagerByCache(beanDefHolder.getBeanDefinition().getBeanClassName(), ClassUtils.getDefaultClassLoader());
//					Object object2 = managerClass.getConstructor(null).newInstance(null);
//					Field field = controClass.getDeclaredField("robbManager");
//					field.setAccessible(true);
//					field.set(object1, object2);
//					
//					Method method = controClass.getDeclaredMethod("add", new Class[]{String.class});
//					method.setAccessible(true);
//					method.invoke(object1, "nihaoo");
					
					beanDefHolder = registerBeanDefinition(readerContext.getRegistry(), source, managerClass);
//					RobbRootBeanDefinition robbBeanDefinition = new RobbRootBeanDefinition( beanDefHolder.getBeanDefinition());
//					
//					Field field = beanDefHolder.getClass().getDeclaredField("beanDefinition");
//					field.setAccessible(true);
//					field.set(beanDefHolder, robbBeanDefinition);
					BeanDefinitionReaderUtils.registerBeanDefinition(beanDefHolder, readerContext.getRegistry());
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
//			RootBeanDefinition rootBeanDefinition = new RootBeanDefinition(beanClassName, cargs, pvs)
			RobbRootBeanDefinition def = new RobbRootBeanDefinition(clazz);
			def.setSource(source);
			def.setScope(BeanDefinition.SCOPE_SINGLETON);
			return register(registry, def, beanName);
		}

		if (AutoControConfig.checkBasePackages(clazz.getName())) {
			RobbRootBeanDefinition def = new RobbRootBeanDefinition(clazz);
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
