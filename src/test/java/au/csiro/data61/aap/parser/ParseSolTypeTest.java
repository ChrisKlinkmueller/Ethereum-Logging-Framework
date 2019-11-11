package au.csiro.data61.aap.parser;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import au.csiro.data61.aap.spec.types.SolidityAddress;
import au.csiro.data61.aap.spec.types.SolidityArray;
import au.csiro.data61.aap.spec.types.SolidityBool;
import au.csiro.data61.aap.spec.types.SolidityBytes;
import au.csiro.data61.aap.spec.types.SolidityFixed;
import au.csiro.data61.aap.spec.types.SolidityInteger;
import au.csiro.data61.aap.spec.types.SolidityString;
import au.csiro.data61.aap.spec.types.SolidityType;

/**
 * ParseSolTypeTest
 */
public class ParseSolTypeTest {

    @ParameterizedTest
    @MethodSource("validBasicTypeDefinitionCases")
    public void testBasicTypeDefinitionCases(String definition, BiFunction<String, Boolean, SolidityType> cast, SolidityType expectedType) {
        SolidityType type = cast.apply(definition, false);
        assertTrue(type != null, String.format("Test case: '%s'", definition));
        assertTrue(type.equals(expectedType), String.format("Test case: '%s'", definition));
    }
    
    @ParameterizedTest
    @MethodSource("validArrayTypeDefinitionCases")
    public void testArrayTypeDefinitionCases(String definition, BiFunction<String, Boolean, SolidityType> cast, SolidityType expectedType) {
        SolidityType type = AnalyzerUtils.parseArrayDefinition(definition, cast, false);
        assertTrue(type != null, String.format("Test case: '%s'", definition));
        assertTrue(type.equals(expectedType), String.format("Test case: '%s'", definition));
    }

    @ParameterizedTest
    @MethodSource("equalsAndHashCases")
    public void testEqualsAndHashcode(SolidityType type1, SolidityType type2, boolean equal) {
        if (equal) {
            assertTrue(type1.equals(type2));
            assertTrue(type1.hashCode() == type2.hashCode());
        }
        else {
            assertFalse(type1.equals(type2));
            assertFalse(type1.hashCode() == type2.hashCode());
        }        
    }

