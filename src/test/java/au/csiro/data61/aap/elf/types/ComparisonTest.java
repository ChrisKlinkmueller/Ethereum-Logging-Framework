package au.csiro.data61.aap.elf.types;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.vavr.Tuple2;

final class ComparisonTest {
    
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
        final List<Tuple2<Type, Type>> typePairs = getTypePairs();
        final List<Boolean> assignabilityResults = List.of(
            true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, // BooleanType.isAssignableFrom(?)
            false, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, // DateType.isAssignableFrom(?)
            false, false, true, false, false, false, false, false, false, false, false, false, false, false, false, false, // FloatType.isAssignableFrom(?)
            false, false, false, true, false, false, false, false, false, false, false, false, false, false, false, false, // IntType.isAssignableFrom(?)
            false, false, false, false, true, false, false, false, false, false, false, false, false, false, false, false, // StringType.isAssignableFrom(?)
            false, false, false, false, false, true, false, false, false, false, false, false, false, false, false, false, // List<Boolean>.isAssignableFrom(?)
            false, false, false, false, false, false, true, false, false, false, false, false, false, false, false, false, // List<Date>.isAssignableFrom(?)
            false, false, false, false, false, false, false, true, false, false, false, false, false, false, false, false, // List<Float>.isAssignableFrom(?)
            false, false, false, false, false, false, false, false, true, false, false, false, false, false, false, false, // List<Int>.isAssignableFrom(?)
            false, false, false, false, false, false, false, false, false, true, false, false, false, false, false, false, // List<String>.isAssignableFrom(?)
            false, false, false, false, false, false, false, false, false, false, true, true, true, false, false, false,   // struct1.isAssignableFrom(?)
            false, false, false, false, false, false, false, false, false, false, false, true, true, false, false, false,  // struct2.isAssignableFrom(?)
            false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, // struct3.isAssignableFrom(?)
            false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, // struct4.isAssignableFrom(?)
            false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, true,  // struct5.isAssignableFrom(?)
            false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, true   // struct6.isAssignableFrom(?)
        );

