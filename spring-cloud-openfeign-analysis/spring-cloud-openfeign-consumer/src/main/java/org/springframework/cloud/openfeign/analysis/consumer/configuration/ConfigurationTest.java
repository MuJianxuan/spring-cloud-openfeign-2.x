package org.springframework.cloud.openfeign.analysis.consumer.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @author Rao
 * @Date 2021/7/22
 **/
@Slf4j
public class ConfigurationTest {
	public static void main(String[] args) {

		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(MyConfiguration.class);
		context.refresh();
		A a = context.getBean(A.class);
		B b = context.getBean(B.class);

		a.hello();
		b.hello();

	}


	@Configuration(proxyBeanMethods = false)
//	@Configuration
	static class MyConfiguration{

		@Bean(initMethod = "init",destroyMethod = "destroyMethod")
		public A a(){
			return new A();
		}

		@Bean(initMethod = "init",destroyMethod = "destroyMethod")
		public B b(){
			/**
			 * 当 @Configuration(proxyBeanMethods = false) 时，此时的 a() 方法则是 重新new A(); 方法进行 注入；
			 * 且相关生命周期接口与Spring相关的接口实现不调用，因为 B里面的 a 实例并不在容器中。
			 */
			return new B( a());
		}


	}

	interface Say{
		void hello();
	}

	static class A implements InitializingBean, DisposableBean,Say{

		@Autowired
		private B b;

		public A() {
			log.info("A Construct .......");
		}

		/**
		 * 初始化方法
		 */
		public void init(){
			log.info("A init.......");
		}

		/**
		 * 销毁方法
		 */
		public void destroyMethod(){
			log.info("A destroyMethod....");
		}

		@PostConstruct
		public void postConstruct(){
			log.info("A postConstruct......");
		}

		@Override
		public void afterPropertiesSet() throws Exception {
			log.info("A afterPropertiesSet......");
		}

		@Override
		public void destroy() throws Exception {
			log.info("A destroy........");
		}

		@Override
		public void hello() {
			log.info("word！ {}",b);
		}
	}

	static class B  implements InitializingBean, DisposableBean,Say {

		private A a;

		public B(A a) {
			this.a = a;
			log.info("B Construct .......");
		}

		/**
		 * 初始化方法
		 */
		public void init(){
			log.info("B init.......");
		}

		/**
		 * 销毁方法
		 */
		public void destroyMethod(){
			log.info("B destroyMethod....");
		}

		@PostConstruct
		public void postConstruct(){
			log.info("B postConstruct......");
		}

		@Override
		public void afterPropertiesSet() throws Exception {
			log.info("B afterPropertiesSet......");
		}

		@Override
		public void destroy() throws Exception {
			log.info("B destroy........");
		}

		@Override
		public void hello() {
			log.info("word！ {}",a.b);
		}

	}



}