    private static Stream<Arguments> validBasicTypeDefinitionCases() {
        return Stream.of(
            Arguments.of("address", addressCast(), SolidityAddress.DEFAULT_INSTANCE),
            Arguments.of("bool", boolCast(), SolidityBool.DEFAULT_INSTANCE),
            Arguments.of("string", stringCast(), SolidityString.DEFAULT_INSTANCE),
            Arguments.of("int", integerCast(), new SolidityInteger(true)),
            Arguments.of("uint", integerCast(), new SolidityInteger(false)),
            Arguments.of("int8", integerCast(), new SolidityInteger(true, 8)),
            Arguments.of("int16", integerCast(), new SolidityInteger(true, 16)),
            Arguments.of("int24", integerCast(), new SolidityInteger(true, 24)),
            Arguments.of("int32", integerCast(), new SolidityInteger(true, 32)),
            Arguments.of("int40", integerCast(), new SolidityInteger(true, 40)),
            Arguments.of("int48", integerCast(), new SolidityInteger(true, 48)),
            Arguments.of("int56", integerCast(), new SolidityInteger(true, 56)),
            Arguments.of("int64", integerCast(), new SolidityInteger(true, 64)),
            Arguments.of("int72", integerCast(), new SolidityInteger(true, 72)),
            Arguments.of("int80", integerCast(), new SolidityInteger(true, 80)),
            Arguments.of("int88", integerCast(), new SolidityInteger(true, 88)),
            Arguments.of("int96", integerCast(), new SolidityInteger(true, 96)),
            Arguments.of("int104", integerCast(), new SolidityInteger(true, 104)),
            Arguments.of("int112", integerCast(), new SolidityInteger(true, 112)),
            Arguments.of("int120", integerCast(), new SolidityInteger(true, 120)),
            Arguments.of("int128", integerCast(), new SolidityInteger(true, 128)),
            Arguments.of("int136", integerCast(), new SolidityInteger(true, 136)),
            Arguments.of("int144", integerCast(), new SolidityInteger(true, 144)),
            Arguments.of("int152", integerCast(), new SolidityInteger(true, 152)),
            Arguments.of("int160", integerCast(), new SolidityInteger(true, 160)),
            Arguments.of("int168", integerCast(), new SolidityInteger(true, 168)),
            Arguments.of("int176", integerCast(), new SolidityInteger(true, 176)),
            Arguments.of("int184", integerCast(), new SolidityInteger(true, 184)),
            Arguments.of("int192", integerCast(), new SolidityInteger(true, 192)),
            Arguments.of("int200", integerCast(), new SolidityInteger(true, 200)),
            Arguments.of("int208", integerCast(), new SolidityInteger(true, 208)),
            Arguments.of("int216", integerCast(), new SolidityInteger(true, 216)),
            Arguments.of("int224", integerCast(), new SolidityInteger(true, 224)),
            Arguments.of("int232", integerCast(), new SolidityInteger(true, 232)),
            Arguments.of("int240", integerCast(), new SolidityInteger(true, 240)),
            Arguments.of("int248", integerCast(), new SolidityInteger(true, 248)),
            Arguments.of("int256", integerCast(), new SolidityInteger(true, 256)),
            Arguments.of("uint8", integerCast(), new SolidityInteger(false, 8)),
            Arguments.of("uint16", integerCast(), new SolidityInteger(false, 16)),
            Arguments.of("uint24", integerCast(), new SolidityInteger(false, 24)),
            Arguments.of("uint32", integerCast(), new SolidityInteger(false, 32)),
            Arguments.of("uint40", integerCast(), new SolidityInteger(false, 40)),
            Arguments.of("uint48", integerCast(), new SolidityInteger(false, 48)),
            Arguments.of("uint56", integerCast(), new SolidityInteger(false, 56)),
            Arguments.of("uint64", integerCast(), new SolidityInteger(false, 64)),
            Arguments.of("uint72", integerCast(), new SolidityInteger(false, 72)),
            Arguments.of("uint80", integerCast(), new SolidityInteger(false, 80)),
            Arguments.of("uint88", integerCast(), new SolidityInteger(false, 88)),
            Arguments.of("uint96", integerCast(), new SolidityInteger(false, 96)),
            Arguments.of("uint104", integerCast(), new SolidityInteger(false, 104)),
            Arguments.of("uint112", integerCast(), new SolidityInteger(false, 112)),
            Arguments.of("uint120", integerCast(), new SolidityInteger(false, 120)),
            Arguments.of("uint128", integerCast(), new SolidityInteger(false, 128)),
            Arguments.of("uint136", integerCast(), new SolidityInteger(false, 136)),
            Arguments.of("uint144", integerCast(), new SolidityInteger(false, 144)),
            Arguments.of("uint152", integerCast(), new SolidityInteger(false, 152)),
            Arguments.of("uint160", integerCast(), new SolidityInteger(false, 160)),
            Arguments.of("uint168", integerCast(), new SolidityInteger(false, 168)),
            Arguments.of("uint176", integerCast(), new SolidityInteger(false, 176)),
            Arguments.of("uint184", integerCast(), new SolidityInteger(false, 184)),
            Arguments.of("uint192", integerCast(), new SolidityInteger(false, 192)),
            Arguments.of("uint200", integerCast(), new SolidityInteger(false, 200)),
            Arguments.of("uint208", integerCast(), new SolidityInteger(false, 208)),
            Arguments.of("uint216", integerCast(), new SolidityInteger(false, 216)),
            Arguments.of("uint224", integerCast(), new SolidityInteger(false, 224)),
            Arguments.of("uint232", integerCast(), new SolidityInteger(false, 232)),
            Arguments.of("uint240", integerCast(), new SolidityInteger(false, 240)),
            Arguments.of("uint248", integerCast(), new SolidityInteger(false, 248)),
            Arguments.of("uint256", integerCast(), new SolidityInteger(false, 256)),
            Arguments.of("fixed16x33", fixedCast(), new SolidityFixed(true, 16, 33)),
            Arguments.of("fixed16x71", fixedCast(), new SolidityFixed(true, 16, 71)),
            Arguments.of("fixed32x2", fixedCast(), new SolidityFixed(true, 32, 2)),
            Arguments.of("fixed32x24", fixedCast(), new SolidityFixed(true, 32, 24)),
            Arguments.of("fixed32x73", fixedCast(), new SolidityFixed(true, 32, 73)),
            Arguments.of("fixed40x42", fixedCast(), new SolidityFixed(true, 40, 42)),
            Arguments.of("fixed40x53", fixedCast(), new SolidityFixed(true, 40, 53)),
            Arguments.of("fixed48x50", fixedCast(), new SolidityFixed(true, 48, 50)),
            Arguments.of("fixed48x64", fixedCast(), new SolidityFixed(true, 48, 64)),
            Arguments.of("fixed56x34", fixedCast(), new SolidityFixed(true, 56, 34)),
            Arguments.of("fixed56x43", fixedCast(), new SolidityFixed(true, 56, 43)),
            Arguments.of("fixed56x56", fixedCast(), new SolidityFixed(true, 56, 56)),
            Arguments.of("fixed64x28", fixedCast(), new SolidityFixed(true, 64, 28)),
            Arguments.of("fixed64x55", fixedCast(), new SolidityFixed(true, 64, 55)),
            Arguments.of("fixed88x28", fixedCast(), new SolidityFixed(true, 88, 28)),
            Arguments.of("fixed96x3", fixedCast(), new SolidityFixed(true, 96, 3)),
            Arguments.of("fixed96x76", fixedCast(), new SolidityFixed(true, 96, 76)),
            Arguments.of("fixed104x42", fixedCast(), new SolidityFixed(true, 104, 42)),
            Arguments.of("fixed104x61", fixedCast(), new SolidityFixed(true, 104, 61)),
            Arguments.of("fixed104x69", fixedCast(), new SolidityFixed(true, 104, 69)),
            Arguments.of("fixed104x72", fixedCast(), new SolidityFixed(true, 104, 72)),
            Arguments.of("fixed104x76", fixedCast(), new SolidityFixed(true, 104, 76)),
            Arguments.of("fixed112x52", fixedCast(), new SolidityFixed(true, 112, 52)),
            Arguments.of("fixed112x62", fixedCast(), new SolidityFixed(true, 112, 62)),
            Arguments.of("fixed120x9", fixedCast(), new SolidityFixed(true, 120, 9)),
            Arguments.of("fixed120x19", fixedCast(), new SolidityFixed(true, 120, 19)),
            Arguments.of("fixed120x21", fixedCast(), new SolidityFixed(true, 120, 21)),
            Arguments.of("fixed120x32", fixedCast(), new SolidityFixed(true, 120, 32)),
            Arguments.of("fixed120x43", fixedCast(), new SolidityFixed(true, 120, 43)),
            Arguments.of("fixed120x44", fixedCast(), new SolidityFixed(true, 120, 44)),
            Arguments.of("fixed144x5", fixedCast(), new SolidityFixed(true, 144, 5)),
            Arguments.of("fixed144x6", fixedCast(), new SolidityFixed(true, 144, 6)),
            Arguments.of("fixed144x77", fixedCast(), new SolidityFixed(true, 144, 77)),
            Arguments.of("fixed152x30", fixedCast(), new SolidityFixed(true, 152, 30)),
            Arguments.of("fixed160x41", fixedCast(), new SolidityFixed(true, 160, 41)),
            Arguments.of("fixed168x11", fixedCast(), new SolidityFixed(true, 168, 11)),
            Arguments.of("fixed168x27", fixedCast(), new SolidityFixed(true, 168, 27)),
            Arguments.of("fixed184x26", fixedCast(), new SolidityFixed(true, 184, 26)),
            Arguments.of("fixed184x79", fixedCast(), new SolidityFixed(true, 184, 79)),
            Arguments.of("fixed208x3", fixedCast(), new SolidityFixed(true, 208, 3)),
            Arguments.of("fixed208x29", fixedCast(), new SolidityFixed(true, 208, 29)),
            Arguments.of("fixed208x61", fixedCast(), new SolidityFixed(true, 208, 61)),
            Arguments.of("fixed216x67", fixedCast(), new SolidityFixed(true, 216, 67)),
            Arguments.of("fixed224x78", fixedCast(), new SolidityFixed(true, 224, 78)),
            Arguments.of("fixed240x52", fixedCast(), new SolidityFixed(true, 240, 52)),
            Arguments.of("fixed240x71", fixedCast(), new SolidityFixed(true, 240, 71)),
            Arguments.of("fixed248x80", fixedCast(), new SolidityFixed(true, 248, 80)),
            Arguments.of("fixed256x22", fixedCast(), new SolidityFixed(true, 256, 22)),
            Arguments.of("fixed256x43", fixedCast(), new SolidityFixed(true, 256, 43)),
            Arguments.of("ufixed8x19", fixedCast(), new SolidityFixed(false, 8, 19)),
            Arguments.of("ufixed8x73", fixedCast(), new SolidityFixed(false, 8, 73)),
            Arguments.of("ufixed16x13", fixedCast(), new SolidityFixed(false, 16, 13)),
            Arguments.of("ufixed24x61", fixedCast(), new SolidityFixed(false, 24, 61)),
            Arguments.of("ufixed32x12", fixedCast(), new SolidityFixed(false, 32, 12)),
            Arguments.of("ufixed40x24", fixedCast(), new SolidityFixed(false, 40, 24)),
            Arguments.of("ufixed40x25", fixedCast(), new SolidityFixed(false, 40, 25)),
            Arguments.of("ufixed48x4", fixedCast(), new SolidityFixed(false, 48, 4)),
            Arguments.of("ufixed48x59", fixedCast(), new SolidityFixed(false, 48, 59)),
            Arguments.of("ufixed48x60", fixedCast(), new SolidityFixed(false, 48, 60)),
            Arguments.of("ufixed56x16", fixedCast(), new SolidityFixed(false, 56, 16)),
            Arguments.of("ufixed64x38", fixedCast(), new SolidityFixed(false, 64, 38)),
            Arguments.of("ufixed72x25", fixedCast(), new SolidityFixed(false, 72, 25)),
            Arguments.of("ufixed80x58", fixedCast(), new SolidityFixed(false, 80, 58)),
            Arguments.of("ufixed88x58", fixedCast(), new SolidityFixed(false, 88, 58)),
            Arguments.of("ufixed96x60", fixedCast(), new SolidityFixed(false, 96, 60)),
            Arguments.of("ufixed96x71", fixedCast(), new SolidityFixed(false, 96, 71)),
            Arguments.of("ufixed104x56", fixedCast(), new SolidityFixed(false, 104, 56)),
            Arguments.of("ufixed112x58", fixedCast(), new SolidityFixed(false, 112, 58)),
            Arguments.of("ufixed136x32", fixedCast(), new SolidityFixed(false, 136, 32)),
            Arguments.of("ufixed136x68", fixedCast(), new SolidityFixed(false, 136, 68)),
            Arguments.of("ufixed144x77", fixedCast(), new SolidityFixed(false, 144, 77)),
            Arguments.of("ufixed152x46", fixedCast(), new SolidityFixed(false, 152, 46)),
            Arguments.of("ufixed152x57", fixedCast(), new SolidityFixed(false, 152, 57)),
            Arguments.of("ufixed152x62", fixedCast(), new SolidityFixed(false, 152, 62)),
            Arguments.of("ufixed160x24", fixedCast(), new SolidityFixed(false, 160, 24)),
            Arguments.of("ufixed160x42", fixedCast(), new SolidityFixed(false, 160, 42)),
            Arguments.of("ufixed168x39", fixedCast(), new SolidityFixed(false, 168, 39)),
            Arguments.of("ufixed168x61", fixedCast(), new SolidityFixed(false, 168, 61)),
            Arguments.of("ufixed176x40", fixedCast(), new SolidityFixed(false, 176, 40)),
            Arguments.of("ufixed176x74", fixedCast(), new SolidityFixed(false, 176, 74)),
            Arguments.of("ufixed184x4", fixedCast(), new SolidityFixed(false, 184, 4)),
            Arguments.of("ufixed184x48", fixedCast(), new SolidityFixed(false, 184, 48)),
            Arguments.of("ufixed184x70", fixedCast(), new SolidityFixed(false, 184, 70)),
            Arguments.of("ufixed192x7", fixedCast(), new SolidityFixed(false, 192, 7)),
            Arguments.of("ufixed192x11", fixedCast(), new SolidityFixed(false, 192, 11)),
            Arguments.of("ufixed192x13", fixedCast(), new SolidityFixed(false, 192, 13)),
            Arguments.of("ufixed192x19", fixedCast(), new SolidityFixed(false, 192, 19)),
            Arguments.of("ufixed192x78", fixedCast(), new SolidityFixed(false, 192, 78)),
            Arguments.of("ufixed192x80", fixedCast(), new SolidityFixed(false, 192, 80)),
            Arguments.of("ufixed200x43", fixedCast(), new SolidityFixed(false, 200, 43)),
            Arguments.of("ufixed208x8", fixedCast(), new SolidityFixed(false, 208, 8)),
            Arguments.of("ufixed208x33", fixedCast(), new SolidityFixed(false, 208, 33)),
            Arguments.of("ufixed216x47", fixedCast(), new SolidityFixed(false, 216, 47)),
            Arguments.of("ufixed224x2", fixedCast(), new SolidityFixed(false, 224, 2)),
            Arguments.of("ufixed224x27", fixedCast(), new SolidityFixed(false, 224, 27)),
            Arguments.of("ufixed232x56", fixedCast(), new SolidityFixed(false, 232, 56)),
            Arguments.of("ufixed240x78", fixedCast(), new SolidityFixed(false, 240, 78)),
            Arguments.of("ufixed248x39", fixedCast(), new SolidityFixed(false, 248, 39)),
            Arguments.of("ufixed256x37", fixedCast(), new SolidityFixed(false, 256, 37)),
            Arguments.of("fixed", fixedCast(), new SolidityFixed(true)),
            Arguments.of("ufixed", fixedCast(), new SolidityFixed(false)),
            Arguments.of("bytes1", bytesCast(), new SolidityBytes(1)),
            Arguments.of("bytes2", bytesCast(), new SolidityBytes(2)),
            Arguments.of("bytes3", bytesCast(), new SolidityBytes(3)),
            Arguments.of("bytes4", bytesCast(), new SolidityBytes(4)),
            Arguments.of("bytes5", bytesCast(), new SolidityBytes(5)),
            Arguments.of("bytes6", bytesCast(), new SolidityBytes(6)),
            Arguments.of("bytes7", bytesCast(), new SolidityBytes(7)),
            Arguments.of("bytes8", bytesCast(), new SolidityBytes(8)),
            Arguments.of("bytes9", bytesCast(), new SolidityBytes(9)),
            Arguments.of("bytes10", bytesCast(), new SolidityBytes(10)),
            Arguments.of("bytes11", bytesCast(), new SolidityBytes(11)),
            Arguments.of("bytes12", bytesCast(), new SolidityBytes(12)),
            Arguments.of("bytes13", bytesCast(), new SolidityBytes(13)),
            Arguments.of("bytes14", bytesCast(), new SolidityBytes(14)),
            Arguments.of("bytes15", bytesCast(), new SolidityBytes(15)),
            Arguments.of("bytes16", bytesCast(), new SolidityBytes(16)),
            Arguments.of("bytes17", bytesCast(), new SolidityBytes(17)),
            Arguments.of("bytes18", bytesCast(), new SolidityBytes(18)),
            Arguments.of("bytes19", bytesCast(), new SolidityBytes(19)),
            Arguments.of("bytes20", bytesCast(), new SolidityBytes(20)),
            Arguments.of("bytes21", bytesCast(), new SolidityBytes(21)),
            Arguments.of("bytes22", bytesCast(), new SolidityBytes(22)),
            Arguments.of("bytes23", bytesCast(), new SolidityBytes(23)),
            Arguments.of("bytes24", bytesCast(), new SolidityBytes(24)),
            Arguments.of("bytes25", bytesCast(), new SolidityBytes(25)),
            Arguments.of("bytes26", bytesCast(), new SolidityBytes(26)),
            Arguments.of("bytes27", bytesCast(), new SolidityBytes(27)),
            Arguments.of("bytes28", bytesCast(), new SolidityBytes(28)),
            Arguments.of("bytes29", bytesCast(), new SolidityBytes(29)),
            Arguments.of("bytes30", bytesCast(), new SolidityBytes(30)),
            Arguments.of("bytes31", bytesCast(), new SolidityBytes(31)),
            Arguments.of("bytes32", bytesCast(), new SolidityBytes(32)),
            Arguments.of("byte", bytesCast(), new SolidityBytes(1)),
            Arguments.of("bytes", bytesCast(), SolidityBytes.DEFAULT_INSTANCE)
        );
    }

