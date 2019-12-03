package au.csiro.data61.aap.elf.configuration;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.ParserRuleContext;

import au.csiro.data61.aap.elf.core.filters.Program;
import au.csiro.data61.aap.elf.parsing.EthqlBaseListener;
import au.csiro.data61.aap.elf.parsing.VariableExistenceAnalyzer;
import au.csiro.data61.aap.elf.parsing.EthqlParser.AddressListContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.BlockFilterContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.BlockNumberContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.ConditionalAndExpressionContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.ConditionalComparisonExpressionContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.ConditionalNotExpressionContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.ConditionalOrExpressionContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.ConditionalPrimaryExpressionContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.DocumentContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.EmitStatementCsvContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.EmitStatementLogContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.EmitStatementXesEventContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.EmitStatementXesTraceContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.GenericFilterContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.LiteralContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.LogEntryFilterContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.LogEntryParameterContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.LogEntrySignatureContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.NamedEmitVariableContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.TransactionFilterContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.ValueExpressionContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.XesEmitVariableContext;
import au.csiro.data61.aap.elf.util.TypeUtils;

/**
 * EthqlProgramBuilder
 */
public class EthqlProgramBuilder extends EthqlBaseListener {
    private final SpecificationComposer composer;
    private final VariableExistenceAnalyzer analyzer;
    private final Stack<Object> genericFilterPredicates;
    private BuildException error;
    private Program program;

    public EthqlProgramBuilder(VariableExistenceAnalyzer analyzer) {
        this.composer = new SpecificationComposer();
        this.analyzer = analyzer;
        this.genericFilterPredicates = new Stack<Object>();
    }

    public boolean containsError() {
        return this.error != null;
    }

    public BuildException getError() {
        return this.error;
    }

    public Program getProgram() {
        return this.program;
    }

    @Override
    public void enterDocument(DocumentContext ctx) {
        this.handleEthqlElement(ctx, this::prepareProgramBuild);
    }

    private void prepareProgramBuild(DocumentContext ctx) throws BuildException {
        this.error = null;
        this.composer.prepareProgramBuild();
    }

    @Override
    public void exitDocument(DocumentContext ctx) {
        this.handleEthqlElement(ctx, this::buildProgram);
    }

    private void buildProgram(DocumentContext ctx) throws BuildException {
        this.program = this.composer.buildProgram();
    }

    @Override
    public void enterBlockFilter(BlockFilterContext ctx) {
        this.handleEthqlElement(ctx, this::prepareBlockFilterBuild);
    }

    private void prepareBlockFilterBuild(BlockFilterContext ctx) throws BuildException {
        this.composer.prepareBlockRangeBuild();
    }

    @Override
    public void exitBlockFilter(BlockFilterContext ctx) {
        this.handleEthqlElement(ctx, this::buildBlockFilter);
    }

    private void buildBlockFilter(BlockFilterContext ctx) throws BuildException {
        BlockNumberSpecification from = this.getBlockNumberSpecification(ctx.from);
        BlockNumberSpecification to = this.getBlockNumberSpecification(ctx.to);
        this.composer.buildBlockRange(from, to);
    }

    private BlockNumberSpecification getBlockNumberSpecification(BlockNumberContext ctx) throws BuildException {
        if (ctx.valueExpression() != null) {
            ValueAccessorSpecification number = this.getValueAccessor(ctx.valueExpression());
            return BlockNumberSpecification.ofBlockNumber(number);
        } else if (ctx.KEY_CURRENT() != null) {
            return BlockNumberSpecification.ofCurrent();
        } else if (ctx.KEY_EARLIEST() != null) {
            return BlockNumberSpecification.ofEarliest();
        } else if (ctx.KEY_CONTINUOUS() != null) {
            return BlockNumberSpecification.ofContinuous();
        } else {
            throw new BuildException("Unsupported variable declaration.");
        }
    }

    @Override
    public void enterTransactionFilter(TransactionFilterContext ctx) {
        this.handleEthqlElement(ctx, this::prepareTransactionFilterBuild);
    }

    private void prepareTransactionFilterBuild(TransactionFilterContext ctx) throws BuildException {
        this.composer.prepareTransactionFilterBuild();
    }

    @Override
    public void exitTransactionFilter(TransactionFilterContext ctx) {
        this.handleEthqlElement(ctx, this::buildTransactionFilter);
    }

