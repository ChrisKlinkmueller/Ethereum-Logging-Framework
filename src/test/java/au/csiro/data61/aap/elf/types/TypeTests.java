package au.csiro.data61.aap.elf.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

final class TypeTests {
    private static final Type STRUCT_1 = new StructType(Map.of("a", FloatType.INSTANCE, "b", ListType.STRING_LIST));
    private static final Type STRUCT_2 = new StructType(Map.of("a", FloatType.INSTANCE, "b", ListType.STRING_LIST, "c", STRUCT_1));
    private static final Type STRUCT_3 = new StructType(Map.of("a", FloatType.INSTANCE, "b", ListType.STRING_LIST, "c", STRUCT_2));
    private static final Type STRUCT_4 = new StructType(Map.of("a", FloatType.INSTANCE, "b", ListType.BOOLEAN_LIST));
    private static final Type STRUCT_5 = new StructType(Map.of("c", StringType.INSTANCE, "d", ListType.INT_LIST));
    private static final Type STRUCT_6 = new StructType(Map.of("c", StringType.INSTANCE, "d", ListType.INT_LIST));
    
    private static final Type[] TYPES = new Type[] {
        BooleanType.INSTANCE,   // 0
        DateType.INSTANCE,      // 1
        FloatType.INSTANCE,     // 2
        IntType.INSTANCE,       // 3
        StringType.INSTANCE,    // 4
        ListType.BOOLEAN_LIST,  // 5
        ListType.DATE_LIST,     // 6
        ListType.FLOAT_LIST,    // 7
        ListType.INT_LIST,      // 8
        ListType.STRING_LIST,   // 9
        STRUCT_1,               // 10
        STRUCT_2,               // 11
        STRUCT_3,               // 12
        STRUCT_4,               // 13
        STRUCT_5,               // 14
        STRUCT_6,               // 15
        new ListType(STRUCT_1), // 16
        new ListType(STRUCT_2), // 17
        new ListType(STRUCT_3), // 18
        new ListType(STRUCT_4), // 19
        new ListType(STRUCT_5), // 20
        new ListType(STRUCT_6)  // 21
    };

    private static final Map<Integer, List<Integer>> ASSIGNABILITY_CASES = Map.of(
        10, List.of(11, 12),
        11, List.of(12),
        14, List.of(15),
        15, List.of(14),
        16, List.of(17, 18),
        17, List.of(18),
        20, List.of(21),
        21, List.of(20)
    );

    private static final Map<Integer, List<Integer>> EQUALITY_CASES = Map.of(
        14, List.of(15),
        15, List.of(14),
        20, List.of(21),
        21, List.of(20)
    );

    private static Stream<Arguments> testAssignability() {        
        return testCases(ASSIGNABILITY_CASES);
    }

    private static Stream<Arguments> testEqualityAndHashCode() {
        return testCases(EQUALITY_CASES);
    }

    private static Stream<Arguments> testCases(Map<Integer, List<Integer>> cases) {
        return IntStream.range(0, TYPES.length)
            .mapToObj(i -> testCases(cases, i))
            .flatMap(Function.identity());
    }

    private static Stream<Arguments> testCases(Map<Integer, List<Integer>> cases, int i) {
        return IntStream.range(0, TYPES.length)
            .mapToObj(j -> Arguments.of(
                TYPES[i], 
                TYPES[j], 
                i == j || cases.getOrDefault(i, Collections.emptyList()).contains(j), 
                String.format("Error in case: %s v. %s", TYPES[i], TYPES[j])
            ));
    }

    private static Stream<Arguments> testListType() {
        return Stream.of(
            Arguments.of(ListType.BOOLEAN_LIST, BooleanType.INSTANCE),
            Arguments.of(ListType.DATE_LIST, DateType.INSTANCE),
            Arguments.of(ListType.FLOAT_LIST, FloatType.INSTANCE),
            Arguments.of(ListType.INT_LIST, IntType.INSTANCE),
            Arguments.of(ListType.STRING_LIST, StringType.INSTANCE),
            Arguments.of(new ListType(ListType.BOOLEAN_LIST), ListType.BOOLEAN_LIST),
            Arguments.of(new ListType(ListType.DATE_LIST), ListType.DATE_LIST),
            Arguments.of(new ListType(ListType.FLOAT_LIST), ListType.FLOAT_LIST),
            Arguments.of(new ListType(ListType.INT_LIST), ListType.INT_LIST),
            Arguments.of(new ListType(ListType.STRING_LIST), ListType.STRING_LIST),
            Arguments.of(new ListType(STRUCT_1), STRUCT_1),
            Arguments.of(new ListType(STRUCT_2), STRUCT_2),
            Arguments.of(new ListType(STRUCT_3), STRUCT_3),
            Arguments.of(new ListType(STRUCT_4), STRUCT_4),
            Arguments.of(new ListType(STRUCT_5), STRUCT_5),
            Arguments.of(new ListType(STRUCT_6), STRUCT_6),
            Arguments.of(new ListType(new ListType(STRUCT_1)), new ListType(STRUCT_1)),
            Arguments.of(new ListType(new ListType(STRUCT_2)), new ListType(STRUCT_2)),
            Arguments.of(new ListType(new ListType(STRUCT_3)), new ListType(STRUCT_3)),
            Arguments.of(new ListType(new ListType(STRUCT_4)), new ListType(STRUCT_4)),
            Arguments.of(new ListType(new ListType(STRUCT_5)), new ListType(STRUCT_5)),
            Arguments.of(new ListType(new ListType(STRUCT_6)), new ListType(STRUCT_6))
        );
    }