    private static Stream<Arguments> validArrayTypeDefinitionCases() {
        return Stream.of(    
            Arguments.of("address[]", addressCast(), new SolidityArray(SolidityAddress.DEFAULT_INSTANCE)),
            Arguments.of("bool[]", boolCast(), new SolidityArray(SolidityBool.DEFAULT_INSTANCE)),
            Arguments.of("string[]", stringCast(), new SolidityArray(SolidityString.DEFAULT_INSTANCE)),
            Arguments.of("bytes14[]", bytesCast(), new SolidityArray(new SolidityBytes(14))),
            Arguments.of("bytes15[]", bytesCast(), new SolidityArray(new SolidityBytes(15))),
            Arguments.of("bytes16[]", bytesCast(), new SolidityArray(new SolidityBytes(16))),
            Arguments.of("bytes20[]", bytesCast(), new SolidityArray(new SolidityBytes(20))),
            Arguments.of("bytes29[]", bytesCast(), new SolidityArray(new SolidityBytes(29))),
            Arguments.of("int8[]", integerCast(), new SolidityArray(new SolidityInteger(true, 8))),
            Arguments.of("int24[]", integerCast(), new SolidityArray(new SolidityInteger(true, 24))),
            Arguments.of("uint128[]", integerCast(), new SolidityArray(new SolidityInteger(false, 128))),
            Arguments.of("uint256[]", integerCast(), new SolidityArray(new SolidityInteger(false, 256))),
            Arguments.of("uint152[]", integerCast(), new SolidityArray(new SolidityInteger(false, 152))),
            Arguments.of("uint248[]", integerCast(), new SolidityArray(new SolidityInteger(false, 248))),
            Arguments.of("uint72[]", integerCast(), new SolidityArray(new SolidityInteger(false, 72))),
            Arguments.of("uint168[]", integerCast(), new SolidityArray(new SolidityInteger(false, 168))),
            Arguments.of("fixed240x47[]", fixedCast(), new SolidityArray(new SolidityFixed(true, 240, 47))),
            Arguments.of("fixed80x73[]", fixedCast(), new SolidityArray(new SolidityFixed(true, 80, 73))),
            Arguments.of("ufixed32x52[]", fixedCast(), new SolidityArray(new SolidityFixed(false, 32, 52))),
            Arguments.of("ufixed32x34[]", fixedCast(), new SolidityArray(new SolidityFixed(false, 32, 34))),
            Arguments.of("fixed96x32[]", fixedCast(), new SolidityArray(new SolidityFixed(true, 96, 32))),
            Arguments.of("fixed152x29[]", fixedCast(), new SolidityArray(new SolidityFixed(true, 152, 29))),
            Arguments.of("ufixed192x54[]", fixedCast(), new SolidityArray(new SolidityFixed(false, 192, 54))),
            Arguments.of("ufixed152x45[]", fixedCast(), new SolidityArray(new SolidityFixed(false, 152, 45))),
            Arguments.of("ufixed24x68[]", fixedCast(), new SolidityArray(new SolidityFixed(false, 24, 68))),
            Arguments.of("fixed32x79[]", fixedCast(), new SolidityArray(new SolidityFixed(true, 32, 79))),
            Arguments.of("ufixed144x10[]", fixedCast(), new SolidityArray(new SolidityFixed(false, 144, 10))),
            Arguments.of("fixed200x36[]", fixedCast(), new SolidityArray(new SolidityFixed(true, 200, 36))),
            Arguments.of("ufixed256x74[]", fixedCast(), new SolidityArray(new SolidityFixed(false, 256, 74))),
            Arguments.of("ufixed104x62[]", fixedCast(), new SolidityArray(new SolidityFixed(false, 104, 62))),
            Arguments.of("ufixed48x20[]", fixedCast(), new SolidityArray(new SolidityFixed(false, 48, 20))),
            Arguments.of("ufixed248x7[]", fixedCast(), new SolidityArray(new SolidityFixed(false, 248, 7))),
            Arguments.of("ufixed48x14[]", fixedCast(), new SolidityArray(new SolidityFixed(false, 48, 14))),
            Arguments.of("fixed144x25[]", fixedCast(), new SolidityArray(new SolidityFixed(true, 144, 25))),
            Arguments.of("fixed168x14[]", fixedCast(), new SolidityArray(new SolidityFixed(true, 168, 14))),
            Arguments.of("ufixed144x64[]", fixedCast(), new SolidityArray(new SolidityFixed(false, 144, 64))),
            Arguments.of("ufixed88x7[]", fixedCast(), new SolidityArray(new SolidityFixed(false, 88, 7))),
            Arguments.of("ufixed144x6[]", fixedCast(), new SolidityArray(new SolidityFixed(false, 144, 6))),
            Arguments.of("fixed208x76[]", fixedCast(), new SolidityArray(new SolidityFixed(true, 208, 76))),
            Arguments.of("fixed56x75[]", fixedCast(), new SolidityArray(new SolidityFixed(true, 56, 75))),
            Arguments.of("fixed184x57[]", fixedCast(), new SolidityArray(new SolidityFixed(true, 184, 57))),
            Arguments.of("ufixed232x66[]", fixedCast(), new SolidityArray(new SolidityFixed(false, 232, 66))),
            Arguments.of("ufixed184x30[]", fixedCast(), new SolidityArray(new SolidityFixed(false, 184, 30))),
            Arguments.of("ufixed88x66[]", fixedCast(), new SolidityArray(new SolidityFixed(false, 88, 66))),
            Arguments.of("ufixed56x11[]", fixedCast(), new SolidityArray(new SolidityFixed(false, 56, 11))),
            Arguments.of("fixed48x22[]", fixedCast(), new SolidityArray(new SolidityFixed(true, 48, 22)))
            
        );
    }

