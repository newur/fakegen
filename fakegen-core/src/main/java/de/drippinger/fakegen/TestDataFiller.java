package de.drippinger.fakegen;

import de.drippinger.fakegen.domain.DomainConfiguration;
import de.drippinger.fakegen.exception.FakegenException;
import de.drippinger.fakegen.filler.BasicObjectFiller;
import de.drippinger.fakegen.filler.ObjectFiller;
import de.drippinger.fakegen.uninstanciable.DynamicClassGenerator;
import de.drippinger.fakegen.util.ReflectionUtils;
import lombok.SneakyThrows;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static de.drippinger.fakegen.exception.ExceptionHelper.createExceptionMessage;
import static de.drippinger.fakegen.util.ReflectionUtils.*;

/**
 * @author Dennis Rippinger
 */
@SuppressWarnings("unchecked")
public class TestDataFiller {

    private final ObjectFiller objectFiller;

    private final DynamicClassGenerator dynamicClassGenerator;

    private final Map<Type, Method> objectFillerFactoryMethods;

    public TestDataFiller() {
        this(new BasicObjectFiller());
    }

    public TestDataFiller(Long seed) {
        this(new BasicObjectFiller(seed));
    }

    public TestDataFiller(Long seed, DomainConfiguration domainConfiguration) {
        this(new BasicObjectFiller(seed, domainConfiguration));
    }

    public TestDataFiller(DomainConfiguration domainConfiguration) {
        this(new BasicObjectFiller(domainConfiguration));
    }

    public TestDataFiller(ObjectFiller objectFiller) {
        this.objectFiller = objectFiller;
        this.dynamicClassGenerator = new DynamicClassGenerator();
        this.objectFillerFactoryMethods = getPotentialRandomFactoryMethods(objectFiller);
    }

    public <T> T createRandomFilledInstanceAndApply(Class<T> clazz, Consumer<T> consumer) {
        T randomFilledInstance = createRandomFilledInstanceInternal(clazz, 0);
        consumer.accept(randomFilledInstance);

        return randomFilledInstance;
    }

    public <T> T createRandomFilledInstance(Class<T> clazz) {
        return createRandomFilledInstanceInternal(clazz, 0);
    }


    public <T> T createRandomFilledByFactory(Class<T> clazz) {
        try {

            Optional<Method> possibleFactoryMethod = Arrays.stream(clazz.getMethods())
                    .filter(ReflectionUtils::isStaticMethod)
                    .filter(method -> method.getReturnType().equals(clazz))
                    .filter(method -> !Arrays.stream(method.getParameterTypes()).anyMatch(clazz::equals))
                    .findFirst();

            if (possibleFactoryMethod.isPresent()) {
                Method method = possibleFactoryMethod.get();

                Object[] instances = Arrays.stream(method.getParameterTypes())
                        .map(this::createRandomFilledInstance)
                        .toArray();

                return (T) method.invoke(null, instances);
            }

        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new FakegenException("Could not invoke factory method", e);
        }

        return null;

    }

    public <T> T createRandomFilledByFactory(Class<T> clazz, MethodHolder methodHolder) {
        try {
            Method method = methodHolder.createMethod(clazz);

            Object[] instances = Arrays.stream(methodHolder.getParameterTypes())
                    .map(this::createRandomFilledInstance)
                    .toArray();

            return (T) method.invoke(null, instances);

        } catch (NoSuchMethodException e) {
            String message = createExceptionMessage(clazz, methodHolder);
            throw new FakegenException(message, e);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new FakegenException("Could not invoke factory method", e);
        }

    }


    public String getSeed() {
        return objectFiller.getSeed();
    }


    private <T> T createRandomFilledInstanceInternal(Class<T> clazz, int recursionCounter) {

        if (objectFillerFactoryMethods.containsKey(clazz)) {
            Method method = objectFillerFactoryMethods.get(clazz);
            return (T) callFactoryMethod(null, method);
        } else if (clazz.isEnum()) {
            return (T) objectFiller.createEnum(null, clazz);
        } else if (clazz.isInterface() || isAbstractClass(clazz)) {
            T simpleImpl = dynamicClassGenerator.createSimpleInstanceOfInterfaceOrAbstract(clazz);
            fill(simpleImpl, simpleImpl.getClass(), recursionCounter);
            return simpleImpl;
        } else {
            return (T) extractPlainConstructor(clazz)
                    .map(this::newInstance)
                    .map(instance -> fill(instance, clazz, recursionCounter))
                    .orElse(null);
        }
    }

    private <T> Object fill(Object instance, Class<T> clazz, int recursionCounter) {

        getAllFields(clazz)
                .filter(ReflectionUtils::isNotFinal)
                .filter(ReflectionUtils::isNotRelevantForCoverage)
                .forEach(field -> fillField(instance, field, recursionCounter));

        return instance;
    }

    @SneakyThrows
    private void fillField(Object instance, Field field, int recursionCounter) {
        field.setAccessible(true);
        Object value = createRandomValueForField(field);

        if (value == null && recursionCounter < 1) {
            recursionCounter++;
            value = createRandomFilledInstanceInternal(field.getType(), recursionCounter);
        }

        if (value != null) {
            field.set(instance, value);
        }
    }

    private Object createRandomValueForField(Field field) {

        Class<?> type = field.getType();
        String fieldName = field.getName();

        if (type.isEnum()) {
            return objectFiller.createEnum(fieldName, type);
        }

        Method method = objectFillerFactoryMethods.get(type);
        if (method != null) {
            return callFactoryMethod(fieldName, method);
        }

        return null;
    }

    @SneakyThrows
    private Object callFactoryMethod(String fieldName, Method method) {
        return method.invoke(objectFiller, fieldName);
    }

    @SneakyThrows
    private Object newInstance(Constructor constructor) {
        return constructor.newInstance();
    }

    private Optional<Constructor> extractPlainConstructor(Class clazz) {
        for (Constructor<?> constructor : clazz.getConstructors()) {
            if (constructor.getParameterCount() == 0) {
                return Optional.of(constructor);
            }
        }
        return Optional.empty();
    }

}
