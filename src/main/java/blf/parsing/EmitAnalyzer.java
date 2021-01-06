package blf.parsing;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import blf.core.writers.XesWriter;
import blf.grammar.BcqlParser;
import blf.util.TypeUtils;
import io.reactivex.annotations.NonNull;
import org.antlr.v4.runtime.ParserRuleContext;

/**
 * EmitAnalyzer
 */
public class EmitAnalyzer extends SemanticAnalyzer {
    private final VariableExistenceListener varAnalyzer;

    public EmitAnalyzer(final ErrorCollector errorCollector, @NonNull final VariableExistenceListener varAnalyzer) {
        super(errorCollector);
        this.varAnalyzer = varAnalyzer;
    }

    @Override
    public void clear() {}

    @Override
    public void exitEmitStatementCsv(final BcqlParser.EmitStatementCsvContext ctx) {
        this.verifyTableName(ctx.valueExpression());

        this.verifyUniquenessOfNames(
            ctx.namedEmitVariable(),
            varCtx -> varCtx.variableName() != null ? varCtx.variableName() : varCtx.valueExpression().variableName()
        );
    }

    private void verifyTableName(BcqlParser.ValueExpressionContext valueExpression) {
        String type = InterpreterUtils.determineType(valueExpression, this.varAnalyzer);
        if (type != null && !TypeUtils.isStringType(type)) {
            this.addError(valueExpression.start, "CSV table name must be a string.");
        }
    }

    @Override
    public void exitEmitStatementXesEvent(BcqlParser.EmitStatementXesEventContext ctx) {
        this.verifyUniquenessOfNames(
            ctx.xesEmitVariable(),
            varCtx -> varCtx.variableName() != null ? varCtx.variableName() : varCtx.valueExpression().variableName()
        );
        this.verifyXesTypeCompatibility(ctx.xesEmitVariable());
        this.verifyXesId(ctx.pid);
        this.verifyXesId(ctx.piid);
        this.verifyXesId(ctx.eid);
    }

    @Override
    public void exitEmitStatementXesTrace(BcqlParser.EmitStatementXesTraceContext ctx) {
        this.verifyUniquenessOfNames(
            ctx.xesEmitVariable(),
            varCtx -> varCtx.variableName() != null ? varCtx.variableName() : varCtx.valueExpression().variableName()
        );
        this.verifyXesTypeCompatibility(ctx.xesEmitVariable());
        this.verifyXesId(ctx.pid);
        this.verifyXesId(ctx.piid);
    }

    private void verifyXesId(BcqlParser.ValueExpressionContext ctx) {
        if (ctx != null) {
            final String type = InterpreterUtils.determineType(ctx, this.varAnalyzer);
            if (type != null && !XesWriter.areTypesCompatible(type, XesWriter.STRING_TYPE)) {
                this.addError(ctx.start, "An XES id must be a string value.");
            }
        }
    }

    private <T extends ParserRuleContext> void verifyUniquenessOfNames(
        final List<T> variables,
        final Function<T, BcqlParser.VariableNameContext> nameAccessor
    ) {
        final Set<String> names = new HashSet<>();
        for (final T varCtx : variables) {
            final BcqlParser.VariableNameContext nameCtx = nameAccessor.apply(varCtx);
            if (nameCtx == null) {
                this.addError(varCtx.start, "Attribute name must be specified for literals");
            }

            if (nameCtx != null) {
                this.verifyUniquenessOfName(nameCtx, names);
                names.add(nameCtx.getText());
            }
        }
    }

    private void verifyUniquenessOfName(final BcqlParser.VariableNameContext ctx, final Set<String> varNames) {
        if (varNames.contains(ctx.getText())) {
            this.addError(ctx.start, "Column name already specified.");
        }
    }

    private void verifyXesTypeCompatibility(List<BcqlParser.XesEmitVariableContext> variables) {
        variables.forEach(this::verifyXesTypeCompatibility);
    }

    private void verifyXesTypeCompatibility(BcqlParser.XesEmitVariableContext ctx) {
        if (ctx.xesTypes() != null) {
            final String solType = InterpreterUtils.determineType(ctx.valueExpression(), varAnalyzer);
            if (solType == null) {
                return;
            }

            final String xesType = ctx.xesTypes().getText();
            if (!XesWriter.areTypesCompatible(solType, xesType)) {
                this.addError(
                    ctx.valueExpression().start,
                    String.format("Cannot export solidity type '%s' as xes type '%s'.", solType, xesType)
                );
            }
        }
    }
}
