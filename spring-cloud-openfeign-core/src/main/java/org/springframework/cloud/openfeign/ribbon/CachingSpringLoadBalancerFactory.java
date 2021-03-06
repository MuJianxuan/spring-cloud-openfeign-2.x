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

package org.springframework.cloud.openfeign.ribbon;

import java.util.Map;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.ILoadBalancer;

import org.springframework.cloud.client.loadbalancer.LoadBalancedRetryFactory;
import org.springframework.cloud.netflix.ribbon.ServerIntrospector;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.util.ConcurrentReferenceHashMap;

/**
 * SpringLoadBalancer 实例的工厂，用于缓存创建的条目。
 *
 * Factory for SpringLoadBalancer instances that caches the entries created.
 *
 * @author Spencer Gibb
 * @author Dave Syer
 * @author Ryan Baxter
 * @author Gang Li
 */
public class CachingSpringLoadBalancerFactory {

	protected final SpringClientFactory factory;

	protected LoadBalancedRetryFactory loadBalancedRetryFactory = null;

	/**
	 * 路由器负载 缓存 ：
	 * 1、调用负载，key是什么？
	 */
	private volatile Map<String, FeignLoadBalancer> cache = new ConcurrentReferenceHashMap<>();

	public CachingSpringLoadBalancerFactory(SpringClientFactory factory) {
		this.factory = factory;
	}

	public CachingSpringLoadBalancerFactory(SpringClientFactory factory,
			LoadBalancedRetryFactory loadBalancedRetryPolicyFactory) {
		this.factory = factory;
		this.loadBalancedRetryFactory = loadBalancedRetryPolicyFactory;
	}

	/**
	 *  关注 clientName 是什么
	 * @param clientName  eg: openfeign-provider
	 * @return
	 */
	public FeignLoadBalancer create(String clientName) {
		/**
		 * 尝试从缓存中获取  看下缓存设计
		 */
		FeignLoadBalancer client = this.cache.get(clientName);
		if (client != null) {
			return client;
		}
		IClientConfig config = this.factory.getClientConfig(clientName);
		/**
		 * 获取负载进行调度服务实例   lb负载均衡器
		 * 1、似乎从容器中获取 对应的实例
		 */
		ILoadBalancer lb = this.factory.getLoadBalancer(clientName);
		// 获取实例
		ServerIntrospector serverIntrospector = this.factory.getInstance(clientName, ServerIntrospector.class);
		//
		client = this.loadBalancedRetryFactory != null
				? new RetryableFeignLoadBalancer(lb, config, serverIntrospector,
						this.loadBalancedRetryFactory)
				: new FeignLoadBalancer(lb, config, serverIntrospector);

		// 存入缓存
		this.cache.put(clientName, client);
		return client;
	}

}
