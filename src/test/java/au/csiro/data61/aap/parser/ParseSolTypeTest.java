package au.csiro.data61.aap.parser;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Function;
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
    @MethodSource("validTypeDefinitionCases")
    public void testTypeDefinitionCases(String definition, Function<String, SolidityType> cast, SolidityType expectedType) {
        SolidityType type = cast.apply(definition);
        assertTrue(type != null, String.format("Test case: '%s'", definition));
        assertTrue(type.equals(expectedType), String.format("Test case: '%s'", definition));
    }

    private static Stream<Arguments> validTypeDefinitionCases() {
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
            Arguments.of("byte", bytesCast(), SolidityBytes.DEFAULT_INSTANCE),
            Arguments.of("bytes", bytesCast(), new SolidityArray(SolidityBytes.DEFAULT_INSTANCE))
        );
    }

    private static Function<String, SolidityType> addressCast() {
        return AnalyzerUtils::parseAddressDefinition;
    }

    private static Function<String, SolidityType> boolCast() {
        return AnalyzerUtils::parseBoolDefinition;
    }

    private static Function<String, SolidityType> bytesCast() {
        return AnalyzerUtils::parseBytesDefinition;
    }

    private static Function<String, SolidityType> fixedCast() {
        return AnalyzerUtils::parseFixedDefinition;
    }

    private static Function<String, SolidityType> integerCast() {
        return AnalyzerUtils::parseIntegerDefinition;
    }

    private static Function<String, SolidityType> stringCast() {
        return AnalyzerUtils::parseStringDefinition;
    }

    public static void main(String[] args) {
        for (boolean signed : new boolean[]{true, false}) {
            for (int m = 8; m <= 256; m +=8) {
                System.out.println(String.format("Arguments.of(\"%sint%s\", integerCast(), new SolidityInteger(%s, %s)),", signed ? "" : "u", m, Boolean.toString(signed), m));
            }
        }
    }
}