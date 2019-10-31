package au.csiro.data61.aap.parser;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import au.csiro.data61.aap.spec.GlobalScope;
import au.csiro.data61.aap.util.StringUtil;

/**
 * ParseScopesTest
 */
public class ParseScopesTest {
    private final SpecificationParser parser = new SpecificationParser();

    @ParameterizedTest
    @MethodSource("validScopeCases")
    void test(String script) {
        final InputStream is = StringUtil.toStream(script);
        final SpecificationParserResult<GlobalScope> parserResult = this.parser.parseDocument(is);

        if (!parserResult.isSuccessful()) {
            parserResult.errorStream().forEach(error -> System.out.println(error.getErrorMessage()));
        }

        assertTrue(parserResult.isSuccessful());
    }

    private static Stream<Arguments> validScopeCases() {
        return Stream.of(
            Arguments.of("BLOCKS (EARLIEST, CURRENT) { TRANSACTIONS(ANY)(ANY) {LOG ENTRY(ANY)(AugurTest(uint8 test)){}}}")
        );
    }
}