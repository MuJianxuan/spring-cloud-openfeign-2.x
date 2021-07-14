package org.springframework.cloud.openfeign.analysis.consumer.factorybean;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.FactoryBean;

/**
 * @author Rao
 * @Date 2021/7/14
 **/
@Slf4j
public class ProxyFactoryBean<T> implements FactoryBean<T> {

	/**
	 * 被代理的接口Class对象
	 */
	private Class<T> interfaceClass;

	public ProxyFactoryBean(Class<T> interfaceClass) {
		this.interfaceClass = interfaceClass;
	}

	@Override
	public T getObject() throws Exception {
		return null;
	}

	@Override
	public Class<?> getObjectType() {
		return interfaceClass;
	}
}
