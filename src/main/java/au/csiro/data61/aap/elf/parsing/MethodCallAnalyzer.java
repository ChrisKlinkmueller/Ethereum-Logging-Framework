package au.csiro.data61.aap.elf.parsing;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import au.csiro.data61.aap.elf.library.Library;
import au.csiro.data61.aap.elf.library.MethodSignature;
import au.csiro.data61.aap.elf.parsing.EthqlParser.MethodCallContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.MethodParameterContext;

/**
 * MethodCallAnalyzer
 */
class MethodCallAnalyzer extends SemanticAnalyzer {
    private final VariableAnalyzer variableAnalyzer;
    private final HashMap<String, MethodSignature> calledMethods;

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

    public String getCalledMethod(MethodCallContext ctx) {
        assert ctx != null;
        final MethodSignature signature = this.calledMethods.get(AnalyzerUtils.tokenPositionString(ctx.start));
        return signature == null ? null : signature.getReturnType();
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
        final MethodSignature signature = this.getSignature(ctx);
        
        if (signature != null) {
            this.calledMethods.put(AnalyzerUtils.tokenPositionString(ctx.start), signature);
        }    

        return signature != null;   
    }

    private MethodSignature getSignature(MethodCallContext ctx) {
        final List<String> parameterTypes = ctx.methodParameter().stream()
            .map(param -> this.getParameterType(param))
            .collect(Collectors.toList());
        return Library.INSTANCE.getRegisteredSignatures(ctx.methodName.getText(), parameterTypes);
    }
    
    private String getParameterType(MethodParameterContext ctx) {
        if (ctx.variableReference() != null) {
            return this.variableAnalyzer.getVariableType(ctx.variableReference().variableName().getText());
        }
        else if (ctx.literal() != null) {
            return AnalyzerUtils.getType(ctx.literal());
        }
        else {
            throw new UnsupportedOperationException(String.format("This way of defining a parameter for a method call is not supported: '%s'.", ctx.getText()));
        }
    }

	public String getCalledMethodType(MethodCallContext methodCall) {
		return null;
	} 

    // TODO: this will be the place for emit and if conditions, as they will be mapped to 
}