package org.springframework.cloud.openfeign.analysis.consumer.namedcontextfactory;

import org.springframework.cloud.context.named.NamedContextFactory;

/**
 * 实现目的：
 * 创建客户端、负载均衡器和客户端配置实例的工厂。 重点： 它为每个客户端名称创建一个 Spring ApplicationContext，并从那里提取它需要的 bean
 *
 * 如Ribbon的SpringClientFactory类的效果
 * 这个bean需要注入 Spring 主容器  ；而我们定义则是 子容器
 *
 * @author Rao
 * @Date 2021/7/21
 **/
public class MyNamedContextFactory extends NamedContextFactory<TestSpecification> {

	/**
	 * 这个很重要
	 */
	public MyNamedContextFactory() {
		super( Test0ContextAutoConfiguration.class, "", "");
	}

}
