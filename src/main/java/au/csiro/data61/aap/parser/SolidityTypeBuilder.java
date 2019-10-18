package au.csiro.data61.aap.parser;

import java.util.function.Function;

import org.antlr.v4.runtime.tree.TerminalNode;

import au.csiro.data61.aap.parser.XbelParser.SolTypeContext;
import au.csiro.data61.aap.parser.XbelParser.SolTypeRuleContext;
import au.csiro.data61.aap.spec.types.AddressType;
import au.csiro.data61.aap.spec.types.ArrayType;
import au.csiro.data61.aap.spec.types.BoolType;
import au.csiro.data61.aap.spec.types.BytesType;
import au.csiro.data61.aap.spec.types.FixedType;
import au.csiro.data61.aap.spec.types.IntegerType;
import au.csiro.data61.aap.spec.types.SolidityType;
import au.csiro.data61.aap.spec.types.StringType;
import au.csiro.data61.aap.util.MethodResult;
import au.csiro.data61.aap.util.StringUtil;

/**
 * SolidityTypeBuilder
 */
public class SolidityTypeBuilder extends XbelBaseVisitor<SpecificationParserResult<SolidityType>> {
    private final static String BYTES_TYPE_DYNAMIC_SUFFIX = "s";
    private static final String ADDRESS_TYPE_NAME = "address";
    private static final String BYTES_BASE_KEYWORD = "byte";
    private static final String BOOLT_TYPE_NAME = "bool";
    private static final int BYTES_DEFAULT_LENGHT = 1;
    private static final String UNSIGNED_PREFIX = "u";
    private static final String FIXED_M_N_DIVIDER = "x";
    private static final String FIXED_TYPE_NAME = "fixed";
    private static final String INTEGER_TYPE_NAME = "int";
    private static final String STRING_TYPE_NAME = "string";
    private static final String ARRAY_TYPE_REGEX = "[a-zA-Z0-9]*\\[\\]"; 
    private static final String ARRAY_TYPE_SUFFIX = "[]";

    @Override
    public SpecificationParserResult<SolidityType> visitSolTypeRule(SolTypeRuleContext ctx) {
        return this.visitSolType(ctx.solType());
    }

    @Override
    public SpecificationParserResult<SolidityType> visitSolType(SolTypeContext ctx) {
        if (ctx == null  || ctx.getText() == null || ctx.getText().trim().isEmpty()) {
            return SpecificationParserResult.ofError("No type provided.");
        }

        if (ctx.SOL_ADDRESS_TYPE() != null) {
            return this.parseType(this::parseAddressType, ctx.SOL_ADDRESS_TYPE());
        }
        else if (ctx.SOL_ADDRESS_ARRAY_TYPE() != null) {
            return this.parseType(k -> this.parseArrayType(k, this::parseAddressType), ctx.SOL_ADDRESS_ARRAY_TYPE());
        }
        else if (ctx.SOL_BOOL_TYPE() != null) {
            return this.parseType(this::parseBoolType, ctx.SOL_BOOL_TYPE());
        }
        else if (ctx.SOL_BOOL_ARRAY_TYPE() != null) {
            return this.parseType(k -> this.parseArrayType(k, this::parseBoolType), ctx.SOL_BOOL_ARRAY_TYPE());
        }
        else if (ctx.SOL_BYTE_TYPE() != null) {
            return this.parseType(this::parseBytesType, ctx.SOL_BYTE_TYPE());
        }
        else if (ctx.SOL_BYTE_ARRAY_TYPE() != null) {
            return this.parseType(k -> this.parseArrayType(k, this::parseBytesType), ctx.SOL_BYTE_ARRAY_TYPE());
        }
        else if (ctx.SOL_FIXED_TYPE() != null) {
            return this.parseType(this::parseFixedType, ctx.SOL_FIXED_TYPE());
        }
        else if (ctx.SOL_FIXED_ARRAY_TYPE() != null) {
            return this.parseType(k -> this.parseArrayType(k, this::parseFixedType), ctx.SOL_FIXED_ARRAY_TYPE());
        }
        else if (ctx.SOL_INT_TYPE() != null) {
            return this.parseType(this::parseIntegerType, ctx.SOL_INT_TYPE());
        }
        else if (ctx.SOL_INT_ARRAY_TYPE() != null) {
            return this.parseType(k -> this.parseArrayType(k, this::parseIntegerType), ctx.SOL_INT_ARRAY_TYPE());
        }
        else if (ctx.SOL_STRING_TYPE() != null) {
            return this.parseType(this::parseStringType, ctx.SOL_STRING_TYPE());
        }
        else {
            return SpecificationParserResult.ofError(ctx.start, String.format("'%s' is not a valid type.", ctx.getText()));
        }
    }

