package blf.parsing;

import java.util.*;
import java.util.stream.Stream;

import blf.blockchains.ethereum.variables.EthereumVariables;
import org.antlr.v4.runtime.Token;

import blf.grammar.BcqlParser.*;

/**
 * VariableCollector
 */
public class VariableExistenceListener extends SemanticAnalyzer {
    private final Stack<List<Variable>> visibleVariables;

    public VariableExistenceListener() {
        this(new ErrorCollector());
    }

    public VariableExistenceListener(ErrorCollector errorCollector) {
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
        return this.variableStream(name).map(Variable::isConstant).findFirst().orElse(false);
    }

    public boolean isVariableDefined(String name) {
        return this.variableStream().anyMatch(variable -> variable.getName().equals(name));
    }

    public String getVariableType(String name) {
        return this.variableStream(name).map(Variable::getType).findFirst().orElse(null);
    }

    private Stream<Variable> variableStream(String name) {
        return this.variableStream().filter(variable -> variable.getName().equals(name));
    }

    private Stream<Variable> variableStream() {
        return this.visibleVariables.stream().flatMap(Collection::stream);
    }

    // #region variable existence

    @Override
    public void enterVariableName(VariableNameContext ctx) {
        if (ctx.parent instanceof VariableDeclarationStatementContext) {
            this.verifyVariableDeclaration((VariableDeclarationStatementContext) ctx.parent);
        } else if (ctx.parent instanceof VariableAssignmentStatementContext) {
            this.verifyVariableAssignment((VariableAssignmentStatementContext) ctx.parent);
        } else if (ctx.parent instanceof LogEntryParameterContext) {
            this.verifyLogEntryParameter((LogEntryParameterContext) ctx.parent);
        } else if (ctx.parent instanceof NamedEmitVariableContext || ctx.parent instanceof XesEmitVariableContext) {
            return;
        } else if (ctx.parent instanceof SmartContractParameterContext) {
            this.verifySmartContractParameter((SmartContractParameterContext) ctx.parent);
        } else if (ctx.parent instanceof SmartContractQueryParameterContext) {
            this.verifySmartContractQueryParameter((SmartContractQueryParameterContext) ctx.parent);
        } else {
            this.verifyVariableReference(ctx);
        }
    }

    private void verifyVariableDeclaration(VariableDeclarationStatementContext ctx) {
        this.verifyVariableDeclaration(ctx.variableName().start, ctx.solType().getText(), ctx.variableName().getText());
    }

    private void verifyLogEntryParameter(LogEntryParameterContext ctx) {
        this.verifyVariableDeclaration(ctx.variableName().start, ctx.solType().getText(), ctx.variableName().getText());
    }

    private void verifySmartContractParameter(SmartContractParameterContext ctx) {
        this.verifyVariableDeclaration(ctx.variableName().start, ctx.solType().getText(), ctx.variableName().getText());
    }

    private void verifyVariableDeclaration(Token token, String solType, String variableName) {
        if (this.isVariableDefined(variableName)) {
            this.addError(token, String.format("Variable '%s' is already defined.", variableName));
            return;
        }

        this.addVariable(solType, variableName);
    }

    private void verifyVariableAssignment(VariableAssignmentStatementContext ctx) {
        final String variableName = ctx.variableName().getText();

        if (!this.verifyVariableReference(ctx.start, variableName)) {
            return;
        }

        if (this.isFilterConstant(variableName)) {
            this.addError(ctx.start, String.format("Variable '%s' not defined.", variableName));
        }
    }

    private void verifyVariableReference(VariableNameContext ctx) {
        final String variableName = ctx.getText();
        this.verifyVariableReference(ctx.start, variableName);
    }

    private void verifySmartContractQueryParameter(SmartContractQueryParameterContext parent) {
        this.verifyVariableReference(parent.variableName().start, parent.variableName().getText());
    }

    private boolean verifyVariableReference(Token token, String variableName) {
        if (!this.isVariableDefined(variableName)) {
            this.addError(token, String.format("Variable '%s' not defined.", variableName));
            return false;
        }
        return true;
    }

    // #endregion variable existence

    // #region scope variables

    @Override
    public void enterDocument(DocumentContext ctx) {
        this.addEmptyVariableList();
    }

    @Override
    public void exitDocument(DocumentContext ctx) {
        this.removeVariableList();
    }

    @Override
    public void enterScope(ScopeContext ctx) {
        this.addEmptyVariableList();
    }

    @Override
    public void exitScope(ScopeContext ctx) {
        this.removeVariableList();
    }

    private void addEmptyVariableList() {
        this.visibleVariables.push(new LinkedList<>());
    }

    private void removeVariableList() {
        this.visibleVariables.pop();
    }

    @Override
    public void enterBlockFilter(BlockFilterContext ctx) {
        this.addFilterConstants(EthereumVariables.getTransactionVariableNamesAndTypes());
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
        nameTypeMap.entrySet().stream().forEach(entry -> {
            if (!this.isVariableDefined(entry.getKey())) {
                this.addConstant(entry.getValue(), entry.getKey());
            }
        });
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
