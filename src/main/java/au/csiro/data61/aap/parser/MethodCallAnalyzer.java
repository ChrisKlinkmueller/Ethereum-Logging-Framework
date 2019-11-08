package au.csiro.data61.aap.parser;

import java.util.HashMap;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import au.csiro.data61.aap.library.Library;
import au.csiro.data61.aap.parser.XbelParser.ArrayValueContext;
import au.csiro.data61.aap.parser.XbelParser.LiteralContext;
import au.csiro.data61.aap.parser.XbelParser.MethodCallContext;
import au.csiro.data61.aap.parser.XbelParser.MethodParameterContext;
import au.csiro.data61.aap.spec.Method;
import au.csiro.data61.aap.spec.types.SolidityAddress;
import au.csiro.data61.aap.spec.types.SolidityArray;
import au.csiro.data61.aap.spec.types.SolidityBool;
import au.csiro.data61.aap.spec.types.SolidityBytes;
import au.csiro.data61.aap.spec.types.SolidityFixed;
import au.csiro.data61.aap.spec.types.SolidityInteger;
import au.csiro.data61.aap.spec.types.SolidityString;
import au.csiro.data61.aap.spec.types.SolidityType;

/**
 * MethodCallAnalyzer
 */
class MethodCallAnalyzer extends SemanticAnalyzer {
    private final VariableAnalyzer variableAnalyzer;
    private final HashMap<Class<?>, BiPredicate<SolidityType, MethodParameterContext>> baseTypeChecks;
    private final HashMap<Class<?>, Predicate<ArrayValueContext>> arrayTypeChecks;

    public MethodCallAnalyzer(ErrorCollector errorCollector, VariableAnalyzer variableAnalyzer) {
        super(errorCollector);
        assert variableAnalyzer != null;
        this.variableAnalyzer = variableAnalyzer;

        this.baseTypeChecks = new HashMap<>();
        this.baseTypeChecks.put(SolidityAddress.class, this::isAddress);
        this.baseTypeChecks.put(SolidityBool.class, this::isBool);
        this.baseTypeChecks.put(SolidityBytes.class, this::isBytes);
        this.baseTypeChecks.put(SolidityFixed.class, this::isFixed);
        this.baseTypeChecks.put(SolidityInteger.class, this::isInteger);
        this.baseTypeChecks.put(SolidityString.class, this::isString);
        this.baseTypeChecks.put(SolidityArray.class, this::isArray);

        this.arrayTypeChecks = new HashMap<>();
        this.arrayTypeChecks.put(SolidityAddress.class, this::isAddressArray);
        this.arrayTypeChecks.put(SolidityBool.class, this::isBoolArray);
        this.arrayTypeChecks.put(SolidityBytes.class, this::isBytesArray);
        this.arrayTypeChecks.put(SolidityFixed.class, this::isFixedArray);
        this.arrayTypeChecks.put(SolidityInteger.class, this::isIntegerArray);
        this.arrayTypeChecks.put(SolidityString.class, this::isStringArray);
    }

    @Override
    public void clear() {
    }

    @Override
    public void exitMethodCall(MethodCallContext ctx) {
        if (!Library.INSTANCE.isMethodNameKnown(ctx.methodName.getText())) {
            this.addError(ctx.methodName,
                    String.format("A method with the name '%s' is not known.", ctx.methodName.getText()));
            return;
        }

        if (!this.existsMethodWithSignature(ctx)) {
            this.addError(ctx.methodName,
                    String.format("The method '%s' is not applicable for these parameters.", ctx.methodName.getText()));
        }
    }

    private boolean existsMethodWithSignature(MethodCallContext ctx) {
        return Library.INSTANCE.methodStream(ctx.methodName.getText())
                .anyMatch(method -> this.areParametersMatching(method, ctx));
    }

    private boolean areParametersMatching(Method method, MethodCallContext ctx) {
        if (method.getSignature().parameterTypeCount() != ctx.methodParameter().size()) {
            return false;
        }

        return IntStream.range(0, ctx.methodParameter().size())
            .allMatch(i -> this.areTypesMatching(method.getSignature().getParameterType(i), ctx.methodParameter(i)));
    }

