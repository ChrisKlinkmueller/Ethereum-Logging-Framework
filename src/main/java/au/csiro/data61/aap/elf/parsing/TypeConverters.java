package au.csiro.data61.aap.elf.parsing;

import au.csiro.data61.aap.elf.parsing.EthqlParser.LiteralContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.ValueExpressionContext;
import au.csiro.data61.aap.elf.util.TypeUtils;

/**
 * LiteralUtils
 */
class TypeConverters {

    public static String determineType(ValueExpressionContext ctx, VariableExistenceAnalyzer varAnalyzer) {
        return ctx.literal() != null
            ? TypeConverters.literalType(ctx.literal())
            : varAnalyzer.getVariableType(ctx.variableName().getText());
    }

    public static String literalType(LiteralContext ctx) {        
        if (ctx.BOOLEAN_LITERAL() != null) {
            return TypeUtils.BOOL_TYPE_KEYWORD;
        }
        else if (ctx.BYTES_LITERAL() != null) {
            return TypeUtils.BYTES_TYPE_KEYWORD;
        }
        else if (ctx.INT_LITERAL() != null) {
            return TypeUtils.BYTES_TYPE_KEYWORD;
        }
        else if (ctx.STRING_LITERAL() != null) {
            return TypeUtils.STRING_TYPE_KEYWORD;
        }
        else if (ctx.arrayLiteral() != null) {
            if (ctx.arrayLiteral().booleanArrayLiteral() != null) {
                return TypeUtils.BOOL_TYPE_KEYWORD;
            }
            if (ctx.arrayLiteral().bytesArrayLiteral() != null) {
                return TypeUtils.BYTES_TYPE_KEYWORD;
            }
            if (ctx.arrayLiteral().intArrayLiteral() != null) {
                return TypeUtils.INT_TYPE_KEYWORD;
            }
            if (ctx.arrayLiteral().stringArrayLiteral() != null) {
                return TypeUtils.STRING_TYPE_KEYWORD;
            }
        } 

        throw new UnsupportedOperationException(String.format("Literal '%s' not supported", ctx.getText()));
    }    
}