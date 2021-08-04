# OpenFeign 源码解析

> 远程调用组件很重要！封装了Ribbon做底层负载均衡调用。

## 为什么接口可以调用服务？

> openFeign 的本质。

1. SpringBoot 应用启动时， 由针对 `@EnableFeignClient` 这一注解的处理逻辑触发程序扫描 classPath中所有被`@FeignClient` 注解的类， 这里以 `DemoService` 为例， 将这些类解析为 BeanDefinition 注册到 Spring 容器中
2. Sping 容器在为某些用的 Feign 接口的 Bean 注入 `DemoService` 时， Spring 会尝试从容器中查找 DemoService 的实现类
3. 由于我们从来没有编写过 `DemoService` 的实现类， 上面步骤获取到的 DemoService 的实现类必然是 feign 框架通过扩展 spring 的 Bean 处理逻辑， 为 `DemoService` 创建一个动态接口代理对象， 这里我们将其称为 `DemoServiceProxy` 注册到spring 容器中。
4. Spring 最终在使用到 `DemoService` 的 Bean 中注入了 `DemoServiceProxy` 这一实例。
5. 当业务请求真实发生时， 对于 `DemoService` 的调用被统一转发到了由 Feign 框架实现的 `InvocationHandler` 中， `InvocationHandler` 负责将接口中的入参转换为 HTTP 的形式， 发到服务端， 最后再解析 HTTP 响应， 将结果转换为 Java 对象， 予以返回。

> 接口没有实现类，那如何创建实例，代理？那么代理调用的方法是如何触发的？InvocationHandler 执行的实现？
>
> 思考一下我们会有那么多那么多的疑问？

## Spring 如何为接口创建代理？

通过统一定义的 FeignClientFactoryBean.class 这个类来实例化接口的实现。而这个类实现了 Spring 中的 FactoryBean 接口。

启动时，@EnableFeignClients 对应包下的 @FeignClient注解接口，对其进行配置，并注入FeignClientFactoryBean.class 的定义，并且将接口上定义的注解参数也一同配置进去。

在Spring中，我们知道，FactoryBean 是作用在 获取bean实例的，因此每个被@FeignClient注解的接口在Spring BeanDefinitionMap中就存在了对应的定义，这样在Spring启动时，就会通过FactoryBean的getObject 来实例，此时

```java
/**
 * 获取实际的Bean对象
 *
 * 创建代理，实现接口调用，我的接口的方法执行，需要有结果，这个时候应该是什么样的结果呢？
 *   似乎还做了 服务的拉取
 *
 * @param <T> the target type of the Feign client
 * @return a {@link Feign} client created with the specified data and the context
 * information   使用指定数据和上下文信息创建的Feign客户端
 */
public  <T> T getTarget() {
   // 获取 Feign 的上下文对象 FeignContext （这个对象是自动装配 FeignAutoConfiguration 而来）
   // （做一个记号研究TraceFeignContext extends FeignContext,实际作用是 TraceFeignContext 属性 delegate =  FeignContext）
   FeignContext context = this.applicationContext.getBean( FeignContext.class);
   // 生成 builder 对象，用来生成 feign
   Feign.Builder builder = feign( context);

   // 判断生成的代理对象类型，如果 url 为空，则走负载均衡，生成有负载均衡功能的代理类
   // 如果不包含文本
   // 域名服务器 则不需要解析 服务名获取数据
   if (! StringUtils.hasText( this.url)) { // url: "" 会进判断
      // url 使用域名形似所以有负载均衡
      if (! this.name.startsWith("http")) {
         this.url = "http://" + this.name;
      }
      else {
         this.url = this.name;
      }
      // 处理访问连接
      this.url += cleanPath(); // http://openfeign-provider

      // 生成负载均衡代理类
      /**
       *  基于 url 的 lb
       */
      return (T) loadBalance( builder, context,  new HardCodedTarget<>(this.type, this.name, this.url) );
   }

   // 猜想是 兼容 url设置成服务名
   if (StringUtils.hasText(this.url) && !this.url.startsWith("http")) {
      this.url = "http://" + this.url;
   }

   String url = this.url + cleanPath();

   // 获取的是 哪个实例
   Client client = getOptional(context, Client.class);
   if (client != null) {
      if (client instanceof LoadBalancerFeignClient) {
         // not load balancing because we have a url,
         // but ribbon is on the classpath, so unwrap
         client = ((LoadBalancerFeignClient) client).getDelegate();
      }
      if (client instanceof FeignBlockingLoadBalancerClient) {
         // not load balancing because we have a url,
         // but Spring Cloud LoadBalancer is on the classpath, so unwrap
         client = ((FeignBlockingLoadBalancerClient) client).getDelegate();
      }
      //  设置调用客户端的意思吧
      builder.client(client);
   }
   Targeter targeter = get(context, Targeter.class);
   // 生成默认代理类
   /**
    *  此时 url 并没有先将 name（远程调用服务名） 拼接
    */
   return (T) targeter.target(this, builder, context, new HardCodedTarget<>(this.type, this.name, url));
}
```

通过Target.target(...)  创建一个JDk代理，这个代理的创建通过封装，把接口上的方法通过一个方法handler封装，因为都是通过Ribbon来调用的，因此此时仅仅返回一个代理对象即可，注入Spring容器中；这个代理对象，实际上就是代理了接口的方法的实现。















