package org.springframework.cloud.openfeign.analysis.consumer.namedcontextfactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 每个服务名定义一个子context对象，各自维护定期更新服务列表，server选择等任务。
 * @author Rao
 * @Date 2021/7/21
 **/
@Configuration
@EnableConfigurationProperties
public class Test1ContextAutoConfiguration {

    String client = "test1";

    @Value("${testcontext.name:hello}")
    String name;


}
