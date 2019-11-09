package au.csiro.data61.aap.parser;

import java.util.HashMap;
import java.util.stream.IntStream;

import au.csiro.data61.aap.library.Library;
import au.csiro.data61.aap.parser.XbelParser.MethodCallContext;
import au.csiro.data61.aap.parser.XbelParser.MethodParameterContext;
import au.csiro.data61.aap.spec.Method;
import au.csiro.data61.aap.spec.types.SolidityType;

/**
 * MethodCallAnalyzer
 */
class MethodCallAnalyzer extends SemanticAnalyzer {
    private final VariableAnalyzer variableAnalyzer;
    private final HashMap<String, Method> calledMethods;

    public MethodCallAnalyzer(ErrorCollector errorCollector, VariableAnalyzer variableAnalyzer) {
        super(errorCollector);
        assert variableAnalyzer != null;
        this.variableAnalyzer = variableAnalyzer;
        this.calledMethods = new HashMap<>();
    }

    @Override
    public void clear() {
        this.calledMethods.clear();
    }

    public Method getCalledMethod(MethodCallContext ctx) {
        assert ctx != null;
        return this.calledMethods.get(AnalyzerUtils.tokenPositionString(ctx.start));
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
        final Method method = 
            Library.INSTANCE.methodStream(ctx.methodName.getText())
                .filter(m -> this.areParametersMatching(m, ctx))
                .findFirst().orElse(null);
        
        if (method != null) {
            this.calledMethods.put(AnalyzerUtils.tokenPositionString(ctx.start), method);
        }    

        return method != null;   
    }

    private boolean areParametersMatching(Method method, MethodCallContext ctx) {
        if (method.getSignature().parameterTypeCount() != ctx.methodParameter().size()) {
            return false;
        }

        return IntStream.range(0, ctx.methodParameter().size())
            .allMatch(i -> this.areTypesMatching(method.getSignature().getParameterType(i), ctx.methodParameter(i)));
    }

    
    private boolean areTypesMatching(SolidityType varType, MethodParameterContext ctx) {
        if (ctx.variableReference() != null) {
            final SolidityType type = this.variableAnalyzer.getVariableType(ctx.variableReference().variableName().getText());
            return type != null && varType.conceptuallyEquals(type);
        }
        else if (ctx.literal() != null) {
            return AnalyzerUtils.isTypeCompatible(varType, ctx.literal());
        }
        else {
            throw new UnsupportedOperationException(String.format("This way of defining a parameter for a method call is not supported: '%s'.", ctx.getText()));
        }
    } 

    // TODO: this will be the place for emit and if conditions, as they will be mapped to 
}