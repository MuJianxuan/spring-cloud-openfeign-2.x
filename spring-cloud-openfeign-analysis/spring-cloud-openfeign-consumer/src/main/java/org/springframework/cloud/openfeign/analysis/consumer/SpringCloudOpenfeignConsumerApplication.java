package org.springframework.cloud.openfeign.analysis.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @Description
 * @Author: kongLiuYi
 * @Date: 2020/5/5 0005 16:44
 */
@SpringBootApplication
// 这个注解的意思应该是暴露自己，把自己注册到注册中心去
@EnableDiscoveryClient
// 源码解析
@EnableFeignClients
public class SpringCloudOpenfeignConsumerApplication {
	public static void main(String[] args) {
		SpringApplication.run(SpringCloudOpenfeignConsumerApplication.class, args);
	}

}