    private SpecificationParserResult<SolidityType> parseType(Function<String, SpecificationParserResult<SolidityType>> parser, TerminalNode node) {
        final SpecificationParserResult<SolidityType> parseResult = parser.apply(node.getText());
        if (parseResult.isSuccessful()) {
            return parseResult;
        }

        return SpecificationParserResult.ofUnsuccessfulParserResult(node.getSymbol(), parseResult);
    }

    public SpecificationParserResult<SolidityType> parseAddressType(String keyword) {
        if (keyword == null) {
            return SpecificationParserResult.ofError("The keyword parameter cannot be null.");
        }

        if (keyword.equals(ADDRESS_TYPE_NAME)) {
            return SpecificationParserResult.ofResult(AddressType.DEFAULT_INSTANCE);
        }
        return SpecificationParserResult.ofError(String.format("'%s' is not a valid Address type.", keyword));
    }

    public SpecificationParserResult<SolidityType> parseBoolType(String keyword) {
        if (keyword == null) {
            return SpecificationParserResult.ofError("The keyword parameter cannot be null.");
        }

        if (keyword.equals(BOOLT_TYPE_NAME)) {
            return SpecificationParserResult.ofResult(BoolType.DEFAULT_INSTANCE);
        }
        return SpecificationParserResult.ofError(String.format("'%s' is not a valid Bool type.", keyword));
    }