    @ParameterizedTest
    @MethodSource("invalidCases")
    public void testInvalidCases(String definition) {
        for (BiFunction<String, Boolean, SolidityType> cast : BASE_TYPE_CASTS) {
            assertNull(cast.apply(definition, false));
            assertNull(AnalyzerUtils.parseArrayDefinition(definition, cast, false));
        }
    }

    private static Stream<Arguments> equalsAndHashCases() {
        return Stream.of(
            Arguments.of(new SolidityFixed(false, 216, 25), new SolidityFixed(false, 216, 25), true),
            Arguments.of(new SolidityFixed(true, 240, 24), new SolidityFixed(true, 240, 24), true),
            Arguments.of(new SolidityFixed(true, 160, 34), new SolidityFixed(true, 160, 34), true),
            Arguments.of(new SolidityFixed(false, 80, 1), new SolidityFixed(false, 80, 1), true),
            Arguments.of(new SolidityFixed(false, 232, 66), new SolidityFixed(false, 232, 66), true),
            Arguments.of(new SolidityFixed(false, 216, 38), new SolidityFixed(false, 216, 38), true),
            Arguments.of(new SolidityFixed(false, 128, 5), new SolidityFixed(false, 128, 5), true),
            Arguments.of(new SolidityFixed(true, 168, 15), new SolidityFixed(true, 168, 15), true),
            Arguments.of(new SolidityFixed(false, 240, 8), new SolidityFixed(false, 240, 8), true),
            Arguments.of(new SolidityFixed(true, 48, 14), new SolidityFixed(true, 48, 14), true),
            Arguments.of(new SolidityFixed(false, 144, 43), new SolidityFixed(false, 144, 43), true),
            Arguments.of(new SolidityFixed(false, 40, 68), new SolidityFixed(false, 40, 68), true),
            Arguments.of(new SolidityFixed(true, 224, 30), new SolidityFixed(true, 224, 30), true),
            Arguments.of(new SolidityFixed(true, 120, 68), new SolidityFixed(true, 120, 68), true),
            Arguments.of(new SolidityFixed(true, 24, 45), new SolidityFixed(true, 24, 45), true),
            Arguments.of(new SolidityFixed(true, 152, 53), new SolidityFixed(true, 152, 53), true),
            Arguments.of(new SolidityFixed(true, 120, 47), new SolidityFixed(true, 120, 47), true),
            Arguments.of(new SolidityFixed(false, 24, 74), new SolidityFixed(false, 24, 74), true),
            Arguments.of(new SolidityFixed(true, 56, 18), new SolidityFixed(true, 56, 18), true),
            Arguments.of(new SolidityFixed(false, 208, 42), new SolidityFixed(false, 208, 42), true),
            Arguments.of(new SolidityArray(new SolidityFixed(true, 16, 78)), new SolidityArray(new SolidityFixed(true, 16, 78)), true),
            Arguments.of(new SolidityArray(new SolidityFixed(false, 248, 52)), new SolidityArray(new SolidityFixed(false, 248, 52)), true),
            Arguments.of(new SolidityArray(new SolidityFixed(false, 224, 28)), new SolidityArray(new SolidityFixed(false, 224, 28)), true),
            Arguments.of(new SolidityArray(new SolidityFixed(false, 240, 80)), new SolidityArray(new SolidityFixed(false, 240, 80)), true),
            Arguments.of(new SolidityArray(new SolidityFixed(false, 240, 23)), new SolidityArray(new SolidityFixed(false, 240, 23)), true),
            Arguments.of(new SolidityArray(new SolidityFixed(true, 48, 46)), new SolidityArray(new SolidityFixed(true, 48, 46)), true),
            Arguments.of(new SolidityArray(new SolidityFixed(true, 128, 51)), new SolidityArray(new SolidityFixed(true, 128, 51)), true),
            Arguments.of(new SolidityArray(new SolidityFixed(true, 216, 32)), new SolidityArray(new SolidityFixed(true, 216, 32)), true),
            Arguments.of(new SolidityArray(new SolidityFixed(false, 200, 22)), new SolidityArray(new SolidityFixed(false, 200, 22)), true),
            Arguments.of(new SolidityArray(new SolidityFixed(false, 136, 20)), new SolidityArray(new SolidityFixed(false, 136, 20)), true),
            Arguments.of(new SolidityArray(new SolidityFixed(false, 112, 3)), new SolidityInteger(true, 160), false),
            Arguments.of(new SolidityFixed(false, 8, 44), new SolidityArray(new SolidityString()), false),
            Arguments.of(new SolidityFixed(true, 112, 20), new SolidityArray(new SolidityFixed(true, 88, 19)), false),
            Arguments.of(new SolidityFixed(false, 224, 48), new SolidityFixed(false, 152, 3), false),
            Arguments.of(new SolidityArray(new SolidityFixed(true, 16, 35)), new SolidityFixed(true, 192, 50), false),
            Arguments.of(new SolidityArray(new SolidityFixed(false, 152, 4)), new SolidityArray(new SolidityFixed(true, 136, 36)), false),
            Arguments.of(new SolidityArray(new SolidityFixed(true, 88, 60)), new SolidityFixed(false, 160, 33), false),
            Arguments.of(new SolidityArray(new SolidityFixed(false, 104, 12)), new SolidityFixed(true, 120, 35), false),
            Arguments.of(new SolidityArray(new SolidityFixed(false, 72, 76)), new SolidityFixed(false, 112, 32), false),
            Arguments.of(new SolidityFixed(false, 128, 57), new SolidityFixed(true, 32, 58), false),
            Arguments.of(new SolidityArray(new SolidityFixed(true, 192, 25)), new SolidityFixed(false, 200, 26), false),
            Arguments.of(new SolidityArray(new SolidityFixed(false, 56, 21)), new SolidityFixed(true, 16, 59), false),
            Arguments.of(new SolidityFixed(false, 120, 66), new SolidityFixed(true, 80, 80), false),
            Arguments.of(new SolidityFixed(false, 104, 11), new SolidityFixed(false, 128, 73), false),
            Arguments.of(new SolidityArray(new SolidityFixed(true, 232, 69)), new SolidityFixed(true, 152, 23), false),
            Arguments.of(new SolidityFixed(false, 112, 30), new SolidityFixed(true, 240, 11), false),
            Arguments.of(new SolidityFixed(true, 48, 3), new SolidityFixed(true, 248, 45), false),
            Arguments.of(new SolidityFixed(true, 40, 78), new SolidityArray(new SolidityFixed(false, 112, 27)), false),
            Arguments.of(new SolidityFixed(false, 208, 28), new SolidityFixed(true, 240, 66), false),
            Arguments.of(new SolidityArray(new SolidityFixed(false, 176, 28)), new SolidityFixed(true, 184, 2), false),
            Arguments.of(new SolidityFixed(true, 176, 19), new SolidityArray(new SolidityFixed(false, 168, 30)), false),
            Arguments.of(new SolidityArray(new SolidityFixed(false, 152, 48)), new SolidityFixed(true, 56, 52), false),
            Arguments.of(new SolidityArray(new SolidityFixed(true, 152, 57)), new SolidityArray(new SolidityFixed(true, 240, 24)), false),
            Arguments.of(new SolidityArray(new SolidityFixed(false, 80, 33)), new SolidityFixed(false, 248, 21), false),
            Arguments.of(new SolidityArray(new SolidityFixed(true, 80, 3)), new SolidityArray(new SolidityFixed(true, 112, 51)), false),
            Arguments.of(new SolidityArray(new SolidityFixed(true, 232, 25)), new SolidityArray(new SolidityFixed(true, 136, 41)), false),
            Arguments.of(new SolidityArray(new SolidityFixed(true, 136, 42)), new SolidityFixed(true, 208, 29), false),
            Arguments.of(new SolidityFixed(false, 96, 8), new SolidityArray(new SolidityFixed(true, 112, 4)), false),
            Arguments.of(new SolidityFixed(true, 232, 5), new SolidityArray(new SolidityFixed(false, 240, 50)), false),
            Arguments.of(new SolidityArray(new SolidityFixed(false, 64, 44)), new SolidityFixed(true, 8, 48), false)
        );
    }

