package au.csiro.data61.aap.parser;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import au.csiro.data61.aap.program.GlobalScope;
import au.csiro.data61.aap.util.StringUtil;

/**
 * ParseScopesTest
 */
public class ParseScopesTest {
    private final SpecificationParser parser = new SpecificationParser();

    @ParameterizedTest
    @MethodSource("validScopeCases")
    void testValidScopeCases(String script) {
        final InputStream is = StringUtil.toStream(script);
        final SpecificationParserResult<GlobalScope> parserResult = this.parser.parseDocument(is);

        if (!parserResult.isSuccessful()) {
            parserResult.errorStream().forEach(error -> System.out.println(error.getErrorMessage()));
        }

        assertTrue(parserResult.isSuccessful(), script);
    }

    private static Stream<Arguments> validScopeCases() {
        return Stream.of(
            Arguments.of("BLOCKS (EARLIEST, CURRENT) {}"),
            Arguments.of("BLOCKS (EARLIEST, PENDING) {}"),
            Arguments.of("BLOCKS (CURRENT, PENDING) {}"),
            Arguments.of("BLOCKS (EARLIEST, 10000000) {}"),
            Arguments.of("BLOCKS (9856654656, CURRENT) {}"),
            Arguments.of("BLOCKS (6546516161, PENDING) {}"),
            Arguments.of("BLOCKS (0, 15646444646) {}"),
            Arguments.of("BLOCKS (EARLIEST, CURRENT) { TRANSACTIONS(ANY)(ANY) {} }"),
            Arguments.of("BLOCKS (EARLIEST, CURRENT) { TRANSACTIONS(0xF4A2CB946f72e1460C490bF490E73d81295130cd)(ANY) {} }"),
            Arguments.of("BLOCKS (EARLIEST, CURRENT) { TRANSACTIONS(0xF4A2CB946f72e1460C490bF490E73d81295130cd,0xe803267c5086252425BEFE8E1f9C4CEC0ea3A952)(ANY) {} }"),
            Arguments.of("BLOCKS (EARLIEST, CURRENT) { TRANSACTIONS(ANY)(0x461d8d95141b94ea64876caa2c6ddb25828a0561) {} }"),
            Arguments.of("BLOCKS (EARLIEST, CURRENT) { TRANSACTIONS(ANY)(0x461d8d95141b94ea64876caa2c6ddb25828a0561,0x9554efa1669014c25070bc23c2df262825704228) {} }"),
            Arguments.of("BLOCKS (EARLIEST, CURRENT) { TRANSACTIONS(ANY)(ANY) {} TRANSACTIONS(ANY)(ANY) {} }"),
            Arguments.of("BLOCKS (EARLIEST, CURRENT) { SMART CONTRACT(ANY)(uint8 test) {} }"),
            Arguments.of("BLOCKS (EARLIEST, CURRENT) { SMART CONTRACT(ANY)(uint8 test, _) {} }"),
            Arguments.of("BLOCKS (EARLIEST, CURRENT) { SMART CONTRACT(ANY)(uint8 test, _, ufixed128x80 test2) {} }"),
            Arguments.of("BLOCKS (EARLIEST, CURRENT) { SMART CONTRACT(ANY)(uint8 test, ...) {} }"),
            Arguments.of("BLOCKS (EARLIEST, CURRENT) { SMART CONTRACT(ANY)(uint8 test, string tes212, ...) {} }"),
            Arguments.of("BLOCKS (EARLIEST, CURRENT) { SMART CONTRACT(ANY)(uint8 test, _, string tes212, ...) {} }"),
            Arguments.of("BLOCKS (EARLIEST, CURRENT) { LOG ENTRY(ANY)(fixed number, string name) {} }"),
            Arguments.of("BLOCKS (EARLIEST, CURRENT) { LOG ENTRY(ANY)(fixed number, _, string name) {} }"),
            Arguments.of("BLOCKS (EARLIEST, CURRENT) { LOG ENTRY(ANY)(fixed number, string name, ...) {} }"),
            Arguments.of("BLOCKS (EARLIEST, CURRENT) { LOG ENTRY(ANY)(fixed number, _, string name, ...) {} }"),
            Arguments.of("BLOCKS (EARLIEST, CURRENT) { LOG ENTRY(ANY)(AugurUpdate(fixed number, string name)) {} }"),
            Arguments.of("BLOCKS (EARLIEST, CURRENT) { LOG ENTRY(ANY)(AugurUpdate()) {} }"),
            Arguments.of("BLOCKS (EARLIEST, CURRENT) { TRANSACTIONS(ANY)(ANY) { LOG ENTRY(ANY)(AugurUpdate()) {} } SMART CONTRACT(ANY)(uint8 test) {} LOG ENTRY(ANY)(AugurUpdate()) {} SMART CONTRACT(ANY)(uint8 test) {} }")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidScopeCases")
    public void testInvalidScopeCases(String script) {
        final InputStream is = StringUtil.toStream(script);
        final SpecificationParserResult<GlobalScope> parserResult = this.parser.parseDocument(is);

        assertTrue(!parserResult.isSuccessful(), script);
    }

    private static Stream<Arguments> invalidScopeCases() {
        return Stream.of(
            Arguments.of("BLOCKS (PENDING, CURRENT) {}"),
            Arguments.of("BLOCKS (PENDING) {}"),
            Arguments.of("BLOCKS (CURRENT, EARLIEST) {}"),
            Arguments.of("BLOCKS (EARLIEST, 10000000, 328980) {}"),
            Arguments.of("BLOCKS (9856654656, 2380ad) {}"),
            Arguments.of("BLOCKS (EARLIEST, CURRENT) { TRANSACTIONS(ANY,ANY)(ANY) {} }"),
            Arguments.of("BLOCKS (EARLIEST, CURRENT) { TRANSACTIONS(ANY,0xF4A2CB946f72e1460C490bF490E73d81295130cd)(ANY) {} }"),
            Arguments.of("BLOCKS (EARLIEST, CURRENT) { TRANSACTIONS(0xF4A2CB946f72e1460C490bF490E73d81295130cd,ANY,0xe803267c5086252425BEFE8E1f9C4CEC0ea3A952)(ANY) {} }"),
            Arguments.of("BLOCKS (EARLIEST, CURRENT) { TRANSACTIONS(ANY)(0x461d8d95141b94ea64876caa2c6ddb25828a0561,ANY) {} }"),
            Arguments.of("BLOCKS (EARLIEST, CURRENT) { TRANSACTIONS(ANY)(0x461d8amtyu1b94ea64876caa2c6ddb25828a0561,0x9554efa1669014c25070bc23c2df262825704228) {} }"),
            Arguments.of("BLOCKS (EARLIEST, CURRENT) { TRANSACTIONS(ANY)(0xefa1669014c25070bc23c2df262825704228) {} }"),
            Arguments.of("BLOCKS (EARLIEST, CURRENT) { SMART CONTRACTS(ANY)(_) {} }"),
            Arguments.of("BLOCKS (EARLIEST, CURRENT) { SMART CONTRACT(ANY)(uint8 test, _, string test, ...) {} }"),
            Arguments.of("BLOCKS (EARLIEST, CURRENT) { SMART CONTRACT(ANY)(uint8 test, ..., string tes212) {} }"),
            Arguments.of("BLOCKS (EARLIEST, CURRENT) { SMART CONTRACT(ANY)(...) {} }"),
            Arguments.of("BLOCKS (EARLIEST, CURRENT) { TRANSACTIONS(ANY,ANY)(ANY) { SMART CONTRACTS(ANY)(_) {} } }"),
            Arguments.of("BLOCKS (EARLIEST, CURRENT) { LOG ENTRY(ANY)() {} }"),
            Arguments.of("BLOCKS (EARLIEST, CURRENT) { LOG ENTRY(ANY)(_) {} }"),
            Arguments.of("BLOCKS (EARLIEST, CURRENT) { LOG ENTRY(ANY)(...) {} }"),
            Arguments.of("BLOCKS (EARLIEST, CURRENT) { LOG ENTRY(ANY)(fixed number, ..., string name) {} }"),
            Arguments.of("BLOCKS (EARLIEST, CURRENT) { LOG ENTRY(ANY)(AugurUpdate(fixed number, _, string name)) {} }"),
            Arguments.of("BLOCKS (EARLIEST, CURRENT) { LOG ENTRY(ANY)(AugurUpdate(fixed number, ...)) {} }"),
            Arguments.of("BLOCKS (EARLIEST, CURRENT) { TRANSACTIONS(ANY)(ANY) { TRANSACTIONS(ANY)(ANY) {} } }"),
            Arguments.of("BLOCKS (EARLIEST, CURRENT) { TRANSACTIONS(ANY)(ANY) { SMART CONTRACT(ANY)(uint8 test, string tes212) {} } }"),
            Arguments.of("BLOCKS (EARLIEST, CURRENT) { LOG ENTRY(ANY)(AugurUpdate(fixed number)) { LOG ENTRY(ANY)(AugurUpdate(fixed number)) {} } }"),
            Arguments.of("BLOCKS (EARLIEST, CURRENT) { LOG ENTRY(ANY)(AugurUpdate(fixed number)) { TRANSACTIONS(ANY)(ANY) {} } }"),
            Arguments.of("BLOCKS (EARLIEST, CURRENT) { LOG ENTRY(ANY)(AugurUpdate(fixed number)) { SMART CONTRACT(ANY)(uint8 test, string tes212) {} } }"),
            Arguments.of("BLOCKS (EARLIEST, CURRENT) { SMART CONTRACT(ANY)(uint8 test, string tes212) { LOG ENTRY(ANY)(AugurUpdate(fixed number)) {} } }"),
            Arguments.of("BLOCKS (EARLIEST, CURRENT) { SMART CONTRACT(ANY)(uint8 test, string tes212) { TRANSACTIONS(ANY)(ANY) {} } }")
        );
    }
}