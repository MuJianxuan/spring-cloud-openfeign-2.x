package org.springframework.cloud.openfeign.analysis.consumer.namedcontextfactory;

import org.springframework.cloud.context.named.NamedContextFactory;

/**
 * @author Rao
 * @Date 2021/7/21
 **/
public class TestSpecification implements NamedContextFactory.Specification {

    private String name;

    private Class<?>[] configuration;

    public TestSpecification(String name, Class<?>[] configuration) {
        this.name = name;
        this.configuration = configuration;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Class<?>[] getConfiguration() {
        return configuration;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setConfiguration(Class<?>[] configuration) {
        this.configuration = configuration;
    }
}
