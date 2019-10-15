package au.csiro.data61.aap.parser;

import au.csiro.data61.aap.specification.types.AddressType;
import au.csiro.data61.aap.specification.types.ArrayType;
import au.csiro.data61.aap.specification.types.BoolType;
import au.csiro.data61.aap.specification.types.BytesType;
import au.csiro.data61.aap.specification.types.FixedType;
import au.csiro.data61.aap.specification.types.IntegerType;
import au.csiro.data61.aap.specification.types.SolidityType;
import au.csiro.data61.aap.specification.types.StringType;
import au.csiro.data61.aap.state.ProgramState;
import au.csiro.data61.aap.util.MethodResult;
import au.csiro.data61.aap.util.StringUtil;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.function.BiFunction;

import org.antlr.v4.runtime.Token;

import au.csiro.data61.aap.parser.XbelParser.SolTypeContext;
import au.csiro.data61.aap.parser.XbelParser.SolTypeStartRuleContext;

/**
 * SolidityTypeVisitor
 */
public class SolidityTypeVisitor extends StatefulVisitor<SpecificationParserResult<SolidityType<?>>> {
    public final static String BYTES_TYPE_DYNAMIC_SUFFIX = "s";
    public static final String ADDRESS_TYPE_NAME = "address";
    public static final String BYTES_BASE_KEYWORD = "byte";
    public static final String BOOLT_TYPE_NAME = "bool";
    public static final int BYTES_DEFAULT_LENGHT = 1;
    public static final String UNSIGNED_PREFIX = "u";
    public static final String FIXED_M_N_DIVIDER = "x";
    public static final String FIXED_TYPE_NAME = "fixed";
    public static final String INTEGER_TYPE_NAME = "int";
    private static final String STRING_TYPE_NAME = "string";
    private static final String ARRAY_TYPE_REGEX = "[a-zA-Z0-9]*\\[\\]"; 
    private static final String ARRAY_TYPE_SUFFIX = "[]";

    public SolidityTypeVisitor(ProgramState state) {
        super(state);
    }

    @Override
    public SpecificationParserResult<SolidityType<?>> visitSolTypeStartRule(SolTypeStartRuleContext ctx) {
        return this.visitSolType(ctx.solType());
    }

    @Override
    public SpecificationParserResult<SolidityType<?>> visitSolType(SolTypeContext ctx) {
        if (ctx == null  || ctx.getText() == null || ctx.getText().trim().isEmpty()) {
            return SpecificationParserResult.ofError("No type provided.");
        }

        if (ctx.SOL_ADDRESS_TYPE() != null) {
            return castResult(mapToAddressType(ctx.SOL_ADDRESS_TYPE().getText(), ctx.SOL_ADDRESS_TYPE().getSymbol()));
        }
        else if (ctx.SOL_ADDRESS_ARRAY_TYPE() != null) {
            return castResult(mapToArrayType(ctx.SOL_ADDRESS_ARRAY_TYPE().getText(), ctx.SOL_ADDRESS_ARRAY_TYPE().getSymbol(), SolidityTypeVisitor::mapToAddressType));
        }
        else if (ctx.SOL_BOOL_TYPE() != null) {
            return castResult(mapToBoolType(ctx.SOL_BOOL_TYPE().getText(), ctx.SOL_BOOL_TYPE().getSymbol()));
        }
        else if (ctx.SOL_BOOL_ARRAY_TYPE() != null) {
            return castResult(mapToArrayType(ctx.SOL_BOOL_ARRAY_TYPE().getText(), ctx.SOL_BOOL_ARRAY_TYPE().getSymbol(), SolidityTypeVisitor::mapToBoolType));
        }
        else if (ctx.SOL_BYTE_TYPE() != null) {
            return castResult(mapToBytesType(ctx.SOL_BYTE_TYPE().getText(), ctx.SOL_BYTE_TYPE().getSymbol()));
        }
        else if (ctx.SOL_BYTE_ARRAY_TYPE() != null) {
            return castResult(mapToArrayType(ctx.SOL_BYTE_ARRAY_TYPE().getText(), ctx.SOL_BYTE_ARRAY_TYPE().getSymbol(), SolidityTypeVisitor::mapToBytesType));
        }
        else if (ctx.SOL_FIXED_TYPE() != null) {
            return castResult(mapToFixedType(ctx.SOL_FIXED_TYPE().getText(), ctx.SOL_FIXED_TYPE().getSymbol()));
        }
        else if (ctx.SOL_FIXED_ARRAY_TYPE() != null) {
            return castResult(mapToArrayType(ctx.SOL_FIXED_ARRAY_TYPE().getText(), ctx.SOL_FIXED_ARRAY_TYPE().getSymbol(), SolidityTypeVisitor::mapToFixedType));
        }
        else if (ctx.SOL_INT_TYPE() != null) {
            return castResult(mapToIntegerType(ctx.SOL_INT_TYPE().getText(), ctx.SOL_INT_TYPE().getSymbol()));
        }
        else if (ctx.SOL_INT_ARRAY_TYPE() != null) {
            return castResult(mapToArrayType(ctx.SOL_INT_ARRAY_TYPE().getText(), ctx.SOL_INT_ARRAY_TYPE().getSymbol(), SolidityTypeVisitor::mapToIntegerType));
        }
        else if (ctx.SOL_STRING_TYPE() != null) {
            return castResult(mapToStringType(ctx.SOL_STRING_TYPE().getText(), ctx.SOL_STRING_TYPE().getSymbol()));
        }
        else {
            return SpecificationParserResult.ofError(ctx.start, String.format("'%s' is not a valid type.", ctx.getText()));
        }
    }