    private void buildTransactionFilter(TransactionFilterContext ctx) throws BuildException {
        final AddressListSpecification senders = this.getAddressListSpecification(ctx.senders);
        final AddressListSpecification recipients = this.getAddressListSpecification(ctx.recipients);
        this.composer.buildTransactionFilter(senders, recipients);
    }

    @Override
    public void enterLogEntryFilter(LogEntryFilterContext ctx) {
        this.handleEthqlElement(ctx, this::prepareLogEntryFilterBuild);
    }

    private void prepareLogEntryFilterBuild(LogEntryFilterContext ctx) throws BuildException {
        this.composer.prepareLogEntryFilterBuild();
    }

    @Override
    public void exitLogEntryFilter(LogEntryFilterContext ctx) {
        this.handleEthqlElement(ctx, this::buildLogEntryFilter);
    }

    private void buildLogEntryFilter(LogEntryFilterContext ctx) throws BuildException {
        final AddressListSpecification contracts = this.getAddressListSpecification(ctx.addressList());
        final LogEntrySignatureSpecification signature = this.getLogEntrySignature(ctx.logEntrySignature());
        this.composer.buildLogEntryFilter(contracts, signature);
    }

    private AddressListSpecification getAddressListSpecification(AddressListContext ctx) throws BuildException {
        if (ctx.BYTES_LITERAL() != null) {
            return AddressListSpecification.ofAddresses(
                    ctx.BYTES_LITERAL().stream().map(literal -> literal.getText()).collect(Collectors.toList()));
        } else if (ctx.KEY_ANY() != null) {
            return AddressListSpecification.ofAny();
        } else if (ctx.variableName() != null) {
            return AddressListSpecification.ofAddress(ctx.variableName().getText());
        } else {
            return AddressListSpecification.ofEmpty();
        }
    }

    private LogEntrySignatureSpecification getLogEntrySignature(LogEntrySignatureContext ctx) throws BuildException {
        final LinkedList<LogEntryParameterSpecification> parameters = new LinkedList<>();
        for (LogEntryParameterContext paramCtx : ctx.logEntryParameter()) {
            parameters.add(LogEntryParameterSpecification.of(paramCtx.variableName().getText(),
                    paramCtx.solType().getText(), paramCtx.KEY_INDEXED() != null));
        }

        return LogEntrySignatureSpecification.of(ctx.methodName.getText(), parameters);
    }

    @Override
    public void enterGenericFilter(GenericFilterContext ctx) {
        this.handleEthqlElement(ctx, this::prepareGenericFilterBuild);
    }

    private void prepareGenericFilterBuild(GenericFilterContext ctx) throws BuildException {
        this.composer.prepareGenericFilterBuild();
    }

    @Override
    public void exitGenericFilter(GenericFilterContext ctx) {
        this.handleEthqlElement(ctx, this::buildGenericFilter);
    }

    private void buildGenericFilter(GenericFilterContext ctx) throws BuildException {
        if (this.genericFilterPredicates.size() != 1
                || !(this.genericFilterPredicates.peek() instanceof GenericFilterPredicateSpecification)) {
            throw new BuildException("Error in boolean expression tree.");
        }

        this.composer.buildGenericFilter((GenericFilterPredicateSpecification) this.genericFilterPredicates.pop());
    }

    @Override
    public void exitConditionalOrExpression(ConditionalOrExpressionContext ctx) {
        this.handleEthqlElement(ctx, this::handleConditionalOrExpression);
    }

    private void handleConditionalOrExpression(ConditionalOrExpressionContext ctx) throws BuildException {
        this.createBinaryConditionalExpression(GenericFilterPredicateSpecification::or);
    }

    @Override
    public void exitConditionalAndExpression(ConditionalAndExpressionContext ctx) {
        this.handleEthqlElement(ctx, this::handleConditionalAndExpression);
    }

    private void handleConditionalAndExpression(ConditionalAndExpressionContext ctx) throws BuildException {
        this.createBinaryConditionalExpression(GenericFilterPredicateSpecification::and);
    }

    private void createBinaryConditionalExpression(
            BiFunction<GenericFilterPredicateSpecification, GenericFilterPredicateSpecification, GenericFilterPredicateSpecification> constructor)
            throws BuildException {
        if (this.genericFilterPredicates.isEmpty()
                || !(this.genericFilterPredicates.peek() instanceof GenericFilterPredicateSpecification)) {
            throw new BuildException("Parse tree error: binary boolean expression requires boolean predicates.");
        }
        final GenericFilterPredicateSpecification predicate1 = (GenericFilterPredicateSpecification) this.genericFilterPredicates
                .pop();

        if (this.genericFilterPredicates.isEmpty()
                || !(this.genericFilterPredicates.peek() instanceof GenericFilterPredicateSpecification)) {
            throw new BuildException("Parse tree error: binary boolean expression requires boolean predicates.");
        }
        final GenericFilterPredicateSpecification predicate2 = (GenericFilterPredicateSpecification) this.genericFilterPredicates
                .pop();

        this.genericFilterPredicates.push(constructor.apply(predicate1, predicate2));
    }

