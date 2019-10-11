package au.csiro.data61.aap.parser;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import au.csiro.data61.aap.specification.Block;
import au.csiro.data61.aap.specification.BlockRangeBlock;
import au.csiro.data61.aap.specification.Constant;
import au.csiro.data61.aap.util.StringUtil;

/**
 * ParseBlockHeadTest
 */
 public class ParseBlockHeadTest {
    private final SpecificationParser parser = new SpecificationParser();


    @ParameterizedTest
    @MethodSource("createValidBlockRangeTypes")
    void testValidBlockRangeTypes(String code, Class<?> fromClass, Class<?> toClass) {
        final InputStream is = StringUtil.toStream(code);
        SpecificationParserResult<Block> parserResult = this.parser.parseBlock(is);
        assertTrue(parserResult.isSuccessful(), parserResult.errorStream().map(e -> e.getErrorMessage()).collect(Collectors.joining(", ")));        
        assertTrue(parserResult.getResult() instanceof BlockRangeBlock);
        final BlockRangeBlock block = (BlockRangeBlock)parserResult.getResult();
        assertTrue(block.getFromBlock().getClass().equals(fromClass));
        assertTrue(block.getToBlock().getClass().equals(toClass));
    }

    private static Stream<Arguments> createValidBlockRangeTypes() {        
        Stream<Arguments> stream = Stream.of(
            Arguments.of("BLOCK RANGE (0,100000) {}", Constant.class, Constant.class),
            Arguments.of("BLOCK RANGE (0,99999) {}", Constant.class, Constant.class),
            Arguments.of("BLOCK RANGE (9999,100000) {}", Constant.class, Constant.class),
            Arguments.of("BLOCK RANGE (7825,15984) {}", Constant.class, Constant.class),
            Arguments.of("BLOCK RANGE (123456,95144789) {}", Constant.class, Constant.class),
            Arguments.of("BLOCK RANGE (EARLIEST,100000) {}", Constant.class, Constant.class),
            Arguments.of("BLOCK RANGE (0,PENDING) {}", Constant.class, Constant.class),
            Arguments.of("BLOCK RANGE (EARLIEST,PENDING) {}", Constant.class, Constant.class)
        );
        return stream;
    } 
}