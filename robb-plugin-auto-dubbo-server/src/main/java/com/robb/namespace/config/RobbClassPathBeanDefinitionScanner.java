package com.robb.namespace.config;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionDefaults;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.annotation.AnnotationScopeMetadataResolver;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.context.annotation.ScopeMetadata;
import org.springframework.context.annotation.ScopeMetadataResolver;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.util.Assert;

import com.robb.config.AutoServerConfig;

public class RobbClassPathBeanDefinitionScanner extends ClassPathBeanDefinitionScanner {
	static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";
	static final String DEFAULT_JAR_PATTERN = "jar!";
	
	private ScopeMetadataResolver scopeMetadataResolver = new AnnotationScopeMetadataResolver();
	private BeanNameGenerator beanNameGenerator = new AnnotationBeanNameGenerator();

	
	public RobbClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry, boolean useDefaultFilters,
			Environment environment, ResourceLoader resourceLoader) {
		super(registry, useDefaultFilters, environment, resourceLoader);
	}
	
	@Override
	public void setScopeMetadataResolver(
			ScopeMetadataResolver scopeMetadataResolver) {
		// TODO Auto-generated method stub
		this.scopeMetadataResolver = scopeMetadataResolver;
		super.setScopeMetadataResolver(scopeMetadataResolver);
	}
	
	@Override
	public void setScopedProxyMode(ScopedProxyMode scopedProxyMode) {
		// TODO Auto-generated method stub
		this.scopeMetadataResolver = new AnnotationScopeMetadataResolver(scopedProxyMode);
		super.setScopedProxyMode(scopedProxyMode);
	}
	
	@Override
	public void setBeanNameGenerator(BeanNameGenerator beanNameGenerator) {
		// TODO Auto-generated method stub
		this.beanNameGenerator = (beanNameGenerator != null ? beanNameGenerator : new AnnotationBeanNameGenerator());
		super.setBeanNameGenerator(beanNameGenerator);
	}
	
	@Override
	public Set<BeanDefinition> findCandidateComponents(String basePackage) {
		System.out.println(getClass().getClassLoader());

		Set<BeanDefinition> candidates = new LinkedHashSet<BeanDefinition>();
		try {
			String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
					resolveBasePackage(basePackage) + '/' + this.DEFAULT_RESOURCE_PATTERN;
			Resource[] resources = ((ResourcePatternResolver)getResourceLoader()).getResources(packageSearchPath);
			boolean traceEnabled = logger.isTraceEnabled();
			boolean debugEnabled = logger.isDebugEnabled();
			for (Resource resource : resources) {
				if (traceEnabled) {
					logger.trace("Scanning " + resource);
				}
				if (resource.isReadable()) {
					try {
						MetadataReader metadataReader = getMetadataReaderFactory().getMetadataReader(resource);
						if (isCandidateComponent(metadataReader)) {
							ScannedGenericBeanDefinition sbd = new ScannedGenericBeanDefinition(metadataReader);
							sbd.setResource(resource);
							sbd.setSource(resource);
							if (isCandidateComponent(sbd)) {
								if (debugEnabled) {
									logger.debug("Identified candidate component class: " + resource);
								}
								candidates.add(sbd);
							}
							else {
								if (debugEnabled) {
									logger.debug("Ignored because not a concrete top-level class: " + resource);
								}
							}
						}
						else {
							//TODO 记录接口类信息
							System.out.println("getDescription---"+resource.getDescription());
							System.out.println("getFilename---"+resource.getFilename());
							if (!resource.getDescription().contains("jar!")) {
								System.out.println(resource.getFile().getAbsolutePath());
								System.out.println(resource.getFile().getName());
								System.out.println(resource.getFile().getPath());
								System.out.println(resource.getFile().getCanonicalPath());
							}

							if ("package-info.class".equals(resource.getFilename())) {
								continue;
							}
							String className = resource.getDescription();
							if (className.contains(DEFAULT_JAR_PATTERN)) {
								className = StringUtils.substringAfter(className, DEFAULT_JAR_PATTERN).replaceAll("]", "");
							}else {
								className = StringUtils.substringAfter(className, "classes").replace("]", "").replace("\\", "/");
							}
							AutoServerConfig.addServiceResourceCache(className, resource);
							if (traceEnabled) {
								logger.trace("Ignored because not matching any filter: " + resource);
							}
						}
					}
					catch (Throwable ex) {
						throw new BeanDefinitionStoreException(
								"Failed to read candidate component class: " + resource, ex);
					}
				}
				else {
					if (traceEnabled) {
						logger.trace("Ignored because not readable: " + resource);
					}
				}
			}
		}
		catch (IOException ex) {
			throw new BeanDefinitionStoreException("I/O failure during classpath scanning", ex);
		}
		return candidates;
	}

	@Override
	public int scan(String... basePackages) {
		// TODO Auto-generated method stub
		return super.scan(basePackages);
	}
	
	@Override
	protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
		// TODO Auto-generated method stub
//		return super.doScan(basePackages);
		Assert.notEmpty(basePackages, "At least one base package must be specified");
		Set<BeanDefinitionHolder> beanDefinitions = new LinkedHashSet<BeanDefinitionHolder>();
		for (String basePackage : basePackages) {
			Set<BeanDefinition> candidates = findCandidateComponents(basePackage);
			for (BeanDefinition candidate : candidates) {
				ScopeMetadata scopeMetadata = this.scopeMetadataResolver.resolveScopeMetadata(candidate);
				candidate.setScope(scopeMetadata.getScopeName());
				String beanName = this.beanNameGenerator.generateBeanName(candidate, getRegistry());
				if (candidate instanceof AbstractBeanDefinition) {
					postProcessBeanDefinition((AbstractBeanDefinition) candidate, beanName);
				}
				if (candidate instanceof AnnotatedBeanDefinition) {
					AnnotationConfigUtils.processCommonDefinitionAnnotations((AnnotatedBeanDefinition) candidate);
				}
				if (checkCandidate(beanName, candidate)) {
					BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(candidate, beanName);
					definitionHolder =
							RobbAnnotationConfigUtils.applyScopedProxyMode(scopeMetadata, definitionHolder, getRegistry());
					beanDefinitions.add(definitionHolder);
//					registerBeanDefinition(definitionHolder, getRegistry());
				}
			}
		}
		return beanDefinitions;
	}
}
