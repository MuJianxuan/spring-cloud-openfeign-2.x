package org.springframework.cloud.openfeign.analysis.provider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @Description
 * @Author: kongLiuYi
 * @Date: 2020/5/5 0005 16:44
 */
@SpringBootApplication
@EnableDiscoveryClient
public class SpringCloudOpenfeignProviderApplication {
	public static void main(String[] args) {
		SpringApplication.run(SpringCloudOpenfeignProviderApplication.class, args);
	}

}
