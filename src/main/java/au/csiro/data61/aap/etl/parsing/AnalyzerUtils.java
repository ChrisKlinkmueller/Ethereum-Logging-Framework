package au.csiro.data61.aap.etl.parsing;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

import au.csiro.data61.aap.etl.util.TypeUtils;
import au.csiro.data61.aap.etl.parsing.EthqlParser.ArrayValueContext;
import au.csiro.data61.aap.etl.parsing.EthqlParser.LiteralContext;

/**
 * AnalyzerUtils
 */
public class AnalyzerUtils {
    private static final String BYTES_PATTERN = "0x[0-9a-fA-F]*";
    private static final int ADDRESS_LENGTH = 42;
    private static final int MINIMUM_BYTES_LENGTH = 4;
    private static final int MAXIMUM_BYTES_LENGTH = 66;

    static boolean isTypeCompatible(String type, LiteralContext ctx) {
        final String ctxType = getType(ctx);
        return TypeUtils.areCompatible(type, ctxType);
    }

    static String getType(LiteralContext ctx) {
        if (ctx.BOOLEAN_LITERAL() != null) {
            return TypeUtils.BOOL_TYPE_KEYWORD;
        }
        else if (ctx.BYTE_AND_ADDRESS_LITERAL() != null) {
            return TypeUtils.BYTES_TYPE_KEYWORD;
        }
        else if (ctx.FIXED_LITERAL() != null) {
            return TypeUtils.FIXED_TYPE_KEYWORD;
        }
        else if (ctx.INT_LITERAL() != null) {
            return TypeUtils.INT_TYPE_KEYWORD;
        }
        else if (ctx.STRING_LITERAL() != null) {
            return TypeUtils.STRING_TYPE_KEYWORD;
        }
        else if (ctx.arrayValue() != null) {
            return getArrayType(ctx.arrayValue());
        }
        else {
            throw new IllegalArgumentException(String.format("Type '%s' unknown.", ctx.getText()));
        }
    }

    static String getArrayType(ArrayValueContext ctx) {
        String baseType = null;
        if (ctx.booleanArrayValue() != null) {
            baseType = TypeUtils.BOOL_TYPE_KEYWORD;
        }
        else if (ctx.byteAndAddressArrayValue() != null) {
            baseType = TypeUtils.BYTES_TYPE_KEYWORD;
        }
        else if (ctx.fixedArrayValue() != null) {
            baseType = TypeUtils.FIXED_TYPE_KEYWORD;
        }
        else if (ctx.intArrayValue() != null) {
            baseType = TypeUtils.INT_TYPE_KEYWORD;
        }
        else if (ctx.stringArrayValue() != null) {
            baseType = TypeUtils.STRING_TYPE_KEYWORD;
        }
        else {
            throw new IllegalArgumentException(String.format("Type '%s' unknown.", ctx.getText()));
        }
        return TypeUtils.getArrayType(baseType);
    }

    static boolean isAddress(String type, LiteralContext ctx) {
        return ctx.BYTE_AND_ADDRESS_LITERAL() != null && isAddressLiteral(ctx.BYTE_AND_ADDRESS_LITERAL());
    }

    static boolean isAddress(ArrayValueContext ctx) {
        return    ctx.byteAndAddressArrayValue() != null 
               && ctx.byteAndAddressArrayValue()
                    .BYTE_AND_ADDRESS_LITERAL()
                    .stream().allMatch(AnalyzerUtils::isAddressLiteral);
    }

    static boolean isAddressLiteral(TerminalNode node) {
        return isAddressLiteral(node.getText());
    }

    static boolean isAddressLiteral(String literal) {
        assert literal != null;
        return literal.matches(BYTES_PATTERN) && literal.length() != ADDRESS_LENGTH;
    }

    static boolean isBytesLiteral(String literal) {
        return     literal.matches(BYTES_PATTERN) 
                && literal.length() % 2 == 0
                && MINIMUM_BYTES_LENGTH <= literal.length()
                && literal.length() <= MAXIMUM_BYTES_LENGTH;
    }

    static String tokenPositionString(Token token) {
        return String.format("Ln %s, Col %s", token.getLine(), token.getCharPositionInLine());
    }

}