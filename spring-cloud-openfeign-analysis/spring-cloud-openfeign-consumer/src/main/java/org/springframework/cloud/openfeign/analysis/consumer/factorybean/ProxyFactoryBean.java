package org.springframework.cloud.openfeign.analysis.consumer.factorybean;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.Proxy;

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
	public T getObject() {
		return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(),
				new Class[]{interfaceClass},
				(proxy, method, args) -> {
					// 调用远程参数进行返回结果
					// 获取返回的类型 用于解析返回结果！
					Class<?> returnType = method.getReturnType();
					return null;
				});
	}

	@Override
	public Class<?> getObjectType() {
		return interfaceClass;
	}
}
