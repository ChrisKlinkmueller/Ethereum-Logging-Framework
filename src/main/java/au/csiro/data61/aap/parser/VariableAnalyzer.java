package au.csiro.data61.aap.parser;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Stream;

import au.csiro.data61.aap.parser.XbelParser.BlockFilterContext;
import au.csiro.data61.aap.parser.XbelParser.DocumentContext;
import au.csiro.data61.aap.parser.XbelParser.LogEntryFilterContext;
import au.csiro.data61.aap.parser.XbelParser.LogEntryParameterContext;
import au.csiro.data61.aap.parser.XbelParser.ScopeContext;
import au.csiro.data61.aap.parser.XbelParser.SmartContractVariableContext;
import au.csiro.data61.aap.parser.XbelParser.SmartContractsFilterContext;
import au.csiro.data61.aap.parser.XbelParser.SolTypeContext;
import au.csiro.data61.aap.parser.XbelParser.TransactionFilterContext;
import au.csiro.data61.aap.parser.XbelParser.VariableDefinitionContext;
import au.csiro.data61.aap.parser.XbelParser.VariableNameContext;
import au.csiro.data61.aap.parser.XbelParser.VariableReferenceContext;
import au.csiro.data61.aap.program.BlockScope;
import au.csiro.data61.aap.program.GlobalScope;
import au.csiro.data61.aap.program.LogEntryScope;
import au.csiro.data61.aap.program.SmartContractScope;
import au.csiro.data61.aap.program.TransactionScope;
import au.csiro.data61.aap.program.suppliers.BlockchainVariable;
import au.csiro.data61.aap.program.suppliers.UserVariable;
import au.csiro.data61.aap.program.suppliers.Variable;
import au.csiro.data61.aap.program.types.SolidityType;

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
        this.addVariableSet(BlockScope.DEFAULT_VARIABLES);
    }

    @Override
    public void enterTransactionFilter(TransactionFilterContext ctx) {
        this.addVariableSet(TransactionScope.DEFAULT_VARIABLES);
    }

    @Override
    public void enterSmartContractsFilter(SmartContractsFilterContext ctx) {
        this.addVariableSet(SmartContractScope.DEFAULT_VARIABLES);
    }

    @Override
    public void enterLogEntryFilter(LogEntryFilterContext ctx) {
        this.addVariableSet(LogEntryScope.DEFAULT_VARIABLES);
    }

    @Override
    public void enterDocument(DocumentContext ctx) {
        this.addVariableSet(GlobalScope.DEFAULT_VARIABLES);
    }

    private void addVariableSet(Set<Variable> variables) {
        this.visibleVariables.push(new HashSet<>(variables));    
    }

    @Override
    public void exitScope(ScopeContext ctx) {
        this.visibleVariables.pop();
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



    //#region defined variables

    @Override
    public void enterVariableDefinition(VariableDefinitionContext ctx) {
        this.verifyVariable(ctx.solType(), ctx.variableName(), true);
    }

    @Override
    public void enterSmartContractVariable(SmartContractVariableContext ctx) {
        if (ctx.solType() != null || ctx.variableName() != null) {
            this.verifyVariable(ctx.solType(), ctx.variableName(), false);
        }
    }

    @Override
    public void enterLogEntryParameter(LogEntryParameterContext ctx) {
        if (ctx.solType() != null || ctx.variableName() != null) {
            this.verifyVariable(ctx.solType(), ctx.variableName(), false);
        }
    }


    private void verifyVariable(SolTypeContext typeCtx, VariableNameContext nameCtx, boolean baseTypeOnly) {
        final Variable variable = this.getVariable(nameCtx.getText());
        if (variable != null) {
            final String message = variable instanceof BlockchainVariable 
                ? String.format("The variable '%s' already exists as an implicit scope variable.", variable.getName())
                : String.format("The variable '%s' already exists as an explicitly defined variable.", variable.getName());
            this.addError(nameCtx.start, message);
            return;
        }

        final SolidityType type = AnalyzerUtils.verifySolidityType(typeCtx, this.errorCollector, baseTypeOnly);
        if (type == null) {
            return;
        }

        final Variable newVariable = new UserVariable(type, nameCtx.getText());
        this.visibleVariables.peek().add(newVariable);
    }
    
    //#endregion



    //#region referenced variables

    @Override
    public void enterVariableReference(VariableReferenceContext ctx) {
        if (this.getVariable(ctx.variableName().getText()) == null) {
            this.addError(ctx.start, String.format("A variable with name '%' does not exist", ctx.variableName().getText()));
        }
    }

    //#endregion


    public boolean containsVariable(String name) {
        return this.variableStream()
            .anyMatch(var -> var.getName().equals(name));
    }

    public Variable getVariable(String name) {
        return this.variableStream()
            .filter(variable -> variable.hasName(name))
            .findFirst()
            .orElse(null);
    }

    public SolidityType getVariableType(String name) {
        final Optional<Variable> searchResult = this.variableStream()
            .filter(variable -> variable.getName().equals(name))
            .findFirst();
        return searchResult.isPresent() ? searchResult.get().getType() : null;
    }

    public boolean existsVariable(String name, SolidityType type) {
        final Variable variable = this.getVariable(name);
        return variable != null && variable.getType().conceptuallyEquals(type);
    }

    private Stream<Variable> variableStream() {
        return this.visibleVariables.stream()
            .flatMap(set -> set.stream());
    }
}