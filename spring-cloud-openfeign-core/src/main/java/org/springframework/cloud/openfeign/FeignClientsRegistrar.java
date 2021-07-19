/*
 * Copyright 2013-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.openfeign;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AbstractClassTestingTypeFilter;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * @author Spencer Gibb
 * @author Jakub Narloch
 * @author Venil Noronha
 * @author Gang Li
 */
class FeignClientsRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware, EnvironmentAware {

	// patterned after Spring Integration IntegrationComponentScanRegistrar
	// and RibbonClientsConfigurationRegistgrar

	/**
	 * 资源加载器
	 */
	private ResourceLoader resourceLoader;

	/**
	 * 环境变量
	 */
	private Environment environment;

	FeignClientsRegistrar() {
	}

	// 静态方法 ----------------------

	static void validateFallback(final Class clazz) {
		Assert.isTrue(!clazz.isInterface(),
				"Fallback class must implement the interface annotated by @FeignClient");
	}

	static void validateFallbackFactory(final Class clazz) {
		Assert.isTrue(!clazz.isInterface(), "Fallback factory must produce instances "
				+ "of fallback classes that implement the interface annotated by @FeignClient");
	}

	static String getName(String name) {
		if (!StringUtils.hasText(name)) {
			return "";
		}

		String host = null;
		try {
			String url;
			if (!name.startsWith("http://") && !name.startsWith("https://")) {
				url = "http://" + name;
			}
			else {
				url = name;
			}
			host = new URI(url).getHost();

		}
		catch (URISyntaxException e) {
		}
		Assert.state(host != null, "Service id not legal hostname (" + name + ")");
		return name;
	}

	static String getUrl(String url) {
		if (StringUtils.hasText(url) && !(url.startsWith("#{") && url.contains("}"))) {
			if (!url.contains("://")) {
				url = "http://" + url;
			}
			try {
				new URL(url);
			}
			catch (MalformedURLException e) {
				throw new IllegalArgumentException(url + " is malformed", e);
			}
		}
		return url;
	}

	static String getPath(String path) {
		if (StringUtils.hasText(path)) {
			path = path.trim();
			if (!path.startsWith("/")) {
				path = "/" + path;
			}
			if (path.endsWith("/")) {
				path = path.substring(0, path.length() - 1);
			}
		}
		return path;
	}

