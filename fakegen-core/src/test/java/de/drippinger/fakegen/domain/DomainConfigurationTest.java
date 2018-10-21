package de.drippinger.fakegen.domain;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Dennis Rippinger (msg systems ag) 2018
 */
class DomainConfigurationTest {

    @Test
    void should_add_and_get_for_string_fluent() {
        DomainConfiguration domain = DomainConfigurator.newSimpleConfigurator()
                .forField("someField")
                .ofType(String.class)
                .use(() -> "someName")
                .done();

        String someName = domain.getSupplier("someField", String.class)
                .map(Supplier::get)
                .orElseThrow(AssertionError::new);

        assertThat(someName).isEqualTo("someName");
    }

    @Test
    void should_add_and_get_for_integer_fluent() {
        DomainConfiguration domain = DomainConfigurator.newSimpleConfigurator()
                .forIntegerField("age")
                .use(() -> 1)
                .done();

        Integer age = domain.getSupplier("age", Integer.class)
                .map(Supplier::get)
                .orElseThrow(AssertionError::new);

        assertThat(age).isEqualTo(1);
    }

    @Test
    void should_add_and_get_for_string() {
        DomainConfiguration domain = new SimpleDomainConfiguration();

        domain.fieldForStringShouldUse("someField", () -> "someName");

        String someName = domain.getSupplier("someField", String.class)
                .map(Supplier::get)
                .orElse("");

        assertThat(someName).isEqualTo("someName");
    }

    @Test
    void should_add_and_get_for_a_object_type() {
        DomainConfiguration domain = new SimpleDomainConfiguration();

        domain.fieldForClassShouldUse("someField", Integer.class, () -> 1);

        Integer someInteger = domain.getSupplier("someField", Integer.class)
                .map(Supplier::get)
                .orElse(0);

        assertThat(someInteger).isEqualTo(1);
    }

    @Test
    void should_add_and_get_for_a_primitive_type() {
        DomainConfiguration domain = new SimpleDomainConfiguration();

        domain.fieldForClassShouldUse("someField", int.class, () -> 1);

        Integer someInteger = domain.getSupplier("someField", Integer.class)
                .map(Supplier::get)
                .orElse(0);

        assertThat(someInteger).isEqualTo(1);
    }

}
