package au.csiro.data61.aap.elf.parsing;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Stream;

import org.antlr.v4.runtime.Token;

import au.csiro.data61.aap.elf.core.values.EthereumVariables;
import au.csiro.data61.aap.elf.parsing.EthqlParser.BlockFilterContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.LogEntryFilterContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.ScopeContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.TransactionFilterContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.VariableAssignmentStatementContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.VariableDeclarationStatementContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.VariableNameContext;

/**
 * VariableCollector
 */
public class VariableExistenceAnalyzer extends SemanticAnalyzer {
    private final Stack<List<Variable>> visibleVariables;

    public VariableExistenceAnalyzer() {
        this(new ErrorCollector());
    }

    public VariableExistenceAnalyzer(ErrorCollector errorCollector) {
        super(errorCollector);

        this.visibleVariables = new Stack<>();
    }

    @Override
    public void clear() {
        this.visibleVariables.clear();
    }

    private void addConstant(String type, String name) {
        this.visibleVariables.peek().add(new Variable(type, name, true));
    }

    private void addVariable(String type, String name) {
        this.visibleVariables.peek().add(new Variable(type, name, false));
    }

    private boolean isFilterConstant(String name) {
        return this.variableStream(name).map(variable -> variable.isConstant()).findFirst().orElse(false);
    }

    public boolean isVariableDefined(String name) {
        return this.variableStream().anyMatch(variable -> variable.getName().equals(name));
    }

    public String getVariableType(String name) {
        return this.variableStream(name).map(variable -> variable.getType()).findFirst().orElse(null);
    }

    private Stream<Variable> variableStream(String name) {
        return this.variableStream().filter(variable -> variable.getName().equals(name));
    }

    private Stream<Variable> variableStream() {
        return this.visibleVariables.stream().flatMap(varList -> varList.stream());
    }



    // #region variable existence

    @Override
    public void enterVariableName(VariableNameContext ctx) {
        if (ctx.parent instanceof VariableDeclarationStatementContext) {
            this.verifyVariableDeclaration((VariableDeclarationStatementContext) ctx.parent);
        } else if (ctx.parent instanceof VariableAssignmentStatementContext) {
            this.verifyVariableAssignment((VariableAssignmentStatementContext) ctx.parent);
        } else {
            this.verifyVariableReference(ctx);
        }
    }

    private void verifyVariableDeclaration(VariableDeclarationStatementContext ctx) {
        final String variableName = ctx.variableName().getText();
        if (this.isVariableDefined(variableName)) {
            this.addError(ctx.start, String.format("Variable '%s' is already defined."));
            return;
        }

        final String solType = ctx.solType().getText();
        this.addVariable(solType, variableName);
    }

    private void verifyVariableAssignment(VariableAssignmentStatementContext ctx) {
        final String variableName = ctx.variableName().getText();

        if (!this.verifyVariableReference(ctx.start, variableName)) {
            return;
        }

        if (this.isFilterConstant(variableName)) {
            this.addError(ctx.start, String.format("Variable '%s' not defined."));
        }
    }

    private void verifyVariableReference(VariableNameContext ctx) {
        final String variableName = ctx.getText();
        this.verifyVariableReference(ctx.start, variableName);
    }

    private boolean verifyVariableReference(Token token, String variableName) {
        if (!this.isVariableDefined(variableName)) {
            this.addError(token, String.format("Variable '%s' not defined."));
            return false;
        }
        return true;
    }

    // #endregion variable existence



    // #region scope variables

    @Override
    public void enterScope(ScopeContext ctx) {
        this.visibleVariables.push(new LinkedList<>());
    }

    @Override
    public void exitScope(ScopeContext ctx) {
        this.visibleVariables.pop();
    }

    @Override
    public void enterBlockFilter(BlockFilterContext ctx) {
        this.addFilterConstants(EthereumVariables.getBlockVariableNamesAndTypes());
    }

    @Override
    public void enterTransactionFilter(TransactionFilterContext ctx) {
        this.addFilterConstants(EthereumVariables.getTransactionVariableNamesAndTypes());
    }

    @Override
    public void enterLogEntryFilter(LogEntryFilterContext ctx) {
        this.addFilterConstants(EthereumVariables.getLogEntryVariableNamesAndTypes());
    }

    private void addFilterConstants(Map<String, String> nameTypeMap) {
        nameTypeMap.entrySet().stream()
            .forEach(entry -> this.addConstant(entry.getKey(), entry.getValue()));
    }

    // #endregion scope variables



    private static class Variable {
        final String name;
        final String type;
        final boolean isConstant;

        public Variable(String type, String name, boolean isConstant) {
            this.name = name;
            this.type = type;
            this.isConstant = isConstant;
        }

        public String getName() {
            return this.name;
        }

        public String getType() {
            return this.type;
        }

        public boolean isConstant() {
            return this.isConstant;
        }
    }
}