    private static Stream<String> invalidCases() {
        return Stream.of(
            "bol",
            "strng",
            "adres",
            "fixed155x77",
            "fixed200x89",
            "ufixed277x99",
            "fixd",
            "ufx",
            "string[",
            "unt",
            "it256",
            "int78",
            "uint300",
            "uint128]",
            "uint[",
            "",
            "    ",
            "byte12",
            "bytes356",
            "bytes0",
            "bytes33[]",
            "uint157[]",
            "int55[]",
            "fixed9x99[]",
            "ufixed80x96[]",
            "ufixed53x80[]",
            "ufixed34x",
            "fixedx123"
        );
    }

    private static List<BiFunction<String, Boolean, SolidityType>> BASE_TYPE_CASTS = Arrays.asList(
        addressCast(),
        boolCast(),
        bytesCast(),
        fixedCast(),
        integerCast(),
        stringCast()
    );

    private static BiFunction<String, Boolean, SolidityType> addressCast() {
        return AnalyzerUtils::parseAddressDefinition;
    }

    private static BiFunction<String, Boolean, SolidityType> boolCast() {
        return AnalyzerUtils::parseBoolDefinition;
    }

    private static BiFunction<String, Boolean, SolidityType> bytesCast() {
        return AnalyzerUtils::parseBytesDefinition;
    }

    private static BiFunction<String, Boolean, SolidityType> fixedCast() {
        return AnalyzerUtils::parseFixedDefinition;
    }

    private static BiFunction<String, Boolean, SolidityType> integerCast() {
        return AnalyzerUtils::parseIntegerDefinition;
    }

    private static BiFunction<String, Boolean, SolidityType> stringCast() {
        return AnalyzerUtils::parseStringDefinition;
    }

    public static void main(String[] args) {
        
    }
}