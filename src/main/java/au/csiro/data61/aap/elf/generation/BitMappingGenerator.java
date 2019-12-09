// package au.csiro.data61.aap.elf.generation;

// import java.math.BigInteger;
// import java.util.List;

// import au.csiro.data61.aap.elf.library.compression.BitMapping;
// import au.csiro.data61.aap.elf.parsing.EthqlParser.ArrayLiteralContext;
// import au.csiro.data61.aap.elf.parsing.EthqlParser.LogEntryFilterContext;
// import au.csiro.data61.aap.elf.parsing.EthqlParser.MethodInvocationContext;
// import au.csiro.data61.aap.elf.parsing.EthqlParser.ValueExpressionContext;
// import au.csiro.data61.aap.elf.parsing.EthqlParser.VariableNameContext;
// import au.csiro.data61.aap.elf.util.TypeUtils;

// /**
//  * BitMappingGenerator
//  */
// public class BitMappingGenerator {
//     private static final BigInteger MIN_SHIFT_VALUE = BigInteger.ZERO;
//     private static final BigInteger MAX_SHIFT_VALUE = BigInteger.valueOf(255);
//     private static final int VAR_NAME_INDEX = 0;
//     private static final int FROM_INDEX = 1;
//     private static final int TO_INDEX = 2;
//     private static final int VALUES_INDEX = 3;


    



//     @Override
//     public void exitMethodInvocation(MethodInvocationContext ctx) {
//         if (!ctx.methodName.getText().equals(BitMapping.METHOD_NAME)) {
//             return;
//         }

//         this.startCodeSnippet(ctx);

//         final String variableName = this.getVariableName(ctx);
//         final BigInteger from = this.getFrom(ctx);
//         final BigInteger to = this.getTo(ctx);
//         final List<?> values = this.getValues(ctx);

//         if (   variableName == null || from == null || to == null || values == null
//             || from.compareTo(MIN_SHIFT_VALUE) < 0 || from.compareTo(MAX_SHIFT_VALUE) > 0
//             || to.compareTo(MIN_SHIFT_VALUE) < 0 || to.compareTo(MAX_SHIFT_VALUE) > 0
//             || from.compareTo(to) > 0
//         ) {
//             this.appendLineComment("Can only generate bit mapping code for the following type of calls");
//             this.appendLineComment(
//                 String.format(
//                     "%s (int-variable, int-literal in [%s, %s] , int-literal in [%s, %s], array-literal)", 
//                     BitMapping.METHOD_NAME,
//                     MIN_SHIFT_VALUE, MAX_SHIFT_VALUE,
//                     MIN_SHIFT_VALUE, MAX_SHIFT_VALUE
//                 )
//             );
//         }


//     }  
    
//     private String getVariableName(MethodInvocationContext ctx) {
//         final VariableNameContext varCtx = ctx.valueExpression().get(VAR_NAME_INDEX).variableName(); 
//         return varCtx == null ? null : varCtx.getText();
//     } 

//     private BigInteger getFrom(MethodInvocationContext ctx) {
//         return this.getPosition(ctx, FROM_INDEX);
//     }

//     private BigInteger getTo(MethodInvocationContext ctx) {
//         return this.getPosition(ctx, TO_INDEX);
//     }

//     private BigInteger getPosition(MethodInvocationContext ctx, int index) {
//         final ValueExpressionContext posCtx = ctx.valueExpression().get(1);
//         return posCtx.literal() == null || posCtx.literal().INT_LITERAL() == null 
//             ? null : TypeUtils.integerFromLiteral(posCtx.getText());
//     }

//     private List<?> getValues(MethodInvocationContext ctx) {
//         final ValueExpressionContext valuesCtx = ctx.valueExpression(VALUES_INDEX);
//         if (valuesCtx.literal() == null || valuesCtx.literal().arrayLiteral() == null) {
//             return null;
//         }

//         final ArrayLiteralContext literalCtx = valuesCtx.literal().arrayLiteral();
//         final String literal = literalCtx.getText();
//         if (literalCtx.booleanArrayLiteral() != null) {
//             return TypeUtils.parseBoolArrayLiteral(literal);
//         }
//         else if (literalCtx.bytesArrayLiteral() != null) {
//             return TypeUtils.parseBytesArrayLiteral(literal);
//         }
//         else if (literalCtx.intArrayLiteral() != null) {
//             return TypeUtils.parseIntArrayLiteral(literal);
//         }
//         else if (literalCtx.stringArrayLiteral() != null) {
//             return TypeUtils.parseStringArrayLiteral(literal);
//         }
//         else {
//             throw new UnsupportedOperationException(String.format("This type of array literal ('%s') is not supported.", literal));
//         }
//     }



//     public static int encode(int[] values, int[] powers) {
//         int code = values[0];
//         for (int i = 1; i < values.length; i++) {
//             code = code << powers[i];
//             code = code ^ values[i];
//         }
//         return code;
//     }
// }