    public SpecificationParserResult<SolidityType> parseBytesType(String keyword) {
        if (keyword == null) {
            return SpecificationParserResult.ofError("The keyword parameter cannot be null.");
        }
        
        if (!keyword.startsWith(BYTES_BASE_KEYWORD)) {
            return SpecificationParserResult.ofError( String.format("'%s' is not a valid Bytes type.", keyword));
        }

        keyword = keyword.replaceFirst(BYTES_BASE_KEYWORD, "");
        if (keyword.isEmpty()) {
            return SpecificationParserResult.ofResult(new BytesType(BYTES_DEFAULT_LENGHT));
        }
        
        if (!keyword.startsWith(BYTES_TYPE_DYNAMIC_SUFFIX)) {
            return SpecificationParserResult.ofError(String.format("'%s' is not a valid Bytes type.", keyword));
        }

        keyword = keyword.replaceFirst(BYTES_TYPE_DYNAMIC_SUFFIX, "");
        if (keyword.isEmpty()) {
            return SpecificationParserResult.ofResult(BytesType.DEFAULT_INSTANCE);
        }

        final MethodResult<Integer> valueResult = StringUtil.parseInt(keyword);
        if (!valueResult.isSuccessful() || !BytesType.isValidLength(valueResult.getResult())) {
            return SpecificationParserResult.ofError("The length of a bytes type must be on the interval [1,32], but was not.");
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
    public SpecificationParserResult<SolidityType> parseFixedType(String keyword) {
        if (keyword == null) {
            return SpecificationParserResult.ofError("The keyword parameter cannot be null.");
        }

        final boolean unsigned = keyword.startsWith(UNSIGNED_PREFIX);
        if (unsigned) {
            keyword = keyword.replaceFirst(UNSIGNED_PREFIX, "");
        }

        if (!keyword.startsWith(FIXED_TYPE_NAME)) {
            return SpecificationParserResult.ofError(String.format("'%s' is not a valid Fixed type.", keyword));
        }

        keyword = keyword.replaceFirst(FIXED_TYPE_NAME, "");
        if (keyword.isEmpty()) {
            return SpecificationParserResult.ofResult(new FixedType(!unsigned));
        }

        final String[] precisionConfig = keyword.split(FIXED_M_N_DIVIDER);
        if (precisionConfig.length != 2) {
            return SpecificationParserResult.ofError(String.format("'%s' is not a valid Fixed type.", keyword));
        }

        final MethodResult<Integer> mResult = StringUtil.parseInt(precisionConfig[0]);
        if (!mResult.isSuccessful() || !FixedType.isValidMValue(mResult.getResult())) {
            return SpecificationParserResult.ofError(String.format("'%s' is not a valid Fixed type.", keyword));
        }

        final MethodResult<Integer> nResult = StringUtil.parseInt(precisionConfig[1]);
        if (!nResult.isSuccessful() || !FixedType.isValidNValue(nResult.getResult())) {
            return SpecificationParserResult.ofError(String.format("'%s' is not a valid Fixed type.", keyword));
        }

        return SpecificationParserResult.ofResult(new FixedType(!unsigned, mResult.getResult(), nResult.getResult()));
    }

    public SpecificationParserResult<SolidityType> parseIntegerType(String keyword) {
        if (keyword == null) {
            return SpecificationParserResult.ofError("The keyword parameter cannot be null.");
        }

        final boolean unsigned = keyword.startsWith(UNSIGNED_PREFIX);
        if (unsigned) {
            keyword = keyword.replaceFirst(UNSIGNED_PREFIX, "");
        }

        if (!keyword.startsWith(INTEGER_TYPE_NAME)) {
            return SpecificationParserResult.ofError( String.format("'%s' is not a valid Integer type.", keyword));
        }

        keyword = keyword.replaceFirst(INTEGER_TYPE_NAME, "");
        if (keyword.isEmpty()) {
            return SpecificationParserResult.ofResult(new IntegerType(!unsigned));
        }

        final MethodResult<Integer> lengthResult = StringUtil.parseInt(keyword);
        return lengthResult.isSuccessful() && IntegerType.isValidLength(lengthResult.getResult())
            ? SpecificationParserResult.ofResult(new IntegerType(!unsigned, lengthResult.getResult())) 
            : SpecificationParserResult.ofError(String.format("'%s' is not a valid Integer type.", keyword));
    }
    
    public SpecificationParserResult<SolidityType> parseStringType(String keyword) {
        if (keyword == null) {
            return SpecificationParserResult.ofError("The keyword parameter cannot be null.");
        }

        return keyword.equals(STRING_TYPE_NAME) 
            ? SpecificationParserResult.ofResult(new StringType()) 
            : SpecificationParserResult.ofError(String.format("'%s' is not a valid String type.", keyword));
    }

    private SpecificationParserResult<SolidityType> parseArrayType(String keyword, Function<String, SpecificationParserResult<SolidityType>> baseTypeConverter) {
        if (!keyword.matches(ARRAY_TYPE_REGEX)) {
            return SpecificationParserResult.ofError(String.format("'%s' is not a valid Array type.", keyword));
        }

        final SpecificationParserResult<SolidityType> baseType = baseTypeConverter.apply(keyword.substring(0, keyword.length() - ARRAY_TYPE_SUFFIX.length()));
        if (!baseType.isSuccessful()) {
            return SpecificationParserResult.ofUnsuccessfulParserResult(baseType);
        }

        return SpecificationParserResult.ofResult(new ArrayType(baseType.getResult()));
    }

}