    private boolean areTypesMatching(SolidityType solType, MethodParameterContext paramType) {
        final BiPredicate<SolidityType, MethodParameterContext> check = this.baseTypeChecks.get(solType.getClass());
        return check != null && check.test(solType, paramType);
    }    

    private boolean isArray(SolidityType solType, MethodParameterContext ctx) {
        if (ctx.variableReference() != null) {
            return this.variableAnalyzer.existsVariable(ctx.variableReference().getText(), solType);
        }

        if (ctx.literal().arrayValue() != null) {
            final SolidityType baseType = ((SolidityArray)solType);
            final Predicate<ArrayValueContext> check = this.arrayTypeChecks.get(baseType.getClass());
            final ArrayValueContext arrayCtx = ctx.literal().arrayValue();
            return check != null && check.test(arrayCtx);
        }

        return false;
    }

    private boolean isAddress(SolidityType solType, MethodParameterContext ctx) {
        return this.hasType(ctx, solType, 
        literalCtx -> literalCtx.BYTE_AND_ADDRESS_LITERAL() != null && AnalyzerUtils.isAddressLiteral(literalCtx.BYTE_AND_ADDRESS_LITERAL())
        );
    }

    private boolean isAddressArray(ArrayValueContext ctx) {
        return    ctx.byteAndAddressArrayValue() != null 
               && ctx.byteAndAddressArrayValue().BYTE_AND_ADDRESS_LITERAL().stream().allMatch(AnalyzerUtils::isAddressLiteral);
    }

    private boolean isBool(SolidityType solType, MethodParameterContext ctx) {
        return this.hasType(ctx, solType, literalCtx -> literalCtx.BOOLEAN_LITERAL() != null);
    }

    private boolean isBoolArray(ArrayValueContext ctx) {
        return ctx.booleanArrayValue() != null;
    }

    private boolean isBytes(SolidityType solType, MethodParameterContext ctx) {
        return this.hasType(
            ctx, solType, 
            literalCtx -> literalCtx.BYTE_AND_ADDRESS_LITERAL() != null && AnalyzerUtils.isBytesLiteral(literalCtx.BYTE_AND_ADDRESS_LITERAL())
        );
    }

    private boolean isBytesArray(ArrayValueContext ctx) {
        return    ctx.byteAndAddressArrayValue() != null
               && ctx.byteAndAddressArrayValue().BYTE_AND_ADDRESS_LITERAL().stream().allMatch(AnalyzerUtils::isBytesLiteral);
    }

    private boolean isFixed(SolidityType solType, MethodParameterContext ctx) {
        return this.hasType(ctx, solType, literalCtx -> literalCtx.FIXED_LITERAL() != null || literalCtx.INT_LITERAL() != null);
    }

    private boolean isFixedArray(ArrayValueContext ctx) {
        return ctx.fixedArrayValue() != null;
    }

    private boolean isInteger(SolidityType solType, MethodParameterContext ctx) {
        return this.hasType(ctx, solType, literalCtx -> literalCtx.INT_LITERAL() != null);
    }

    private boolean isIntegerArray(ArrayValueContext ctx) {
        return ctx.intArrayValue() != null;
    }

    private boolean isString(SolidityType solType, MethodParameterContext ctx) {
        return this.hasType(ctx, solType, literalCtx -> literalCtx.STRING_LITERAL() != null);
    }

    private boolean isStringArray(ArrayValueContext ctx) {
        return ctx.stringArrayValue() != null;
    }

    private boolean hasType(
        MethodParameterContext ctx, 
        SolidityType type, 
        Predicate<LiteralContext> literalCheck) {
        
        if (ctx.variableReference() != null) {
            return this.variableAnalyzer.existsVariable(ctx.variableReference().getText(), type);
        }

        if (ctx.literal() != null) {
            return literalCheck.test(ctx.literal());
        }

        return false;
    }

    // TODO: this will be the place for emit and if conditions, as they will be mapped to 
}