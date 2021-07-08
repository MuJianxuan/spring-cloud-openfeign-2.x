package org.springframework.cloud.openfeign.analysis.consumer.config;

import com.github.xiaoymin.swaggerbootstrapui.annotations.EnableSwaggerBootstrapUI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * 第三方swagger增强API注解
 *
 */
@Configuration
@EnableSwagger2
@EnableSwaggerBootstrapUI
public class SwaggerConfig {
    @Bean
    public Docket createRestApi() {
        /**
         * @Author
         * @Description 关于分组接口,可后期根据多模块,拆解为根据模块来管理API文档
         * @Date 10:18 2019/3/15
         * @Param []
         * @return springfox.documentation.spring.web.plugins.Docket
         **/
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("后台管理接口")
                .apiInfo(apiInfo())
                .host("localhost:82")
                // 扫描接口即 Controller 包
                .select().apis(RequestHandlerSelectors.basePackage("com.kly.admin.rest"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                // 文档页标题
                .title("swagger-bootstrap-ui RESTFul APIs")
                // 详细信息
                .description("系统化信息化XX平台,为您提供最优质的服务")
                // 网站地址
                .termsOfServiceUrl("")
                // 联系人信息
                .contact(new Contact("demo",
                        "localhost:8082",
                        "developer@mail.com"))
                // 版本
                .version("1.0")
                .build();
    }
}