    @Override
    public void exitConditionalComparisonExpression(ConditionalComparisonExpressionContext ctx) {
        this.handleEthqlElement(ctx, this::handleConditionalComparisonExpression);
    }

    private void handleConditionalComparisonExpression(ConditionalComparisonExpressionContext ctx)
            throws BuildException {
        if (ctx.comparators() == null) {
            return;
        }

        if (this.genericFilterPredicates.size() < 2) {
            throw new BuildException("Parse tree does not contain enough expressions.");
        }

        final Object value2 = this.genericFilterPredicates.pop();
        if (!(value2 instanceof ValueAccessorSpecification)) {
            throw new BuildException("Can only compare values, but not boolean expressions.");
        }

        final Object value1 = this.genericFilterPredicates.pop();
        if (!(value2 instanceof ValueAccessorSpecification)) {
            throw new BuildException("Can only compare values, but not boolean expressions.");
        }

        final ValueAccessorSpecification spec1 = (ValueAccessorSpecification) value1;
        final ValueAccessorSpecification spec2 = (ValueAccessorSpecification) value2;

        GenericFilterPredicateSpecification predicate = null;
        switch (ctx.comparators().getText().toLowerCase()) {
        case "==":
            predicate = GenericFilterPredicateSpecification.equals(spec1, spec2);
            break;
        case "!=":
            predicate = GenericFilterPredicateSpecification.notEquals(spec1, spec2);
            break;
        case ">=":
            predicate = GenericFilterPredicateSpecification.greaterThanAndEquals(spec1, spec2);
            break;
        case ">":
            predicate = GenericFilterPredicateSpecification.greaterThan(spec1, spec2);
            break;
        case "<":
            predicate = GenericFilterPredicateSpecification.smallerThan(spec1, spec2);
            break;
        case "<=":
            predicate = GenericFilterPredicateSpecification.smallerThanAndEquals(spec1, spec2);
            break;
        case "in":
            predicate = GenericFilterPredicateSpecification.in(spec1, spec2);
            break;
        default:
            throw new BuildException(String.format("Comparator %s not supported.", ctx.comparators().getText()));
        }

        this.genericFilterPredicates.push(predicate);
    }

    @Override
    public void exitConditionalNotExpression(ConditionalNotExpressionContext ctx) {
        this.handleEthqlElement(ctx, this::handleConditionalNotExpression);
    }

    private void handleConditionalNotExpression(ConditionalNotExpressionContext ctx) throws BuildException {
        if (ctx.KEY_NOT() == null) {
            return;
        }

        Object valueExpression = this.genericFilterPredicates.pop();
        if (valueExpression instanceof ValueAccessorSpecification) {
            valueExpression = GenericFilterPredicateSpecification
                    .ofBooleanValue((ValueAccessorSpecification) valueExpression);
        }

        if (!(valueExpression instanceof GenericFilterPredicateSpecification)) {
            throw new BuildException(String.format("GenericFilterPredicateSpecification required, but was %s.",
                    valueExpression.getClass()));
        }
        this.genericFilterPredicates
                .push(GenericFilterPredicateSpecification.not((GenericFilterPredicateSpecification) valueExpression));
    }

    @Override
    public void exitConditionalPrimaryExpression(ConditionalPrimaryExpressionContext ctx) {
        this.handleEthqlElement(ctx, this::handleConitionalPrimaryExpression);
    }

    private void handleConitionalPrimaryExpression(ConditionalPrimaryExpressionContext ctx) throws BuildException {
        if (ctx.valueExpression() != null) {
            this.genericFilterPredicates.push(this.getValueAccessor(ctx.valueExpression()));
        }
    }

    @Override
    public void exitEmitStatementLog(EmitStatementLogContext ctx) {
        this.handleEthqlElement(ctx, this::handleEmitStatementLog);
    }

    private void handleEmitStatementLog(EmitStatementLogContext ctx) throws BuildException {
        final List<ValueAccessorSpecification> accessors = new LinkedList<>();
        for (ValueExpressionContext valEx : ctx.valueExpression()) {
            accessors.add(this.getValueAccessor(valEx));
        }
        final LogLineExportSpecification spec = LogLineExportSpecification.ofValues(accessors);
        this.composer.addInstruction(spec);
    }

