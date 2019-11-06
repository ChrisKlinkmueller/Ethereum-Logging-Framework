package au.csiro.data61.aap.parser;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.antlr.v4.runtime.tree.TerminalNode;

import au.csiro.data61.aap.parser.XbelParser.SolTypeContext;
import au.csiro.data61.aap.spec.types.SolidityType;

/**
 * AnalyzerUtils
 */
class AnalyzerUtils {
    private static final Logger LOGGER = Logger.getLogger(AnalyzerUtils.class.getName());
    private static final int ADDRESS_LENGTH = 42;

    static String verifyAddressLiteral(TerminalNode node, ErrorCollector collector) {
        assert collector != null && node != null;

        if (node.getText().length() != ADDRESS_LENGTH) {
            collector.addSemanticError(node.getSymbol(), "An address literal must start with '0x' followed by 40 hexa-decimal characters.");
            return null;
        }

        return node.getText();
    }

    static List<String> verifyAddressLiterals(List<TerminalNode> nodes, ErrorCollector collector) {
        assert collector != null && nodes != null && nodes.stream().allMatch(Objects::nonNull);
        
        final int priorErrors = collector.errorCount();
        final List<String> addresses = nodes.stream()
            .map(node -> verifyAddressLiteral(node, collector))
            .collect(Collectors.toList());

        return priorErrors == collector.errorCount() ? addresses : null;
    }

    static BigInteger verifyIntegerLiteral(TerminalNode node, ErrorCollector collector) {
        assert collector != null && node != null;
        
        try {
            return new BigInteger(node.getText());
        }
        catch (NumberFormatException ex) {
            final String message = String.format("An integer literal should always be parseable, but this '%s' was not.", node.getText());
            LOGGER.log(Level.SEVERE, message, ex);
            collector.addSemanticError(node.getSymbol(), message);
            return null;
        }
    }

    static SolidityType verifySolidityType(SolTypeContext ctx, ErrorCollector collector) {
        assert ctx != null && collector != null;

        if (ctx.SOL_INT_TYPE() != null) {
            return parseIntType(ctx.SOL_INT_ARRAY_TYPE(), collector);
        }

        return null;
    }

    private static SolidityType parseIntType(TerminalNode node, ErrorCollector collector) {
        return null;
    }
    
    public static void main(String[] args) {
        final String[] types = {"uint128", "int8", "uint", "int"};
        Pattern pattern = Pattern.compile("\\d*");

        for (String type : types) {
            final boolean unsigned = type.startsWith("u");
            final String bitLength = type.chars().filter(Character::isDigit).mapToObj(c -> Character.toString(c)).collect(Collectors.joining());
            System.out.println(unsigned + " + " + bitLength);
        }
    }

}