    private static SpecificationParserResult<SolidityType<String>> mapToAddressType(String keyword, Token token) {
        if (keyword.equals(ADDRESS_TYPE_NAME)) {
            return SpecificationParserResult.ofResult(new AddressType());
        }
        return SpecificationParserResult.ofError(token, String.format("'%s' is not a valid Address type.", keyword));
    }

    private static SpecificationParserResult<SolidityType<Boolean>> mapToBoolType(String keyword, Token token) {
        if (keyword.equals(BOOLT_TYPE_NAME)) {
            return SpecificationParserResult.ofResult(new BoolType());
        }
        return SpecificationParserResult.ofError(token, String.format("'%s' is not a valid Boolean type.", keyword));
    }

    private static SpecificationParserResult<SolidityType<String>> mapToBytesType(String keyword, Token token) {
        if (!keyword.startsWith(BYTES_BASE_KEYWORD)) {
            return SpecificationParserResult.ofError(token, String.format("'%s' is not a valid Bytes type.", keyword));
        }

        keyword = keyword.replaceFirst(BYTES_BASE_KEYWORD, "");
        if (keyword.isEmpty()) {
            return SpecificationParserResult.ofResult(new BytesType(BYTES_DEFAULT_LENGHT));
        }
        
        if (!keyword.startsWith(BYTES_TYPE_DYNAMIC_SUFFIX)) {
            return SpecificationParserResult.ofError(token, String.format("'%s' is not a valid Bytes type.", keyword));
        }

        keyword = keyword.replaceFirst(BYTES_TYPE_DYNAMIC_SUFFIX, "");
        if (keyword.isEmpty()) {
            return SpecificationParserResult.ofResult(new BytesType());
        }

        final MethodResult<Integer> valueResult = StringUtil.parseInt(keyword);
        if (!valueResult.isSuccessful() || valueResult.getResult() < BytesType.MIN_STATIC_LENGTH || BytesType.MAX_STATIC_LENGTH < valueResult.getResult()) {
            return SpecificationParserResult.ofError(token, "The length of a bytes type must be on the interval [1,32], but was not.");
        }              
        return SpecificationParserResult.ofResult(new BytesType(valueResult.getResult()));
    }