    @Override
    public void exitEmitStatementCsv(EmitStatementCsvContext ctx) {
        this.handleEthqlElement(ctx, this::handleEmitStatementCsv);
    }

    private void handleEmitStatementCsv(EmitStatementCsvContext ctx) throws BuildException {
        LinkedList<CsvColumnSpecification> columns = new LinkedList<>();
        for (NamedEmitVariableContext varCtx : ctx.namedEmitVariable()) {
            final String name = varCtx.valueExpression().variableName() == null ? varCtx.variableName().getText()
                    : varCtx.valueExpression().variableName().getText();
            final ValueAccessorSpecification accessor = this.getValueAccessor(varCtx.valueExpression());
            columns.add(CsvColumnSpecification.of(name, accessor));
        }
        CsvExportSpecification.of(this.getValueAccessor(ctx.tableName), columns);
    }

    @Override
    public void exitEmitStatementXesTrace(EmitStatementXesTraceContext ctx) {
        this.handleEthqlElement(ctx, this::handleEmitStatementXesTrace);
    }

    private void handleEmitStatementXesTrace(EmitStatementXesTraceContext ctx) throws BuildException {
        final ValueAccessorSpecification pid = this.getXesId(ctx.pid);
        final ValueAccessorSpecification piid = this.getXesId(ctx.piid);
        final List<XesParameterSpecification> parameters = this.getXesParameters(ctx.xesEmitVariable());
        this.composer.addInstruction(XesExportSpecification.ofTraceExport(pid, piid, parameters));
    }

    @Override
    public void exitEmitStatementXesEvent(EmitStatementXesEventContext ctx) {
        this.handleEthqlElement(ctx, this::handleEmitStatementXesEvent);
    }

    private void handleEmitStatementXesEvent(EmitStatementXesEventContext ctx) throws BuildException {
        final ValueAccessorSpecification pid = this.getXesId(ctx.pid);
        final ValueAccessorSpecification piid = this.getXesId(ctx.piid);
        final ValueAccessorSpecification eid = this.getXesId(ctx.eid);
        final List<XesParameterSpecification> parameters = this.getXesParameters(ctx.xesEmitVariable());
        this.composer.addInstruction(XesExportSpecification.ofEventExport(pid, piid, eid, parameters));
    }

    private ValueAccessorSpecification getXesId(ValueExpressionContext ctx) throws BuildException {
        return ctx == null ? null : this.getValueAccessor(ctx);
    }

    private List<XesParameterSpecification> getXesParameters(List<XesEmitVariableContext> variables) throws BuildException {
        final LinkedList<XesParameterSpecification> parameters = new LinkedList<>();
        for (XesEmitVariableContext varCtx : variables) {
            final String name = varCtx.variableName() == null 
                ? varCtx.valueExpression().variableName().toString()
                : varCtx.variableName().toString();
            final ValueAccessorSpecification accessor = this.getValueAccessor(varCtx.valueExpression());

            XesParameterSpecification parameter = null;
            switch (varCtx.xesTypes().getText()) {
                case "xs:string" : parameter = XesParameterSpecification.ofStringLiteral(name, accessor); break;
                case "xs:date" : parameter = XesParameterSpecification.ofDateParameter(name, accessor); break;
                case "xs:int" : parameter = XesParameterSpecification.ofIntegerParameter(name, accessor); break;
                case "xs:float" : parameter = XesParameterSpecification.ofFloatParameter(name, accessor); break;
                case "xs:boolean"  : parameter = XesParameterSpecification.ofBooleanParameter(name, accessor); break;
                default : throw new BuildException(String.format("Xes type '%s' not supported", varCtx.xesTypes().getText()));
            }
            parameters.add(parameter);
        }
        
        return parameters;
    }



    //#region Utils

    private <T extends ParserRuleContext> void handleEthqlElement(T ctx, BuilderMethod<T> builderMethod) {
        if (this.containsError()) {
            return;
        }

        try {
            builderMethod.build(ctx);
        } catch (BuildException e) {
            this.error = e;
        }
    }

    @FunctionalInterface
    private static interface BuilderMethod<T>  {
        public void build(T ctx) throws BuildException;
    }

    private ValueAccessorSpecification getValueAccessor(ValueExpressionContext ctx) throws BuildException {
        if (ctx.variableName() != null) {
            return ValueAccessorSpecification.ofVariable(ctx.getText());
        }
        else if (ctx.literal() != null) {
            return this.getLiteral(TypeUtils.INT_TYPE_KEYWORD, ctx.literal());
        }
        else {
            throw new UnsupportedOperationException("This value accessor specification is not supported.");
        }
    }