    private static Stream<Arguments> testStructFieldComparison() {
        return Stream.of(
            Arguments.of(new StructField("a", BooleanType.INSTANCE), new StructField("a", BooleanType.INSTANCE), true),
            Arguments.of(new StructField("b", STRUCT_1), new StructField("b", STRUCT_1), true),
            Arguments.of(new StructField("c", new ListType(STRUCT_4)), new StructField("c", new ListType(STRUCT_4)), true),
            Arguments.of(new StructField("d", ListType.INT_LIST), new StructField("d", ListType.INT_LIST), true),
            Arguments.of(new StructField("a", BooleanType.INSTANCE), new StructField("g", BooleanType.INSTANCE), false),
            Arguments.of(new StructField("b", STRUCT_1), new StructField("h", STRUCT_1), false),
            Arguments.of(new StructField("c", new ListType(STRUCT_4)), new StructField("i", new ListType(STRUCT_4)), false),
            Arguments.of(new StructField("d", ListType.INT_LIST), new StructField("j", ListType.INT_LIST), false),
            Arguments.of(new StructField("a", BooleanType.INSTANCE), new StructField("a", STRUCT_2), false),
            Arguments.of(new StructField("b", STRUCT_1), new StructField("b", STRUCT_4), false),
            Arguments.of(new StructField("c", new ListType(STRUCT_4)), new StructField("c", FloatType.INSTANCE), false),
            Arguments.of(new StructField("d", ListType.INT_LIST), new StructField("d", ListType.DATE_LIST), false)
        );
    }

    @ParameterizedTest
    @MethodSource
    void testAssignability(Type superType, Type subType, boolean expectedAssignability, String caseId) {
        final boolean actualAssignability = superType.isAssignableFrom(subType);
        assertEquals(expectedAssignability, actualAssignability, caseId);
    }
    
    @ParameterizedTest
    @MethodSource
    void testEqualityAndHashCode(Type superType, Type subType, boolean expectedEquality, String caseId) {
        final boolean actualEquality = superType.equals(subType);
        assertEquals(expectedEquality, actualEquality);

        if (expectedEquality) {
            assertEquals(superType.hashCode(), subType.hashCode(), caseId);
        }
    }

    @ParameterizedTest
    @MethodSource
    void testListType(ListType listType, Type expectedBaseType) {
        assertEquals(expectedBaseType, listType.getBaseType());
    }

    @ParameterizedTest
    @MethodSource
    void testStructFieldComparison(StructField field1, StructField field2, boolean expectedResult) {
        assertEquals(field1.equals(field2), expectedResult);
        assertEquals(field2.equals(field1), expectedResult);

        if (expectedResult) {
            assertEquals(field1.hashCode(), field2.hashCode());
        }
    }

    private static Stream<Arguments> testStructFieldEqualsWithNoStructFields() {
        return Stream.of(
            Arguments.of(new Object()),
            Arguments.of(BigInteger.ONE),
            Arguments.of(new IllegalArgumentException()),
            Arguments.of(new Object[] { null })
        );
    }

    @ParameterizedTest
    @MethodSource
    void testStructFieldEqualsWithNoStructFields(Object obj) {
        final StructField field = new StructField("a", BooleanType.INSTANCE);
        assertNotEquals(field, obj);
    }

    @Test
    void testStructFieldConstructorThrowsException() {
        assertThrows(NullPointerException.class, () -> new StructField(null, FloatType.INSTANCE));
        assertThrows(IllegalArgumentException.class, () -> new StructField("", FloatType.INSTANCE));
        assertThrows(NullPointerException.class, () -> new StructField("biggy", null));
    }

    @Test
    void testStructType() {
        final StructField[] fieldArray = new StructField[] { 
            new StructField("a", DateType.INSTANCE),
            new StructField("b", STRUCT_1),
            new StructField("c", ListType.FLOAT_LIST),
            new StructField("d", new ListType(STRUCT_4)),
            new StructField("e", new ListType(ListType.INT_LIST)),
            new StructField("f", new ListType(new ListType(STRUCT_6)))
        };
        final List<StructField> fieldList = new ArrayList<>(List.of(fieldArray));

        verifyStructType(new StructType(fieldArray), fieldList);
        verifyStructType(new StructType(fieldList), fieldList);

        fieldList.add(new StructField("f", new ListType(new ListType(STRUCT_6))));
        assertThrows(IllegalArgumentException.class, () -> new StructType(fieldList));
    }

    private void verifyStructType(StructType type, List<StructField> fields) {
        assertEquals(type.getFields().size(), fields.size());

        fields.stream().allMatch(f1 -> type.fieldStream().anyMatch(f2 -> f1.equals(f2)));
        type.fieldStream().allMatch(f1 -> fields.stream().anyMatch(f2 -> f1.equals(f1)));
    }
}
