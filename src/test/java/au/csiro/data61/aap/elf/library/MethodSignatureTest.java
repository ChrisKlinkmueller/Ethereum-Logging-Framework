package au.csiro.data61.aap.elf.library;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import au.csiro.data61.aap.elf.types.BooleanType;
import au.csiro.data61.aap.elf.types.DateType;
import au.csiro.data61.aap.elf.types.FloatType;
import au.csiro.data61.aap.elf.types.IntType;
import au.csiro.data61.aap.elf.types.StringType;
import au.csiro.data61.aap.elf.types.StructField;
import au.csiro.data61.aap.elf.types.StructType;
import au.csiro.data61.aap.elf.types.Type;

public class MethodSignatureTest {
    
    @ParameterizedTest
    @MethodSource
    void new_CorrectInitialization(String name, List<Type> parameterTypes) {
        MethodSignature signature = new MethodSignature(name, parameterTypes);
        this.verifySignature(signature, name, parameterTypes);

        signature = new MethodSignature(name, parameterTypes.toArray(Type[]::new));
        this.verifySignature(signature, name, parameterTypes);
    }

    private void verifySignature(MethodSignature signature, String name, List<Type> parameterTypes) {
        assertEquals(name, signature.getName());
        assertEquals(parameterTypes.size(), signature.getParameterTypes().size());
        IntStream.range(0, parameterTypes.size())
            .forEach(i -> assertEquals(parameterTypes.get(i), signature.getParameterTypes().get(i)));
    }

    @ParameterizedTest
    @MethodSource
    void equals_ComparesCorrectly(MethodSignature sig1, MethodSignature sig2, boolean areEqual) {
        assertEquals(areEqual, sig1.equals(sig2));
    }

    @ParameterizedTest
    @MethodSource
    void hashCode_IsSameForEqualSignatures(MethodSignature sig1, MethodSignature sig2, boolean areEqual) {
        if (areEqual) {
            assertEquals(sig1.hashCode(), sig2.hashCode());
        }
    }

    @ParameterizedTest
    @MethodSource
    void isAssignableFrom_ComparesCorrectly(MethodSignature sig1, MethodSignature sig2, boolean isAssignableFrom) {
        assertEquals(isAssignableFrom, sig1.isAssignableFrom(sig2));
    }

    private static Stream<Arguments> new_CorrectInitialization() {
        return Stream.of(
            Arguments.of("add", List.of(IntType.INSTANCE, IntType.INSTANCE)),
            Arguments.of("transform", List.of(StringType.INSTANCE, FloatType.INSTANCE)),
            Arguments.of("extract", List.of(DateType.INSTANCE, new StructType(new StructField("value1", FloatType.INSTANCE), new StructField("value2", IntType.INSTANCE))))
        );
    }

    private static Stream<Arguments> equals_ComparesCorrectly() {
        final MethodSignature sig1 = new MethodSignature("add", IntType.INSTANCE, IntType.INSTANCE);
        final MethodSignature sig2 = new MethodSignature("add", IntType.INSTANCE, IntType.INSTANCE);
        final MethodSignature sig3 = new MethodSignature("subtract", IntType.INSTANCE, IntType.INSTANCE);
        final MethodSignature sig4 = new MethodSignature("transform", StringType.INSTANCE, FloatType.INSTANCE);
        final MethodSignature sig5 = new MethodSignature("extract", DateType.INSTANCE, new StructType(new StructField("value1", FloatType.INSTANCE), new StructField("value2", IntType.INSTANCE)));
        final MethodSignature sig6 = new MethodSignature("extract", DateType.INSTANCE, new StructType(new StructField("value1", FloatType.INSTANCE), new StructField("value2", IntType.INSTANCE), new StructField("value3", BooleanType.INSTANCE)));

        return Stream.of(
            Arguments.of(sig1, sig1, true),
            Arguments.of(sig1, sig2, true),
            Arguments.of(sig1, sig3, false),
            Arguments.of(sig1, sig4, false),
            Arguments.of(sig1, sig5, false),
            Arguments.of(sig1, sig6, false),
            Arguments.of(sig2, sig1, true),
            Arguments.of(sig2, sig2, true),
            Arguments.of(sig2, sig3, false),
            Arguments.of(sig2, sig4, false),
            Arguments.of(sig2, sig5, false),
            Arguments.of(sig2, sig6, false),
            Arguments.of(sig3, sig1, false),
            Arguments.of(sig3, sig2, false),
            Arguments.of(sig3, sig3, true),
            Arguments.of(sig3, sig4, false),
            Arguments.of(sig3, sig5, false),
            Arguments.of(sig3, sig6, false),
            Arguments.of(sig4, sig1, false),
            Arguments.of(sig4, sig2, false),
            Arguments.of(sig4, sig3, false),
            Arguments.of(sig4, sig4, true),
            Arguments.of(sig4, sig5, false),
            Arguments.of(sig4, sig6, false),
            Arguments.of(sig5, sig1, false),
            Arguments.of(sig5, sig2, false),
            Arguments.of(sig5, sig3, false),
            Arguments.of(sig5, sig4, false),
            Arguments.of(sig5, sig5, true),
            Arguments.of(sig5, sig6, false),
            Arguments.of(sig6, sig1, false),
            Arguments.of(sig6, sig2, false),
            Arguments.of(sig6, sig3, false),
            Arguments.of(sig6, sig4, false),
            Arguments.of(sig6, sig5, false),
            Arguments.of(sig6, sig6, true)
        );
    }