    /**
     * Creates a FixedType instance based on the keyword. A valid keyword matches the regex u?fixed<M>x<N> where
     * M must be divisible by 8 and on the interval [8,256] and N must be on the interval [0,80], more information 
     * <a href="https://solidity.readthedocs.io/en/v0.5.11/types.html#fixed-point-numbers">here</a>. The method
     * returns null, if the keyword is invalid.
     * @param keyword the keyword
     * @return  a FixedType instance
     */
    private static SpecificationParserResult<SolidityType<BigDecimal>> mapToFixedType(String keyword, Token token) {
        final boolean unsigned = keyword.startsWith(UNSIGNED_PREFIX);
        if (unsigned) {
            keyword = keyword.replaceFirst(UNSIGNED_PREFIX, "");
        }

        if (!keyword.startsWith(FIXED_TYPE_NAME)) {
            return SpecificationParserResult.ofError(token, String.format("'%s' is not a valid Fixed type.", keyword));
        }

        keyword = keyword.replaceFirst(FIXED_TYPE_NAME, "");
        if (keyword.isEmpty()) {
            return SpecificationParserResult.ofResult(new FixedType(!unsigned));
        }

        final String[] precisionConfig = keyword.split(FIXED_M_N_DIVIDER);
        if (precisionConfig.length != 2) {
            return SpecificationParserResult.ofError(token, String.format("'%s' is not a valid Fixed type.", keyword));
        }

        final MethodResult<Integer> mResult = StringUtil.parseInt(precisionConfig[0]);
        if (!mResult.isSuccessful() || !FixedType.isValidMValue(mResult.getResult())) {
            return SpecificationParserResult.ofError(token, String.format("'%s' is not a valid Fixed type.", keyword));
        }

        final MethodResult<Integer> nResult = StringUtil.parseInt(precisionConfig[1]);
        if (!nResult.isSuccessful() || !FixedType.isValidNValue(nResult.getResult())) {
            return SpecificationParserResult.ofError(token, String.format("'%s' is not a valid Fixed type.", keyword));
        }

        return SpecificationParserResult.ofResult(new FixedType(!unsigned, mResult.getResult(), nResult.getResult()));
    }

    private static <T> SpecificationParserResult<SolidityType<BigInteger>> mapToIntegerType(String keyword, Token token) {
        final boolean unsigned = keyword.startsWith(UNSIGNED_PREFIX);
        if (unsigned) {
            keyword = keyword.replaceFirst(UNSIGNED_PREFIX, "");
        }

        if (!keyword.startsWith(INTEGER_TYPE_NAME)) {
            return SpecificationParserResult.ofError(token, String.format("'%s' is not a valid Integer type.", keyword));
        }

        keyword = keyword.replaceFirst(INTEGER_TYPE_NAME, "");
        if (keyword.isEmpty()) {
            return SpecificationParserResult.ofResult(new IntegerType(!unsigned));
        }

        final MethodResult<Integer> lengthResult = StringUtil.parseInt(keyword);
        return lengthResult.isSuccessful()
            ? SpecificationParserResult.ofResult(new IntegerType(!unsigned, lengthResult.getResult())) 
            : SpecificationParserResult.ofError(token, String.format("'%s' is not a valid Integer type.", keyword));
    }
    private static <T> SpecificationParserResult<SolidityType<String>> mapToStringType(String keyword, Token token) {
        return keyword.equals(STRING_TYPE_NAME) 
            ? SpecificationParserResult.ofResult(new StringType()) 
            : SpecificationParserResult.ofError(token, String.format("'%s' is not a valid String type.", keyword));
    }
    
    private static <T> SpecificationParserResult<SolidityType<List<T>>> mapToArrayType(
        String keyword, 
        Token token, 
        BiFunction<String, Token, SpecificationParserResult<SolidityType<T>>> baseTypeConverter) {
        if (!keyword.matches(ARRAY_TYPE_REGEX)) {
            return SpecificationParserResult.ofError(token, String.format("'%s' is not a valid Array type.", keyword));
        }

        final SpecificationParserResult<SolidityType<T>> baseType = baseTypeConverter.apply(keyword.substring(0, keyword.length() - ARRAY_TYPE_SUFFIX.length()), token);
        if (!baseType.isSuccessful()) {
            return SpecificationParserResult.ofUnsuccessfulParserResult(baseType);
        }

        return SpecificationParserResult.ofResult(new ArrayType<>(baseType.getResult()));
    }

    private static <T> SpecificationParserResult<SolidityType<?>> castResult(SpecificationParserResult<SolidityType<T>> result) {
        if (!result.isSuccessful()) {
            return SpecificationParserResult.ofUnsuccessfulParserResult(result);
        }
        final SolidityType<?> type = result.getResult();
        return SpecificationParserResult.ofResult(type);
    }
    
}