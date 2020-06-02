package au.csiro.data61.aap.elf.parsing;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.antlr.v4.runtime.ParserRuleContext;

import au.csiro.data61.aap.elf.core.writers.XesWriter;
import au.csiro.data61.aap.elf.parsing.EthqlParser.EmitStatementCsvContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.EmitStatementXesEventContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.EmitStatementXesTraceContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.ValueExpressionContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.VariableNameContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.XesEmitVariableContext;
import au.csiro.data61.aap.elf.util.TypeUtils;

/**
 * EmitAnalyzer
 */
public class EmitAnalyzer extends SemanticAnalyzer {
    private final VariableExistenceAnalyzer varAnalyzer;

    public EmitAnalyzer(final ErrorCollector errorCollector,
            final VariableExistenceAnalyzer varAnalyzer) {
        super(errorCollector);
        assert varAnalyzer != null;
        this.varAnalyzer = varAnalyzer;
    }

    @Override
    public void clear() {}

    @Override
    public void exitEmitStatementCsv(final EmitStatementCsvContext ctx) {
        this.verifyTableName(ctx.valueExpression());

        this.verifyUniquenessOfNames(ctx.namedEmitVariable(),
                varCtx -> varCtx.variableName() != null ? varCtx.variableName()
                        : varCtx.valueExpression().variableName());
    }

    private void verifyTableName(ValueExpressionContext valueExpression) {
        String type = InterpreterUtils.determineType(valueExpression, this.varAnalyzer);
        if (type != null && !TypeUtils.isStringType(type)) {
            this.addError(valueExpression.start, "CSV table name must be a string.");
        }
    }

    @Override
    public void exitEmitStatementXesEvent(EmitStatementXesEventContext ctx) {
        this.verifyUniquenessOfNames(ctx.xesEmitVariable(),
                varCtx -> varCtx.variableName() != null ? varCtx.variableName()
                        : varCtx.valueExpression().variableName());
        this.verifyXesTypeCompatibility(ctx.xesEmitVariable());
        this.verifyXesId(ctx.pid);
        this.verifyXesId(ctx.piid);
        this.verifyXesId(ctx.eid);
    }

    @Override
    public void exitEmitStatementXesTrace(EmitStatementXesTraceContext ctx) {
        this.verifyUniquenessOfNames(ctx.xesEmitVariable(),
                varCtx -> varCtx.variableName() != null ? varCtx.variableName()
                        : varCtx.valueExpression().variableName());
        this.verifyXesTypeCompatibility(ctx.xesEmitVariable());
        this.verifyXesId(ctx.pid);
        this.verifyXesId(ctx.piid);
    }

    private void verifyXesId(ValueExpressionContext ctx) {
        if (ctx != null) {
            final String type = InterpreterUtils.determineType(ctx, this.varAnalyzer);
            if (type != null && !XesWriter.areTypesCompatible(type, XesWriter.STRING_TYPE)) {
                this.addError(ctx.start, "An XES id must be a string value.");
            }
        }
    }

    private <T extends ParserRuleContext> void verifyUniquenessOfNames(final List<T> variables,
            final Function<T, VariableNameContext> nameAccessor) {
        final Set<String> names = new HashSet<>();
        for (final T varCtx : variables) {
            final VariableNameContext nameCtx = nameAccessor.apply(varCtx);
            if (nameCtx == null) {
                this.addError(varCtx.start, "Attribute name must be specified for literals");
            }

            if (nameCtx != null) {
                this.verifyUniquenessOfName(nameCtx, names);
                names.add(nameCtx.getText());
            }
        }
    }

    private void verifyUniquenessOfName(final VariableNameContext ctx, final Set<String> varNames) {
        if (varNames.contains(ctx.getText())) {
            this.addError(ctx.start, String.format("Column name already specified."));
        }
    }

    private void verifyXesTypeCompatibility(List<XesEmitVariableContext> variables) {
        variables.forEach(ctx -> this.verifyXesTypeCompatibility(ctx));
    }

    private void verifyXesTypeCompatibility(XesEmitVariableContext ctx) {
        if (ctx.xesTypes() != null) {
            final String solType =
                    InterpreterUtils.determineType(ctx.valueExpression(), varAnalyzer);
            if (solType == null) {
                this.addError(ctx.valueExpression().start, "Cannot infer type.");
            }

            final String xesType = ctx.xesTypes().getText();
            if (!XesWriter.areTypesCompatible(solType, xesType)) {
                this.addError(ctx.valueExpression().start, String.format(
                        "Cannot export solidity type '%s' as xes type '%s'.", solType, xesType));
            }
        }
    }
}