        return IntStream.range(0, assignabilityResults.size())
            .mapToObj(i -> Arguments.of(
                typePairs.get(i)._1(), 
                typePairs.get(i)._2(), 
                assignabilityResults.get(i),
                String.format("Error in case: %s v. %s", typePairs.get(i)._1(), typePairs.get(i)._2())
            ));
    }

    private static Stream<Arguments> testEqualityAndHashCode() {
        final List<Tuple2<Type, Type>> typePairs = getTypePairs();
        final List<Boolean> equalityResults = List.of(
            true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, // BooleanType.equals(?)
            false, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, // DateType.equals(?)
            false, false, true, false, false, false, false, false, false, false, false, false, false, false, false, false, // FloatType.equals(?)
            false, false, false, true, false, false, false, false, false, false, false, false, false, false, false, false, // IntType.equals(?)
            false, false, false, false, true, false, false, false, false, false, false, false, false, false, false, false, // StringType.equals(?)
            false, false, false, false, false, true, false, false, false, false, false, false, false, false, false, false, // BooleanType[].equals(?)
            false, false, false, false, false, false, true, false, false, false, false, false, false, false, false, false, // DateType[].equals(?)
            false, false, false, false, false, false, false, true, false, false, false, false, false, false, false, false, // FloatType[].equals(?)
            false, false, false, false, false, false, false, false, true, false, false, false, false, false, false, false, // IntType[].equals(?)
            false, false, false, false, false, false, false, false, false, true, false, false, false, false, false, false, // StringType[].equals(?)
            false, false, false, false, false, false, false, false, false, false, true, false, false, false, false, false, // struct1.equals(?)
            false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, false, // struct2.equals(?)
            false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, // struct3.equals(?)
            false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, // struct4.equals(?)
            false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, true,  // struct5.equals(?)
            false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, true   // struct6.equals(?)
        ); 

        return IntStream.range(0, equalityResults.size())
            .mapToObj(i -> Arguments.of(
                typePairs.get(i)._1(), 
                typePairs.get(i)._2(), 
                equalityResults.get(i),
                String.format("Error in case: %s v. %s", typePairs.get(i)._1(), typePairs.get(i)._2())
            ));
    }

    private static List<Tuple2<Type, Type>> getTypePairs() {
        final Type boolList = new ListType(BooleanType.INSTANCE);
        final Type dateList = new ListType(DateType.INSTANCE);
        final Type floatList = new ListType(FloatType.INSTANCE);
        final Type intList = new ListType(IntType.INSTANCE);
        final Type stringList = new ListType(StringType.INSTANCE);

        final Type struct1 = new StructType(
            new StructField("a", FloatType.INSTANCE),
            new StructField("b", stringList)
        );

        final Type struct2 = new StructType(
            new StructField("a", FloatType.INSTANCE),
            new StructField("b", stringList),
            new StructField("c", struct1)
        );
        
        final Type struct3 = new StructType(
            new StructField("a", FloatType.INSTANCE),
            new StructField("b", stringList),
            new StructField("c", struct2)
        );

        final Type struct4 = new StructType(
            new StructField("a", FloatType.INSTANCE),
            new StructField("b", boolList)
        );

        final Type struct5 = new StructType(
            new StructField("c", StringType.INSTANCE),
            new StructField("d", intList)
        );

        final Type struct6 = new StructType(
            new StructField("c", StringType.INSTANCE),
            new StructField("d", intList)
        );

        return List.of(
            // BooleanType -> ?
            new Tuple2<>(BooleanType.INSTANCE, BooleanType.INSTANCE),
            new Tuple2<>(BooleanType.INSTANCE, DateType.INSTANCE),
            new Tuple2<>(BooleanType.INSTANCE, FloatType.INSTANCE),
            new Tuple2<>(BooleanType.INSTANCE, IntType.INSTANCE),
            new Tuple2<>(BooleanType.INSTANCE, StringType.INSTANCE),
            new Tuple2<>(BooleanType.INSTANCE, boolList),
            new Tuple2<>(BooleanType.INSTANCE, dateList),
            new Tuple2<>(BooleanType.INSTANCE, floatList),
            new Tuple2<>(BooleanType.INSTANCE, intList),
            new Tuple2<>(BooleanType.INSTANCE, stringList),
            new Tuple2<>(BooleanType.INSTANCE, struct1),
            new Tuple2<>(BooleanType.INSTANCE, struct2),
            new Tuple2<>(BooleanType.INSTANCE, struct3),
            new Tuple2<>(BooleanType.INSTANCE, struct4),
            new Tuple2<>(BooleanType.INSTANCE, struct5),
            new Tuple2<>(BooleanType.INSTANCE, struct6),

            // DateType -> ?
            new Tuple2<>(DateType.INSTANCE, BooleanType.INSTANCE),
            new Tuple2<>(DateType.INSTANCE, DateType.INSTANCE),
            new Tuple2<>(DateType.INSTANCE, FloatType.INSTANCE),
            new Tuple2<>(DateType.INSTANCE, IntType.INSTANCE),
            new Tuple2<>(DateType.INSTANCE, StringType.INSTANCE),
            new Tuple2<>(DateType.INSTANCE, boolList),
            new Tuple2<>(DateType.INSTANCE, dateList),
            new Tuple2<>(DateType.INSTANCE, floatList),
            new Tuple2<>(DateType.INSTANCE, intList),
            new Tuple2<>(DateType.INSTANCE, stringList),
            new Tuple2<>(DateType.INSTANCE, struct1),
            new Tuple2<>(DateType.INSTANCE, struct2),
            new Tuple2<>(DateType.INSTANCE, struct3),
            new Tuple2<>(DateType.INSTANCE, struct4),
            new Tuple2<>(DateType.INSTANCE, struct5),
            new Tuple2<>(DateType.INSTANCE, struct6),

            // FloatType -> ?
            new Tuple2<>(FloatType.INSTANCE, BooleanType.INSTANCE),
            new Tuple2<>(FloatType.INSTANCE, DateType.INSTANCE),
            new Tuple2<>(FloatType.INSTANCE, FloatType.INSTANCE),
            new Tuple2<>(FloatType.INSTANCE, IntType.INSTANCE),
            new Tuple2<>(FloatType.INSTANCE, StringType.INSTANCE),
            new Tuple2<>(FloatType.INSTANCE, boolList),
            new Tuple2<>(FloatType.INSTANCE, dateList),
            new Tuple2<>(FloatType.INSTANCE, floatList),
            new Tuple2<>(FloatType.INSTANCE, intList),
            new Tuple2<>(FloatType.INSTANCE, stringList),
            new Tuple2<>(FloatType.INSTANCE, struct1),
            new Tuple2<>(FloatType.INSTANCE, struct2),
            new Tuple2<>(FloatType.INSTANCE, struct3),
            new Tuple2<>(FloatType.INSTANCE, struct4),
            new Tuple2<>(FloatType.INSTANCE, struct5),
            new Tuple2<>(FloatType.INSTANCE, struct6),

            // IntType -> ?
            new Tuple2<>(IntType.INSTANCE, BooleanType.INSTANCE),
            new Tuple2<>(IntType.INSTANCE, DateType.INSTANCE),
            new Tuple2<>(IntType.INSTANCE, FloatType.INSTANCE),
            new Tuple2<>(IntType.INSTANCE, IntType.INSTANCE),
            new Tuple2<>(IntType.INSTANCE, StringType.INSTANCE),
            new Tuple2<>(IntType.INSTANCE, boolList),
            new Tuple2<>(IntType.INSTANCE, dateList),
            new Tuple2<>(IntType.INSTANCE, floatList),
            new Tuple2<>(IntType.INSTANCE, intList),
            new Tuple2<>(IntType.INSTANCE, stringList),
            new Tuple2<>(IntType.INSTANCE, struct1),
            new Tuple2<>(IntType.INSTANCE, struct2),
            new Tuple2<>(IntType.INSTANCE, struct3),
            new Tuple2<>(IntType.INSTANCE, struct4),
            new Tuple2<>(IntType.INSTANCE, struct5),
            new Tuple2<>(IntType.INSTANCE, struct6),

            // StringType -> ?
            new Tuple2<>(StringType.INSTANCE, BooleanType.INSTANCE),
            new Tuple2<>(StringType.INSTANCE, DateType.INSTANCE),
            new Tuple2<>(StringType.INSTANCE, FloatType.INSTANCE),
            new Tuple2<>(StringType.INSTANCE, IntType.INSTANCE),
            new Tuple2<>(StringType.INSTANCE, StringType.INSTANCE),
            new Tuple2<>(StringType.INSTANCE, boolList),
            new Tuple2<>(StringType.INSTANCE, dateList),
            new Tuple2<>(StringType.INSTANCE, floatList),
            new Tuple2<>(StringType.INSTANCE, intList),
            new Tuple2<>(StringType.INSTANCE, stringList),
            new Tuple2<>(StringType.INSTANCE, struct1),
            new Tuple2<>(StringType.INSTANCE, struct2),
            new Tuple2<>(StringType.INSTANCE, struct3),
            new Tuple2<>(StringType.INSTANCE, struct4),
            new Tuple2<>(StringType.INSTANCE, struct5),
            new Tuple2<>(StringType.INSTANCE, struct6),
            
            // boolList -> ?
            new Tuple2<>(boolList, BooleanType.INSTANCE),
            new Tuple2<>(boolList, DateType.INSTANCE),
            new Tuple2<>(boolList, FloatType.INSTANCE),
            new Tuple2<>(boolList, IntType.INSTANCE),
            new Tuple2<>(boolList, StringType.INSTANCE),
            new Tuple2<>(boolList, boolList),
            new Tuple2<>(boolList, dateList),
            new Tuple2<>(boolList, floatList),
            new Tuple2<>(boolList, intList),
            new Tuple2<>(boolList, stringList),
            new Tuple2<>(boolList, struct1),
            new Tuple2<>(boolList, struct2),
            new Tuple2<>(boolList, struct3),
            new Tuple2<>(boolList, struct4),
            new Tuple2<>(boolList, struct5),
            new Tuple2<>(boolList, struct6),
            
            // dataList -> ?
            new Tuple2<>(dateList, BooleanType.INSTANCE),
            new Tuple2<>(dateList, DateType.INSTANCE),
            new Tuple2<>(dateList, FloatType.INSTANCE),
            new Tuple2<>(dateList, IntType.INSTANCE),
            new Tuple2<>(dateList, StringType.INSTANCE),
            new Tuple2<>(dateList, boolList),
            new Tuple2<>(dateList, dateList),
            new Tuple2<>(dateList, floatList),
            new Tuple2<>(dateList, intList),
            new Tuple2<>(dateList, stringList),
            new Tuple2<>(boolList, struct1),
            new Tuple2<>(boolList, struct2),
            new Tuple2<>(boolList, struct3),
            new Tuple2<>(boolList, struct4),
            new Tuple2<>(boolList, struct5),
            new Tuple2<>(boolList, struct6),
            
            // floatList -> ?
            new Tuple2<>(floatList, BooleanType.INSTANCE),
            new Tuple2<>(floatList, DateType.INSTANCE),
            new Tuple2<>(floatList, FloatType.INSTANCE),
            new Tuple2<>(floatList, IntType.INSTANCE),
            new Tuple2<>(floatList, StringType.INSTANCE),
            new Tuple2<>(floatList, boolList),
            new Tuple2<>(floatList, dateList),
            new Tuple2<>(floatList, floatList),
            new Tuple2<>(floatList, intList),
            new Tuple2<>(floatList, stringList),
            new Tuple2<>(floatList, struct1),
            new Tuple2<>(floatList, struct2),
            new Tuple2<>(floatList, struct3),
            new Tuple2<>(floatList, struct4),
            new Tuple2<>(floatList, struct5),
            new Tuple2<>(floatList, struct6),
            
            // intList -> ?
            new Tuple2<>(intList, BooleanType.INSTANCE),
            new Tuple2<>(intList, DateType.INSTANCE),
            new Tuple2<>(intList, FloatType.INSTANCE),
            new Tuple2<>(intList, IntType.INSTANCE),
            new Tuple2<>(intList, StringType.INSTANCE),
            new Tuple2<>(intList, boolList),
            new Tuple2<>(intList, dateList),
            new Tuple2<>(intList, floatList),
            new Tuple2<>(intList, intList),
            new Tuple2<>(intList, stringList),
            new Tuple2<>(intList, struct1),
            new Tuple2<>(intList, struct2),
            new Tuple2<>(intList, struct3),
            new Tuple2<>(intList, struct4),
            new Tuple2<>(intList, struct5),
            new Tuple2<>(intList, struct6),
            
            // stringList -> ?
            new Tuple2<>(stringList, BooleanType.INSTANCE),
            new Tuple2<>(stringList, DateType.INSTANCE),
            new Tuple2<>(stringList, FloatType.INSTANCE),
            new Tuple2<>(stringList, IntType.INSTANCE),
            new Tuple2<>(stringList, StringType.INSTANCE),
            new Tuple2<>(stringList, boolList),
            new Tuple2<>(stringList, dateList),
            new Tuple2<>(stringList, floatList),
            new Tuple2<>(stringList, intList),
            new Tuple2<>(stringList, stringList),
            new Tuple2<>(stringList, struct1),
            new Tuple2<>(stringList, struct2),
            new Tuple2<>(stringList, struct3),
            new Tuple2<>(stringList, struct4),
            new Tuple2<>(stringList, struct5),
            new Tuple2<>(stringList, struct6),
            
            // struct1 -> ?
            new Tuple2<>(struct1, BooleanType.INSTANCE),
            new Tuple2<>(struct1, DateType.INSTANCE),
            new Tuple2<>(struct1, FloatType.INSTANCE),
            new Tuple2<>(struct1, IntType.INSTANCE),
            new Tuple2<>(struct1, StringType.INSTANCE),
            new Tuple2<>(struct1, boolList),
            new Tuple2<>(struct1, dateList),
            new Tuple2<>(struct1, floatList),
            new Tuple2<>(struct1, intList),
            new Tuple2<>(struct1, stringList),
            new Tuple2<>(struct1, struct1),
            new Tuple2<>(struct1, struct2),
            new Tuple2<>(struct1, struct3),
            new Tuple2<>(struct1, struct4),
            new Tuple2<>(struct1, struct5),
            new Tuple2<>(struct1, struct6),
            
            // struct2 -> ?
            new Tuple2<>(struct2, BooleanType.INSTANCE),
            new Tuple2<>(struct2, DateType.INSTANCE),
            new Tuple2<>(struct2, FloatType.INSTANCE),
            new Tuple2<>(struct2, IntType.INSTANCE),
            new Tuple2<>(struct2, StringType.INSTANCE),
            new Tuple2<>(struct2, boolList),
            new Tuple2<>(struct2, dateList),
            new Tuple2<>(struct2, floatList),
            new Tuple2<>(struct2, intList),
            new Tuple2<>(struct2, stringList),
            new Tuple2<>(struct2, struct1),
            new Tuple2<>(struct2, struct2),
            new Tuple2<>(struct2, struct3),
            new Tuple2<>(struct2, struct4),
            new Tuple2<>(struct2, struct5),
            new Tuple2<>(struct2, struct6),
            
            // struct3 -> ?
            new Tuple2<>(struct3, BooleanType.INSTANCE),
            new Tuple2<>(struct3, DateType.INSTANCE),
            new Tuple2<>(struct3, FloatType.INSTANCE),
            new Tuple2<>(struct3, IntType.INSTANCE),
            new Tuple2<>(struct3, StringType.INSTANCE),
            new Tuple2<>(struct3, boolList),
            new Tuple2<>(struct3, dateList),
            new Tuple2<>(struct3, floatList),
            new Tuple2<>(struct3, intList),
            new Tuple2<>(struct3, stringList),
            new Tuple2<>(struct3, struct1),
            new Tuple2<>(struct3, struct2),
            new Tuple2<>(struct3, struct3),
            new Tuple2<>(struct3, struct4),
            new Tuple2<>(struct3, struct5),
            new Tuple2<>(struct3, struct6),
            
            // struct4 -> ?
            new Tuple2<>(struct4, BooleanType.INSTANCE),
            new Tuple2<>(struct4, DateType.INSTANCE),
            new Tuple2<>(struct4, FloatType.INSTANCE),
            new Tuple2<>(struct4, IntType.INSTANCE),
            new Tuple2<>(struct4, StringType.INSTANCE),
            new Tuple2<>(struct4, boolList),
            new Tuple2<>(struct4, dateList),
            new Tuple2<>(struct4, floatList),
            new Tuple2<>(struct4, intList),
            new Tuple2<>(struct4, stringList),
            new Tuple2<>(struct4, struct1),
            new Tuple2<>(struct4, struct2),
            new Tuple2<>(struct4, struct3),
            new Tuple2<>(struct4, struct4),
            new Tuple2<>(struct4, struct5),
            new Tuple2<>(struct4, struct6),
            
            // struct5 -> ?
            new Tuple2<>(struct5, BooleanType.INSTANCE),
            new Tuple2<>(struct5, DateType.INSTANCE),
            new Tuple2<>(struct5, FloatType.INSTANCE),
            new Tuple2<>(struct5, IntType.INSTANCE),
            new Tuple2<>(struct5, StringType.INSTANCE),
            new Tuple2<>(struct5, boolList),
            new Tuple2<>(struct5, dateList),
            new Tuple2<>(struct5, floatList),
            new Tuple2<>(struct5, intList),
            new Tuple2<>(struct5, stringList),
            new Tuple2<>(struct5, struct1),
            new Tuple2<>(struct5, struct2),
            new Tuple2<>(struct5, struct3),
            new Tuple2<>(struct5, struct4),
            new Tuple2<>(struct5, struct5),
            new Tuple2<>(struct5, struct6),
            
            // struct6 -> ?
            new Tuple2<>(struct6, BooleanType.INSTANCE),
            new Tuple2<>(struct6, DateType.INSTANCE),
            new Tuple2<>(struct6, FloatType.INSTANCE),
            new Tuple2<>(struct6, IntType.INSTANCE),
            new Tuple2<>(struct6, StringType.INSTANCE),
            new Tuple2<>(struct6, boolList),
            new Tuple2<>(struct6, dateList),
            new Tuple2<>(struct6, floatList),
            new Tuple2<>(struct6, intList),
            new Tuple2<>(struct6, stringList),
            new Tuple2<>(struct6, struct1),
            new Tuple2<>(struct6, struct2),
            new Tuple2<>(struct6, struct3),
            new Tuple2<>(struct6, struct4),
            new Tuple2<>(struct6, struct5),
            new Tuple2<>(struct6, struct6)
        );
    }
}
