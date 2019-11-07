package au.csiro.data61.aap.parser;

import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import au.csiro.data61.aap.library.DefaultVariables;
import au.csiro.data61.aap.parser.XbelParser.BlockFilterContext;
import au.csiro.data61.aap.parser.XbelParser.DocumentContext;
import au.csiro.data61.aap.parser.XbelParser.LogEntryFilterContext;
import au.csiro.data61.aap.parser.XbelParser.ScopeContext;
import au.csiro.data61.aap.parser.XbelParser.SmartContractsFilterContext;
import au.csiro.data61.aap.parser.XbelParser.TransactionFilterContext;
import au.csiro.data61.aap.parser.XbelParser.VariableDefinitionContext;
import au.csiro.data61.aap.spec.Variable;
import au.csiro.data61.aap.spec.VariableCategory;
import au.csiro.data61.aap.spec.types.SolidityType;

/**
 * VariableCollector
 */
public class VariableAnalyzer extends SemanticAnalyzer {
    private final Stack<Set<Variable>> visibleVariables;
    
    public VariableAnalyzer(ErrorCollector errorCollector) {
        super(errorCollector);

        this.visibleVariables = new Stack<>();
    }



    //#region scope variables

    @Override
    public void enterBlockFilter(BlockFilterContext ctx) {
        this.addVariableSet(DefaultVariables.defaultBlockVariableStream());
    }

    @Override
    public void enterTransactionFilter(TransactionFilterContext ctx) {
        this.addVariableSet(DefaultVariables.defaultTransactionVariableStream());
    }

    @Override
    public void enterSmartContractsFilter(SmartContractsFilterContext ctx) {
        this.addVariableSet(DefaultVariables.defaultSmartContractVariableStream());
    }

    @Override
    public void enterLogEntryFilter(LogEntryFilterContext ctx) {
        this.addVariableSet(DefaultVariables.defaultLogEntryVariableStream());
    }

    @Override
    public void exitScope(ScopeContext ctx) {
        this.visibleVariables.pop();
    }

    @Override
    public void enterDocument(DocumentContext ctx) {
        this.addVariableSet(DefaultVariables.defaultGlobalVariableStream());
    }

    private void addVariableSet(Stream<Variable> variableStream) {
        this.visibleVariables.push(variableStream.collect(Collectors.toSet()));    
    }

    @Override
    public void exitDocument(DocumentContext ctx) {
        this.visibleVariables.pop();
    }

    @Override
    public void clear() {
        this.visibleVariables.clear();    
    }

    //#endregion scope variables



    //#region statement variables

    @Override
    public void enterVariableDefinition(VariableDefinitionContext ctx) {
        final Variable lookupResult = this.getVariable(ctx.variableName().getText());
        if (lookupResult != null) {
            final String message = lookupResult.getCategory() == VariableCategory.SCOPE_VARIABLE 
                ? String.format("The variable '%s' already exists as an implicit scope variable.", lookupResult.getName())
                : String.format("The variable '%s' already exists as an explicitly defined variable.", lookupResult.getName());
            this.errorCollector.addSemanticError(ctx.variableName().start, message);
            return;
        }

        final SolidityType type = AnalyzerUtils.verifySolidityType(ctx.solType(), this.errorCollector);
        if (type == null) {
            return;
        }

        final Variable variable = new Variable(type, ctx.variableName().getText());
        this.visibleVariables.peek().add(variable);
    }

    //#endregion statement variables

    public boolean containsVariable(String name) {
        return this.variableStream()
            .anyMatch(var -> var.getName().equals(name));
    }

    private Variable getVariable(String name) {
        return this.variableStream()
            .filter(variable -> variable.getName().equals(name))
            .findFirst()
            .orElse(null);
    }

    public SolidityType getVariableType(String name) {
        final Optional<Variable> searchResult = this.variableStream()
            .filter(variable -> variable.getName().equals(name))
            .findFirst();
        return searchResult.isPresent() ? searchResult.get().getType() : null;
    }

    private Stream<Variable> variableStream() {
        return this.visibleVariables.stream()
            .flatMap(set -> set.stream());
    }
    
}