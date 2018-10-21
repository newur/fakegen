package de.drippinger.fakegen.domain;

import java.util.List;

public class DomainConfigurator {

    DomainConfiguration domainConfiguration;

    private DomainConfigurator(DomainConfiguration domainConfiguration) {
        this.domainConfiguration = domainConfiguration;
    }

    public static DomainConfigurator newSimpleConfigurator() {
        return new DomainConfigurator(new SimpleDomainConfiguration());
    }

    public static DomainConfigurator newConfigurator(DomainConfiguration domainConfiguration) {
        return new DomainConfigurator(domainConfiguration);
    }

    public FieldConfiguration forField(String fieldName) {
        return new FieldConfiguration(fieldName, domainConfiguration);
    }

    public ClassConfiguration<Integer> forIntegerField(String fieldName) {
        return new FieldConfiguration(fieldName, domainConfiguration).ofType(Integer.class);
    }

    public DomainConfiguration done() {
        return domainConfiguration;
    }

}
