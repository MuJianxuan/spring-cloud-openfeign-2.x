package org.springframework.cloud.openfeign.analysis.consumer.namedcontextfactory;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.cloud.context.named.NamedContextFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Arrays;

public class CommonNameContextTest {

	private static final String PROPERTY_NAME = "test.context.name";

	@Test
	public void test() {
		//创建 parent context
		AnnotationConfigApplicationContext parent = new AnnotationConfigApplicationContext();
		//添加 BaseConfig 相关配置
		parent.register(BaseConfig.class);
		//初始化 parent
		parent.refresh();


		BaseBean bean = parent.getBean(BaseBean.class);
		System.out.println("bean 的信息: "+ bean);


		/**
		 * 疑问 ? 为什么默认需要一个bean呢？
		 *    客户端公共配置
		 */
		//创建 testClient1，默认配置使用 ClientCommonConfig
		TestClient testClient1 = new TestClient( ClientCommonConfig.class);
		//创建 service1 与 service2 以及指定对应额外的配置类
		TestSpec testSpec1 = new TestSpec("service1", new Class[]{Service1Config1.class, Service1Config2.class});
		TestSpec testSpec2 = new TestSpec("service2", new Class[]{Service2Config.class});
		//设置 parent ApplicationContext 为 parent
		testClient1.setApplicationContext(parent);
		//将 service1 与 service2 的配置加入 testClient1
		testClient1.setConfigurations( Arrays.asList(testSpec1, testSpec2));

		/**
		 * 具体流程分析
		 *   1、创建 service1 对应的 子容器 子容器的bean父容器无法访问，并放入映射关系中去
		 *   2、从对应的子容器中获取 对应的 bean，这个bean是属于这个子容器内的；
		 *   3、创建后的bean则会在子容器中存在了，下次获取则不会走 新创建bean了
		 *   4、子容器可以访问父容器的bean，因此该bean是从 父容器中获取的
		 */
		BaseBean baseBean1 = testClient1.getInstance("service1", BaseBean.class);
		System.out.println("testClient1.getInstance(\"service1\", BaseBean.class)" + baseBean1);
		//验证正常获取到了 baseBean1
		Assert.assertNotNull(baseBean1);
		Assert.assertEquals( bean,baseBean1);

		/**
		 *  这个bean是公共配置的 那么这个bean需要留意的是 key的问题
		 *  1、尝试在 service1 对应的容器中获取 ClientCommonBean 这个bean
		 *  2、一开始存在吗？ 似乎是的 但是每一个都是独立开来的
		 */
		ClientCommonBean commonBean = testClient1.getInstance("service1", ClientCommonBean.class);
		System.out.println( "testClient1.getInstance(\"service1\", ClientCommonBean.class);" + commonBean+ "："+commonBean.name);

		//验证正常获取到了 commonBean
		Assert.assertNotNull(commonBean);

		/**
		 * 1、 同理
		 * 2、 注入的 ClientCommonBean 应该是同一个bean，在同一个容器中获取的注入
		 */
		Service1Bean1 service1Bean1 = testClient1.getInstance("service1", Service1Bean1.class);
		System.out.println("testClient1.getInstance(\"service1\", Service1Bean1.class);" + service1Bean1);
		//验证正常获取到了 service1Bean1
		Assert.assertNotNull(service1Bean1);

		/**
		 * 1、同理
		 * 2、 获取这个 Service1Bean2 bean
		 */
		Service1Bean2 service1Bean2 = testClient1.getInstance("service1", Service1Bean2.class);
		System.out.println( "testClient1.getInstance(\"service1\", Service1Bean2.class);"+ service1Bean2);
		//验证正常获取到了 service1Bean2
		Assert.assertNotNull(service1Bean2);


		BaseConfig service1BaseConfig = testClient1.getInstance("service1", BaseConfig.class);
		Assert.assertNotNull( service1BaseConfig);

		// ---------------------------------------------------------------------------------------------------

		/**
		 * server2 尝试获取 BaseBean.class  父容器存在则会在父容器中获取
		 */
		BaseBean baseBean2 = testClient1.getInstance("service2", BaseBean.class);
		System.out.println( "testClient1.getInstance(\"service2\", BaseBean.class);"+ baseBean2);
		Assert.assertEquals(baseBean1, baseBean2);

		/**
		 * 1、注入名称
		 */
		ClientCommonBean commonBean2 = testClient1.getInstance("service2", ClientCommonBean.class);
		System.out.println( "testClient1.getInstance(\"service2\", ClientCommonBean.class);"+ commonBean2 + "："+commonBean2.name);
		//验证正常获取到了 commonBean2 并且 commonBean 和 commonBean2 不是同一个
		Assert.assertNotNull(commonBean2);
		Assert.assertNotEquals(commonBean, commonBean2);

		/**
		 *
		 */
		Service2Bean service2Bean = testClient1.getInstance("service2", Service2Bean.class);
		System.out.println("testClient1.getInstance(\"service2\", Service2Bean.class);" + service2Bean);
		//验证正常获取到了 service2Bean
		Assert.assertNotNull(service2Bean);
		Assert.assertNotEquals( service1Bean2,service2Bean);

		/**
		 * 可见从父容器中获取的 bean是一致的 ，而只要父容器中未定义的 bean， 通过 不同的 配置去获取 则是相互隔离的，
		 *   总结：NamedContextFactory 提供了一种创建子容器的方式，通过配置公共配置类，设置调用时初始化的 bean配置类，相当于提供了一种 在运行时创建互相隔离的 子容器机制；
		 *   而子容器的初始化 则和 公共配置类 挂钩，通过设置对应的 子容器的 bean配置，在调用时检查是否存在来初始化。
		 */
		BaseConfig service2BaseConfig = testClient1.getInstance("service2", BaseConfig.class);
		Assert.assertNotNull( service2BaseConfig);
		Assert.assertEquals( service1BaseConfig,service2BaseConfig);


		parent.close();
	}