    private ValueAccessorSpecification getLiteral(String type, LiteralContext ctx) throws BuildException {
        return this.getLiteral(type, ctx.toString());
    }

    private ValueAccessorSpecification getLiteral(String type, String literal) throws BuildException {
        final boolean isArray = TypeUtils.isArrayType(type);
        if (TypeUtils.isArrayType(type, TypeUtils.ADDRESS_TYPE_KEYWORD)) {
            return isArray 
                ? ValueAccessorSpecification.addressArrayLiteral(literal)
                : ValueAccessorSpecification.addressLiteral(literal);
        }
        else if (TypeUtils.isArrayType(type, TypeUtils.BOOL_TYPE_KEYWORD)) {
            return isArray 
                ? ValueAccessorSpecification.booleanArrayLiteral(literal)
                : ValueAccessorSpecification.booleanLiteral(literal);
        }
        else if (TypeUtils.isArrayType(type, TypeUtils.BYTES_TYPE_KEYWORD)) {
            return isArray 
                ? ValueAccessorSpecification.bytesArrayLiteral(literal)
                : ValueAccessorSpecification.bytesLiteral(literal);
        }
        else if (TypeUtils.isArrayType(type, TypeUtils.INT_TYPE_KEYWORD)) {
            return isArray 
                ? ValueAccessorSpecification.integerArrayLiteral(literal)
                : ValueAccessorSpecification.integerLiteral(literal);
        }
        else if (TypeUtils.isArrayType(type, TypeUtils.STRING_TYPE_KEYWORD)) {
            return isArray 
                ? ValueAccessorSpecification.stringArrayLiteral(literal)
                : ValueAccessorSpecification.stringLiteral(literal);
        }
        else {
            throw new BuildException(String.format("Unsupported type: '%s'.", type));
        }
    }

    //#endregion Utils
}    

    

    

    // @Override
    // public void exitStatement(StatementContext ctx) {
    //     this.handleEthqlElement(ctx, this::buildStatement);
    // }

    // private void buildStatement(StatementContext ctx) throws BuildException {
        // final String assignedVariable = this.getVariable(ctx.variable());
        // if (ctx.valueCreation().methodCall() != null) {
        //     this.buildMethodCallStatement(assignedVariable, ctx.valueCreation().methodCall());
        // } 
        // else if (ctx.valueCreation().variableReference() != null) {
        //     this.buildVariableAssignment(assignedVariable, ctx.valueCreation().variableReference());
        // } 
        // else if (ctx.valueCreation().literal() != null) {
        //     this.buildVariableAssignment(assignedVariable, ctx.valueCreation().literal());
        // }
        // else {
            // throw new BuildException("Unsupported statement declaration.");
        // }
    // }

    // private String getVariable(VariableContext variable) throws BuildException {
    //     if (variable == null) {
    //         return null;
    //     }
    //     else if (variable.variableDefinition() != null) {
    //         return variable.variableDefinition().variableName().getText();
    //     }
    //     else if (variable.variableReference() != null) {
    //         return variable.variableReference().variableName().getText();
    //     }
    //     else {
    //         throw new BuildException("Unsupported variable declaration.");
    //     }
    // }

    // private void buildMethodCallStatement(String assignedVariable, MethodCallContext ctx) throws BuildException {
    //     // TODO: implement
    // }

    // private void buildVariableAssignment(String assignedVariable, VariableReferenceContext variableReference) throws BuildException {
    //     if (assignedVariable == null) {
    //         throw new BuildException("No variable for assignement specified.");
    //     }
    //     final ValueMutatorSpecification variable = ValueMutatorSpecification.ofVariableName(assignedVariable);
    //     final ValueAccessorSpecification value = ValueAccessorSpecification.ofVariable(variableReference.variableName().getText());
    //     this.builder.addVariableAssignment(variable, value);
    // }

    // private void buildVariableAssignment(String assignedVariable, LiteralContext literal) throws BuildException {
    //     if (assignedVariable == null) {
    //         throw new BuildException("No variable for assignement specified.");
    //     }
    //     final String type = this.analyzer.getVariableType(assignedVariable);
    //     final ValueMutatorSpecification variable = ValueMutatorSpecification.ofVariableName(assignedVariable);
    //     final ValueAccessorSpecification value = this.getLiteral(type, literal);
    //     this.builder.addVariableAssignment(variable, value);
    // }    
// }