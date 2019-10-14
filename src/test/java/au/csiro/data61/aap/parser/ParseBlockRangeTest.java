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
import au.csiro.data61.aap.specification.SmartContractsRangeBlock;
import au.csiro.data61.aap.specification.TransactionRangeBlock;
import au.csiro.data61.aap.util.StringUtil;

/**
 * ParseBlockHeadTest
 */
 public class ParseBlockRangeTest {
    private final SpecificationParser parser = new SpecificationParser();

    @ParameterizedTest
    @MethodSource("createValidBlockRangeTypes")
    void testValidBlockRangeTypes(String code, Class<?> fromClass, Class<?> toClass) {
        final InputStream is = StringUtil.toStream(code);
        final SpecificationParserResult<Block> parserResult = this.parser.parseBlock(is);
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

    @ParameterizedTest
    @MethodSource("createValidTransactionRangeValues") 
    void testValidTransactionRangeValues(String code, Class<?> senderClass, Class<?> recipientClass) {
        final InputStream is = StringUtil.toStream(code);
        final SpecificationParserResult<Block> parserResult = this.parser.parseBlock(is);
        assertTrue(parserResult.isSuccessful());
        assertTrue(parserResult.getResult() instanceof TransactionRangeBlock);
        final TransactionRangeBlock block = (TransactionRangeBlock)parserResult.getResult();
        assertTrue(block.getTransactionSenders().getClass().equals(senderClass));
        assertTrue(block.getTransactionRecipients().getClass().equals(recipientClass));
    }

    private static Stream<Arguments> createValidTransactionRangeValues() {        
        return Stream.of(
            Arguments.of("TRANSACTIONS (0xca197948d4ea0f83d752ae71a321e54dbe735bc5,0x5ed78d90326826f54986122500afc139d6333ce3)(0xca197948d4ea0f83d752ae71a321e54dbe735bc5,0x5ed78d90326826f54986122500afc139d6333ce3) {}", Constant.class, Constant.class)
        );
    } 

    @ParameterizedTest
    @MethodSource("createInvalidTransactionRangeValues") 
    void testInvalidTransactionRangeValues(String code) {
        final InputStream is = StringUtil.toStream(code);
        final SpecificationParserResult<Block> parserResult = this.parser.parseBlock(is);
        assertTrue(!parserResult.isSuccessful());
    }

    private static Stream<Arguments> createInvalidTransactionRangeValues() {        
        return Stream.of(
            Arguments.of("TRANSACTIONS ()() {}")
        );
    } 

    @ParameterizedTest
    @MethodSource("createValidSmartContractRangeValues") 
    void testValidSmartContractsRangeValues(String code, Class<?> addressClass) {
        final InputStream is = StringUtil.toStream(code);
        final SpecificationParserResult<Block> parserResult = this.parser.parseBlock(is);
        assertTrue(parserResult.isSuccessful(), parserResult.errorStream().map(e -> e.getErrorMessage()).collect(Collectors.joining(", ")));
        assertTrue(parserResult.getResult() instanceof SmartContractsRangeBlock);
        final SmartContractsRangeBlock block = (SmartContractsRangeBlock)parserResult.getResult();
        assertTrue(block.getAddresses().getClass().equals(addressClass));
    }

    private static Stream<Arguments> createValidSmartContractRangeValues() {        
        return Stream.of(
            Arguments.of("SMART CONTRACTS(0xca197948d4ea0f83d752ae71a321e54dbe735bc5,0x5ed78d90326826f54986122500afc139d6333ce3) {}", Constant.class)
        );
    } 
}