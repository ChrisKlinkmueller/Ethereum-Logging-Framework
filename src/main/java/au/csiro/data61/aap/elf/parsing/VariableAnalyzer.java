package au.csiro.data61.aap.elf.parsing;

import java.util.Map;
import java.util.Stack;

/**
 * VariableCollector
 */
public class VariableAnalyzer extends SemanticAnalyzer {
    private final Stack<Map<String, String>> visibleVariables;
    
    public VariableAnalyzer() {
        this(new ErrorCollector());
    }

    public VariableAnalyzer(ErrorCollector errorCollector) {
        super(errorCollector);

        this.visibleVariables = new Stack<>();
    }

    @Override
    public void clear() {
        this.visibleVariables.clear();    
    }

    public boolean isVariableDefined(String name) {
        return this.visibleVariables.stream()
            .anyMatch(
                map -> map.keySet().stream()
                    .anyMatch(knownName -> knownName.equals(name))
            );
    }

    public String getVariableType(String name) {
        return this.visibleVariables.stream()
            .flatMap(map -> map.entrySet().stream())
            .filter(entry -> entry.getKey().equals(name))
            .map(entry -> entry.getValue())
            .findFirst().orElse(null);
    }



    //#region scope variables

    // @Override
    // public void enterBlockFilter(BlockFilterContext ctx) {
    //     this.addVariableSet(EthereumVariables.getBlockVariableNamesAndTypes());
    // }

    // @Override
    // public void enterTransactionFilter(TransactionFilterContext ctx) {
    //     this.addVariableSet(EthereumVariables.getTransactionVariableNamesAndTypes());
    // }

    // @Override
    // public void enterSmartContractsFilter(SmartContractsFilterContext ctx) {
    //     this.addVariableSet(new HashMap<>());
    // }

    // @Override
    // public void enterLogEntryFilter(LogEntryFilterContext ctx) {
    //     this.addVariableSet(EthereumVariables.getLogEntryVariableNamesAndTypes());
    // }

    // @Override
    // public void enterDocument(DocumentContext ctx) {
    //     this.addVariableSet(new HashMap<>());
    // }

    // private void addVariableSet(Map<String, String> variables) {
    //     this.visibleVariables.push(variables);    
    // }

    // @Override
    // public void exitScope(ScopeContext ctx) {
    //     this.visibleVariables.pop();
    // }

    // @Override
    // public void exitDocument(DocumentContext ctx) {
    //     this.visibleVariables.pop();
    // }

    //#endregion scope variables



    //#region defined variables

    // @Override
    // public void enterVariableDefinition(VariableDefinitionContext ctx) {
    //     this.verifyVariable(ctx.solType(), ctx.variableName(), true);
    // }

    // @Override
    // public void enterSmartContractVariable(SmartContractVariableContext ctx) {
    //     if (ctx.solType() != null || ctx.variableName() != null) {
    //         this.verifyVariable(ctx.solType(), ctx.variableName(), false);
    //     }
    // }

    // @Override
    // public void enterLogEntryParameter(LogEntryParameterContext ctx) {
    //     if (ctx.solType() != null || ctx.variableName() != null) {
    //         this.verifyVariable(ctx.solType(), ctx.variableName(), false);
    //     }
    // }


    // private void verifyVariable(SolTypeContext typeCtx, VariableNameContext nameCtx, boolean baseTypeOnly) {
    //     final String varName = (nameCtx.getText());
    //     if (this.isVariableDefined(varName)) {
    //         final String message = EthereumVariables.isEthereumVariable(varName) 
    //             ? String.format("The variable '%s' already exists as an implicit filter variable.", varName)
    //             : String.format("The variable '%s' already exists as an explicitly defined variable.", varName);
    //         this.addError(nameCtx.start, message);
    //         return;
    //     }

    //     this.visibleVariables.peek().put(varName, typeCtx.getText());
    // }
    
    //#endregion



    //#region referenced variables

    // @Override
    // public void enterVariableReference(VariableReferenceContext ctx) {
    //     final String varName = ctx.variableName().getText();
    //     if (!this.isVariableDefined(varName)) {
    //         this.addError(ctx.start, String.format("A variable with name '%' does not exist", varName));
    //     }
    // }

    //#endregion
}