    private static Stream<Arguments> hashCode_IsSameForEqualSignatures() {
        return equals_ComparesCorrectly();
    }

    private static Stream<Arguments> isAssignableFrom_ComparesCorrectly() {
        final MethodSignature sig1 = new MethodSignature("add", IntType.INSTANCE, IntType.INSTANCE);
        final MethodSignature sig2 = new MethodSignature("add", IntType.INSTANCE, IntType.INSTANCE);
        final MethodSignature sig3 = new MethodSignature("subtract", IntType.INSTANCE, IntType.INSTANCE);
        final MethodSignature sig4 = new MethodSignature("transform", StringType.INSTANCE, FloatType.INSTANCE);
        final MethodSignature sig5 = new MethodSignature("extract", DateType.INSTANCE, new StructType(new StructField("value1", FloatType.INSTANCE), new StructField("value2", IntType.INSTANCE)));
        final MethodSignature sig6 = new MethodSignature("extract", DateType.INSTANCE, new StructType(new StructField("value1", FloatType.INSTANCE), new StructField("value2", IntType.INSTANCE), new StructField("value3", BooleanType.INSTANCE)));

        return Stream.of(
            Arguments.of(sig1, sig1, true),
            Arguments.of(sig1, sig2, true),
            Arguments.of(sig1, sig3, false),
            Arguments.of(sig1, sig4, false),
            Arguments.of(sig1, sig5, false),
            Arguments.of(sig1, sig6, false),
            Arguments.of(sig2, sig1, true),
            Arguments.of(sig2, sig2, true),
            Arguments.of(sig2, sig3, false),
            Arguments.of(sig2, sig4, false),
            Arguments.of(sig2, sig5, false),
            Arguments.of(sig2, sig6, false),
            Arguments.of(sig3, sig1, false),
            Arguments.of(sig3, sig2, false),
            Arguments.of(sig3, sig3, true),
            Arguments.of(sig3, sig4, false),
            Arguments.of(sig3, sig5, false),
            Arguments.of(sig3, sig6, false),
            Arguments.of(sig4, sig1, false),
            Arguments.of(sig4, sig2, false),
            Arguments.of(sig4, sig3, false),
            Arguments.of(sig4, sig4, true),
            Arguments.of(sig4, sig5, false),
            Arguments.of(sig4, sig6, false),
            Arguments.of(sig5, sig1, false),
            Arguments.of(sig5, sig2, false),
            Arguments.of(sig5, sig3, false),
            Arguments.of(sig5, sig4, false),
            Arguments.of(sig5, sig5, true),
            Arguments.of(sig5, sig6, true),
            Arguments.of(sig6, sig1, false),
            Arguments.of(sig6, sig2, false),
            Arguments.of(sig6, sig3, false),
            Arguments.of(sig6, sig4, false),
            Arguments.of(sig6, sig5, false),
            Arguments.of(sig6, sig6, true)
        );
    }

}