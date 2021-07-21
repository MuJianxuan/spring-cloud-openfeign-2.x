package org.springframework.cloud.openfeign.analysis.consumer.namedcontextfactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Rao
 * @Date 2021/7/21
 **/
@Configuration
@EnableConfigurationProperties
public class CommonContextAutoConfiguration {

    String client = "common";

    @Value("${testcontext.name:hello}")
    String name;



}
