package au.csiro.data61.aap.elf.types;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

final class ComparisonTest {
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
}
