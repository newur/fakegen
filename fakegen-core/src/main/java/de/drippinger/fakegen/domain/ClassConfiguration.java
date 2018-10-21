package de.drippinger.fakegen.domain;

import java.util.function.Supplier;

public class ClassConfiguration<T> {

    private final Class clazz;
    private final String fieldName;
    private final DomainConfiguration domainConfiguration;

    ClassConfiguration(Class<T> clazz, String fieldName, DomainConfiguration domainConfiguration) {
        this.clazz = clazz;
        this.fieldName = fieldName;
        this.domainConfiguration = domainConfiguration;
    }


    public DomainConfigurator use(Supplier<T> supplier) {
        domainConfiguration.fieldForClassShouldUse(fieldName, clazz, supplier);

        return DomainConfigurator.newConfigurator(domainConfiguration);
    }
}