	@Configuration(proxyBeanMethods = false)
	static class BaseConfig {
		@Bean(initMethod = "init",destroyMethod = "destroyMethod")
		BaseBean baseBean() {
			return new BaseBean();
		}
	}

	static class BaseBean {
		/**
		 * 初始化方法
		 */
		public void init(){
			System.out.println("B init.......");
		}

		/**
		 * 销毁方法
		 */
		public void destroyMethod(){
			System.out.println("B destroyMethod.......");
		}
	}

	/**
	 * context 隔离 bean
	 */
	@Configuration(proxyBeanMethods = false)
	static class ClientCommonConfig {

		@Bean
		ClientCommonBean clientCommonBean(Environment environment, BaseBean baseBean) {
			//在创建 NamedContextFactory 里面的子 ApplicationContext 的时候，会指定 name，这个 name 对应的属性 key 即 PROPERTY_NAME
			return new ClientCommonBean(environment.getProperty(PROPERTY_NAME), baseBean);
		}
	}

	/**
	 *  留意name 注入方式
	 */
	static class ClientCommonBean {
		private final String name;
		private final BaseBean baseBean;

		ClientCommonBean(String name, BaseBean baseBean) {
			this.name = name;
			this.baseBean = baseBean;
		}

		@Override
		public String toString() {
			return "ClientCommonBean{" +
				"name='" + name + '\'' +
				", baseBean=" + baseBean +
				'}';
		}
	}

	/**
	 * context 隔离 bean
	 */
	@Configuration(proxyBeanMethods = false)
	static class Service1Config1 {
		@Bean
		Service1Bean1 service1Bean1(ClientCommonBean clientCommonBean) {
			return new Service1Bean1(clientCommonBean);
		}
	}

	/**
	 * 普通bean
	 */
	static class Service1Bean1 {
		private final ClientCommonBean clientCommonBean;

		Service1Bean1(ClientCommonBean clientCommonBean) {
			this.clientCommonBean = clientCommonBean;
		}

		@Override
		public String toString() {
			return "Service1Bean1{" +
				"clientCommonBean=" + clientCommonBean +
				'}';
		}
	}

	@Configuration(proxyBeanMethods = false)
	static class Service1Config2 {
		@Bean
		Service1Bean2 service1Bean2() {
			return new Service1Bean2();
		}
	}

	/**
	 * 普通bean
	 */
	static class Service1Bean2 {
	}

	@Configuration(proxyBeanMethods = false)
	static class Service2Config {
		@Bean
		Service2Bean service2Bean(ClientCommonBean clientCommonBean) {
			return new Service2Bean(clientCommonBean);
		}
	}

	/**
	 * 普通类
	 */
	static class Service2Bean {
		private final ClientCommonBean clientCommonBean;

		Service2Bean(ClientCommonBean clientCommonBean) {
			this.clientCommonBean = clientCommonBean;
		}

		@Override
		public String toString() {
			return "Service2Bean{" +
				"clientCommonBean=" + clientCommonBean +
				'}';
		}
	}

	/**
	 * 为子容器定义的规格
	 */
	static class TestSpec implements NamedContextFactory.Specification {
		private final String name;
		private final Class<?>[] configurations;

		public TestSpec(String name, Class<?>[] configurations) {
			this.name = name;
			this.configurations = configurations;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public Class<?>[] getConfiguration() {
			return configurations;
		}
	}

	/**
	 * 子容器创建工厂
	 */
	static class TestClient extends NamedContextFactory<TestSpec> {

		public TestClient(Class<?> defaultConfigType) {
			super(defaultConfigType, "testClient", PROPERTY_NAME);
		}
	}
}