	// 静态方法 ----------------------

	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	/**
	 * 注册 BeanDefinitions
	 * @param metadata
	 * @param registry
	 */
	@Override
	public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
		// 1.注册 FeignClientSpecification 的 BeanDefinition，
		// 并将在 @EnableFeignClients 中 defaultConfiguration 属性值作为属性放入其中
		registerDefaultConfiguration(metadata, registry);
		// 2.注册那些添加了 @FeignClient 的类或接口
		registerFeignClients(metadata, registry);
	}

	private void registerDefaultConfiguration(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
		// 获取 EnableFeignClients 中属性值
		Map<String, Object> defaultAttrs = metadata
				.getAnnotationAttributes(EnableFeignClients.class.getName(), true);

		// 这里要注意的是 defaultAttrs.containsKey("defaultConfiguration")
		if (defaultAttrs != null && defaultAttrs.containsKey("defaultConfiguration")) {
			String name;
			if (metadata.hasEnclosingClass()) {
				name = "default." + metadata.getEnclosingClassName();
			}
			else {
				name = "default." + metadata.getClassName();
			}

			//注册
			this.registerClientConfiguration(registry, name,
					defaultAttrs.get("defaultConfiguration"));
		}
	}

	/**
	 * 通过获取类加载器大概猜到是用来扫描  远程调用的实现接口的。
	 * @param metadata
	 * @param registry
	 */
	public void registerFeignClients(AnnotationMetadata metadata,
			BeanDefinitionRegistry registry) {

		ClassPathScanningCandidateComponentProvider scanner = getScanner();
		scanner.setResourceLoader(this.resourceLoader);

		Set<String> basePackages;

		Map<String, Object> attrs = metadata.getAnnotationAttributes(EnableFeignClients.class.getName());
		// @FeignClient 注解过滤器
		AnnotationTypeFilter annotationTypeFilter = new AnnotationTypeFilter(FeignClient.class);

		final Class<?>[] clients = attrs == null ? null
				: (Class<?>[]) attrs.get("clients");
		// 默认情况下 clients.length == 0
		if (clients == null || clients.length == 0) {
			// 在这里获取带有 @FeignClient 注解的类，放在 basePackages 中
			scanner.addIncludeFilter(annotationTypeFilter);
			// basePackages 从 @EnableFeignClients 中获取，如果没有设置默认当前启动类包路径
			basePackages = getBasePackages(metadata);
		}
		// 若配置了参数  clients  具体看 这个
		else {
			final Set<String> clientClasses = new HashSet<>();
			basePackages = new HashSet<>();
			for (Class<?> clazz : clients) {
				basePackages.add(ClassUtils.getPackageName(clazz));
				clientClasses.add(clazz.getCanonicalName());
			}
			AbstractClassTestingTypeFilter filter = new AbstractClassTestingTypeFilter() {
				@Override
				protected boolean match(ClassMetadata metadata) {
					String cleaned = metadata.getClassName().replaceAll("\\$", ".");
					return clientClasses.contains(cleaned);
				}
			};
			scanner.addIncludeFilter( new AllTypeFilter(Arrays.asList(filter, annotationTypeFilter)));
		}

		// 遍历这个扫描包路径数组
		for (String basePackage : basePackages) {
			// 在当前包下，将带 @FeignClient 注解扫描出来(其实这些都很简单，不懂就去看 spring 源码)
			// 很明显这个 扫描器 很厉害，至于是怎么实现的，参考 xml解析就知道啦

			// 所有添加 @FeignClient 注解的接口
			Set<BeanDefinition> candidateComponents = scanner.findCandidateComponents( basePackage);

			for (BeanDefinition candidateComponent : candidateComponents) {
				// 注解Bean定义
				if (candidateComponent instanceof AnnotatedBeanDefinition) {
					//验证带注释的类是一个接口
					// verify annotated class is an interface
					AnnotatedBeanDefinition beanDefinition = (AnnotatedBeanDefinition) candidateComponent;
					// 获取当前 bean 的元注解
					AnnotationMetadata annotationMetadata = beanDefinition.getMetadata();
					// 验证带注解的类是接口
					Assert.isTrue(annotationMetadata.isInterface(),
							"@FeignClient can only be specified on an interface");

					// 获取 FeignClient 注解属性值
					Map<String, Object> attributes = annotationMetadata.getAnnotationAttributes( FeignClient.class.getCanonicalName());

					// 获取 ClientName ，优先级 value >= name > serviceId > contextId
					String name = getClientName(attributes);
					// 和之前类似，注册一个 FeignClientSpecification 的 BeanDefinition，之前是全局默认

					// 为 每一个 @FeignClient 注解都添加一个 configuration 的 beanDefinition
					registerClientConfiguration( registry, name, attributes.get("configuration"));

					// 需要留意的是 这里是每一个 FeignClient 接口都注入一个相应处理的  FeignClientFactoryBean
					// 注册一个 FeignClientFactoryBean 的 BeanDefinition  把谁注册？ 是被@FeignClient注解接口
					registerFeignClient( registry, annotationMetadata, attributes);
				}
			}
		}
	}

	/**
	 * 为每一个 @FeignClient 注解的接口注入一个 xxxFactoryBean<T>  FeignClientFactoryBean
	 * 本质是 注入 FeignClientFactoryBean 类
	 * @param registry
	 * @param annotationMetadata
	 * @param attributes
	 */
	private void registerFeignClient(BeanDefinitionRegistry registry, AnnotationMetadata annotationMetadata, Map<String, Object> attributes) {
		// 1.获取类名称，也就是被 @FeignClient 注解修饰的接口
		String className = annotationMetadata.getClassName();
		// 2.使用 BeanDefinitionBuilder 构造bean：FeignClientFactoryBean
		BeanDefinitionBuilder definition = BeanDefinitionBuilder.genericBeanDefinition(  FeignClientFactoryBean.class);
		// 校验 Fallback 和 FallbackFactory （必须实现被 @FeignClient 这个接口）
		validate(attributes);
		// 3.添加 FeignClientFactoryBean 的各个属性值，以下赋值都支持 SPEL 表达式
		definition.addPropertyValue("url", getUrl(attributes));
		definition.addPropertyValue("path", getPath(attributes));
		String name = getName(attributes);
		definition.addPropertyValue("name", name);
		// 获取优先级 contextId > serviceId > name > value ，校验合法性
		String contextId = getContextId(attributes);
		definition.addPropertyValue("contextId", contextId);
		definition.addPropertyValue("type", className);
		definition.addPropertyValue("decode404", attributes.get("decode404"));
		definition.addPropertyValue("fallback", attributes.get("fallback"));
		definition.addPropertyValue("fallbackFactory", attributes.get("fallbackFactory"));
		// BY_TYPE 注入模型
		definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);

		// 4.设置别名
		String alias = contextId + "FeignClient";
		AbstractBeanDefinition beanDefinition = definition.getBeanDefinition();

		// has a default, won't be null
		boolean primary = (Boolean) attributes.get("primary");
		beanDefinition.setPrimary(primary);

		String qualifier = getQualifier(attributes);
		if (StringUtils.hasText(qualifier)) {
			alias = qualifier;
		}

		BeanDefinitionHolder holder = new BeanDefinitionHolder(beanDefinition, className,
				new String[] { alias });
		// 5.注册 FeignClientFactoryBean 的 BeanDefinition
		BeanDefinitionReaderUtils.registerBeanDefinition(holder, registry);
	}

	private void validate(Map<String, Object> attributes) {
		AnnotationAttributes annotation = AnnotationAttributes.fromMap(attributes);
		// This blows up if an aliased property is overspecified
		// FIXME annotation.getAliasedString("name", FeignClient.class, null);
		validateFallback(annotation.getClass("fallback"));
		validateFallbackFactory(annotation.getClass("fallbackFactory"));
	}

	/* for testing */ String getName(Map<String, Object> attributes) {
		String name = (String) attributes.get("serviceId");
		if (!StringUtils.hasText(name)) {
			name = (String) attributes.get("name");
		}
		if (!StringUtils.hasText(name)) {
			name = (String) attributes.get("value");
		}
		name = resolve(name);
		return getName(name);
	}

	private String getContextId(Map<String, Object> attributes) {
		String contextId = (String) attributes.get("contextId");
		if (!StringUtils.hasText(contextId)) {
			return getName(attributes);
		}

		contextId = resolve(contextId);
		return getName(contextId);
	}

	private String resolve(String value) {
		if (StringUtils.hasText(value)) {
			return this.environment.resolvePlaceholders(value);
		}
		return value;
	}

	private String getUrl(Map<String, Object> attributes) {
		String url = resolve((String) attributes.get("url"));
		return getUrl(url);
	}

	private String getPath(Map<String, Object> attributes) {
		String path = resolve((String) attributes.get("path"));
		return getPath(path);
	}

	protected ClassPathScanningCandidateComponentProvider getScanner() {
		return new ClassPathScanningCandidateComponentProvider(false, this.environment) {
			@Override
			protected boolean isCandidateComponent(
					AnnotatedBeanDefinition beanDefinition) {
				boolean isCandidate = false;
				if (beanDefinition.getMetadata().isIndependent()) {
					if (!beanDefinition.getMetadata().isAnnotation()) {
						isCandidate = true;
					}
				}
				return isCandidate;
			}
		};
	}

	/**
	 * 获取配置包  远程调用的接口包
	 * @param importingClassMetadata
	 * @return
	 */
	protected Set<String> getBasePackages(AnnotationMetadata importingClassMetadata) {
		Map<String, Object> attributes = importingClassMetadata
				.getAnnotationAttributes(EnableFeignClients.class.getCanonicalName());

		Set<String> basePackages = new HashSet<>();
		for (String pkg : (String[]) attributes.get("value")) {
			if (StringUtils.hasText(pkg)) {
				basePackages.add(pkg);
			}
		}
		for (String pkg : (String[]) attributes.get("basePackages")) {
			if (StringUtils.hasText(pkg)) {
				basePackages.add(pkg);
			}
		}
		for (Class<?> clazz : (Class[]) attributes.get("basePackageClasses")) {
			basePackages.add(ClassUtils.getPackageName(clazz));
		}

		if (basePackages.isEmpty()) {
			basePackages.add(
					ClassUtils.getPackageName(importingClassMetadata.getClassName()));
		}
		return basePackages;
	}

	private String getQualifier(Map<String, Object> client) {
		if (client == null) {
			return null;
		}
		String qualifier = (String) client.get("qualifier");
		if (StringUtils.hasText(qualifier)) {
			return qualifier;
		}
		return null;
	}

	private String getClientName(Map<String, Object> client) {
		if (client == null) {
			return null;
		}
		String value = (String) client.get("contextId");
		if (!StringUtils.hasText(value)) {
			value = (String) client.get("value");
		}
		if (!StringUtils.hasText(value)) {
			value = (String) client.get("name");
		}
		if (!StringUtils.hasText(value)) {
			value = (String) client.get("serviceId");
		}
		if (StringUtils.hasText(value)) {
			return value;
		}

		throw new IllegalStateException("Either 'name' or 'value' must be provided in @"
				+ FeignClient.class.getSimpleName());
	}

	/**
	 *  FeignClientSpecification.class 都是这个bean
	 *
	 * @param registry
	 * @param name
	 * @param configuration
	 */
	private void registerClientConfiguration(BeanDefinitionRegistry registry, Object name, Object configuration) {
		// 注册 BeanDefinitionBuilder
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(FeignClientSpecification.class);
		// FeignClientSpecification 的属性 name 和 configuration 以构造方法注入,其值如下
		builder.addConstructorArgValue(name);
		builder.addConstructorArgValue(configuration);
		// 将 BeanDefinition 注册进容器中
		registry.registerBeanDefinition(
				name + "." + FeignClientSpecification.class.getSimpleName(),
				builder.getBeanDefinition());

		// 很明显最后注入的是 FeignClientSpecification.class 的实例

		// 虽然很想看一下这个类有啥，但还是先下走

	}

	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	/**
	 * Helper class to create a {@link TypeFilter} that matches if all the delegates
	 * match.
	 *
	 * @author Oliver Gierke
	 */
	private static class AllTypeFilter implements TypeFilter {

		private final List<TypeFilter> delegates;

		/**
		 * Creates a new {@link AllTypeFilter} to match if all the given delegates match.
		 * @param delegates must not be {@literal null}.
		 */
		AllTypeFilter(List<TypeFilter> delegates) {
			Assert.notNull(delegates, "This argument is required, it must not be null");
			this.delegates = delegates;
		}

		@Override
		public boolean match(MetadataReader metadataReader,
				MetadataReaderFactory metadataReaderFactory) throws IOException {

			for (TypeFilter filter : this.delegates) {
				if (!filter.match(metadataReader, metadataReaderFactory)) {
					return false;
				}
			}

			return true;
		}

	}

}
