package de.drippinger.fakegen.domain;

public class FieldConfiguration {

    private final String fieldName;
    private final DomainConfiguration domainConfiguration;

    FieldConfiguration(String fieldName, DomainConfiguration domainConfiguration) {
        this.fieldName = fieldName;
        this.domainConfiguration = domainConfiguration;
    }

    <T> ClassConfiguration<T> ofType(Class<T> clazz) {
        return new ClassConfiguration<>(clazz, this.fieldName, this.domainConfiguration);
    }
}
