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

import org.springframework.cloud.context.named.NamedContextFactory;

/**
 *
 * 这个我没看懂，似乎这个很重要
 *  1、服务启动会注入这个bean，我们看这个bean都做了那些事情
 *
 *  根据 Ribbon 的 SpringClientFactory 类的意思
 *      创建 feign 类实例的工厂。 它为每个客户端名称创建一个 Spring ApplicationContext，并从那里提取它需要的 bean。
 *      客户端名称 >>  即远程调用服务的名称关联
 *
 * A factory that creates instances of feign classes. It creates a Spring
 * ApplicationContext per client name, and extracts the beans that it needs from there.
 *
 * @author Spencer Gibb
 * @author Dave Syer
 */
public class FeignContext extends NamedContextFactory<FeignClientSpecification> {

	public FeignContext() {

		super( FeignClientsConfiguration.class, "feign", "feign.client.name");
	}

}
