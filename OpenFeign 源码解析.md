# OpenFeign 源码解析

> 远程调用组件很重要！

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





















