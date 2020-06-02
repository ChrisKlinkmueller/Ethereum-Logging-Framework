package au.csiro.data61.aap.elf.configuration;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.function.BiFunction;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.ParserRuleContext;

import au.csiro.data61.aap.elf.core.filters.Program;
import au.csiro.data61.aap.elf.parsing.EthqlBaseListener;
import au.csiro.data61.aap.elf.parsing.InterpreterUtils;
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
import au.csiro.data61.aap.elf.parsing.EthqlParser.FilterContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.GenericFilterContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.LiteralContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.LogEntryFilterContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.LogEntryParameterContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.LogEntrySignatureContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.MethodInvocationContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.MethodStatementContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.NamedEmitVariableContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.PublicFunctionQueryContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.PublicVariableQueryContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.ScopeContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.SmartContractFilterContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.SmartContractParameterContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.SmartContractQueryContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.SmartContractQueryParameterContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.StatementExpressionContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.TransactionFilterContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.ValueExpressionContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.VariableAssignmentStatementContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.VariableDeclarationStatementContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.VariableNameContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.XesEmitVariableContext;
import au.csiro.data61.aap.elf.util.TypeUtils;

/**
 * EthqlProgramComposer
 */
public class EthqlProgramComposer extends EthqlBaseListener {
    private static final Logger LOGGER = Logger.getLogger(EthqlProgramComposer.class.getName());

    private final SpecificationComposer composer;
    private final VariableExistenceAnalyzer variableAnalyzer;

    private final Stack<Object> genericFilterPredicates;
    private BuildException error;
    private Program program;

    public EthqlProgramComposer(VariableExistenceAnalyzer analyzer) {
        this.composer = new SpecificationComposer();
        this.variableAnalyzer = analyzer;
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
        LOGGER.info("Prepare Program Build");
        this.error = null;
        this.composer.prepareProgramBuild();
    }

    @Override
    public void exitDocument(DocumentContext ctx) {
        LOGGER.info("Build Program");
        this.handleEthqlElement(ctx, this::buildProgram);
    }

    private void buildProgram(DocumentContext ctx) throws BuildException {
        try {
            this.program = this.composer.buildProgram();
        } finally {
            this.genericFilterPredicates.clear();
        }
    }

    @Override
    public void enterBlockFilter(BlockFilterContext ctx) {
        this.handleEthqlElement(ctx, this::prepareBlockFilterBuild);
    }

    private void prepareBlockFilterBuild(BlockFilterContext ctx) throws BuildException {
        LOGGER.info("Prepare Block Filter Build");
        this.composer.prepareBlockRangeBuild();
    }

    private void buildBlockFilter(BlockFilterContext ctx) throws BuildException {
        LOGGER.info("Build Block Filter");
        BlockNumberSpecification from = this.getBlockNumberSpecification(ctx.from);
        BlockNumberSpecification to = this.getBlockNumberSpecification(ctx.to);
        this.composer.buildBlockRange(from, to);
    }

    private BlockNumberSpecification getBlockNumberSpecification(BlockNumberContext ctx)
            throws BuildException {
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
        LOGGER.info("Prepare Transaction Filter Build");
        this.composer.prepareTransactionFilterBuild();
    }

    private void buildTransactionFilter(TransactionFilterContext ctx) throws BuildException {
        LOGGER.info("Build Transaction Filter");
        final AddressListSpecification senders = this.getAddressListSpecification(ctx.senders);
        final AddressListSpecification recipients =
                this.getAddressListSpecification(ctx.recipients);
        this.composer.buildTransactionFilter(senders, recipients);
    }

    @Override
    public void enterLogEntryFilter(LogEntryFilterContext ctx) {
        this.handleEthqlElement(ctx, this::prepareLogEntryFilterBuild);
    }

    private void prepareLogEntryFilterBuild(LogEntryFilterContext ctx) throws BuildException {
        LOGGER.info("Prepare Log Entry Filter Build");
        this.composer.prepareLogEntryFilterBuild();
    }

    private void buildLogEntryFilter(LogEntryFilterContext ctx) throws BuildException {
        LOGGER.info("Build Log Entry Filter");
        final AddressListSpecification contracts =
                this.getAddressListSpecification(ctx.addressList());
        final LogEntrySignatureSpecification signature =
                this.getLogEntrySignature(ctx.logEntrySignature());
        this.composer.buildLogEntryFilter(contracts, signature);
    }

    private AddressListSpecification getAddressListSpecification(AddressListContext ctx)
            throws BuildException {
        if (ctx.BYTES_LITERAL() != null) {
            return AddressListSpecification.ofAddresses(ctx.BYTES_LITERAL().stream()
                    .map(literal -> literal.getText()).collect(Collectors.toList()));
        } else if (ctx.KEY_ANY() != null) {
            return AddressListSpecification.ofAny();
        } else if (ctx.variableName() != null) {
            return AddressListSpecification.ofAddress(ctx.variableName().getText());
        } else {
            return AddressListSpecification.ofEmpty();
        }
    }

    private LogEntrySignatureSpecification getLogEntrySignature(LogEntrySignatureContext ctx)
            throws BuildException {
        final LinkedList<ParameterSpecification> parameters = new LinkedList<>();
        for (LogEntryParameterContext paramCtx : ctx.logEntryParameter()) {
            parameters.add(ParameterSpecification.of(paramCtx.variableName().getText(),
                    paramCtx.solType().getText(), paramCtx.KEY_INDEXED() != null));
        }

        return LogEntrySignatureSpecification.of(ctx.methodName.getText(), parameters);
    }

    @Override
    public void enterGenericFilter(GenericFilterContext ctx) {
        this.handleEthqlElement(ctx, this::prepareGenericFilterBuild);
    }

    private void prepareGenericFilterBuild(GenericFilterContext ctx) throws BuildException {
        LOGGER.info("Prepare Generic Filter Build");
        this.composer.prepareGenericFilterBuild();
    }

    private void buildGenericFilter(GenericFilterContext ctx) throws BuildException {
        LOGGER.info("Build Generic Filter");

        if (this.genericFilterPredicates.size() != 1) {
            throw new BuildException("Error in boolean expression tree.");
        }

        final Object predicate = this.genericFilterPredicates.pop();
        if (predicate instanceof GenericFilterPredicateSpecification) {
            this.composer.buildGenericFilter((GenericFilterPredicateSpecification) predicate);
        } else if (predicate instanceof ValueAccessorSpecification) {
            final GenericFilterPredicateSpecification filterSpec =
                    GenericFilterPredicateSpecification
                            .ofBooleanAccessor((ValueAccessorSpecification) predicate);
            this.composer.buildGenericFilter(filterSpec);
        } else {
            final String message = String.format(
                    "Unsupported type for specification of generic filter predicates: %s",
                    predicate.getClass());
            throw new BuildException(message);
        }
    }

    @Override
    public void exitScope(ScopeContext ctx) {
        this.handleEthqlElement(ctx.filter(), this::handleScopeBuild);
    }

    private void handleScopeBuild(FilterContext ctx) throws BuildException {
        if (ctx.logEntryFilter() != null) {
            this.buildLogEntryFilter(ctx.logEntryFilter());
        } else if (ctx.blockFilter() != null) {
            this.buildBlockFilter(ctx.blockFilter());
        } else if (ctx.transactionFilter() != null) {
            this.buildTransactionFilter(ctx.transactionFilter());
        } else if (ctx.genericFilter() != null) {
            this.buildGenericFilter(ctx.genericFilter());
        } else if (ctx.smartContractFilter() != null) {
            this.buildSmartContractFilter(ctx.smartContractFilter());
        } else {
            throw new BuildException(
                    String.format("Filter type '%s' not supported.", ctx.getText()));
        }
    }

    @Override
    public void exitConditionalOrExpression(ConditionalOrExpressionContext ctx) {
        this.handleEthqlElement(ctx, this::handleConditionalOrExpression);
    }

    private void handleConditionalOrExpression(ConditionalOrExpressionContext ctx)
            throws BuildException {
        if (ctx.conditionalOrExpression() != null) {
            this.createBinaryConditionalExpression(GenericFilterPredicateSpecification::or);
        }
    }

    @Override
    public void exitConditionalAndExpression(ConditionalAndExpressionContext ctx) {
        this.handleEthqlElement(ctx, this::handleConditionalAndExpression);
    }

    private void handleConditionalAndExpression(ConditionalAndExpressionContext ctx)
            throws BuildException {
        if (ctx.conditionalAndExpression() != null) {
            this.createBinaryConditionalExpression(GenericFilterPredicateSpecification::and);
        }
    }

    private void createBinaryConditionalExpression(
            BiFunction<GenericFilterPredicateSpecification, GenericFilterPredicateSpecification, GenericFilterPredicateSpecification> constructor)
            throws BuildException {
        if (this.genericFilterPredicates.isEmpty() || !(this.genericFilterPredicates
                .peek() instanceof GenericFilterPredicateSpecification)) {
            throw new BuildException(
                    "Parse tree error: binary boolean expression requires boolean predicates.");
        }
        final GenericFilterPredicateSpecification predicate1 =
                (GenericFilterPredicateSpecification) this.genericFilterPredicates.pop();

        if (this.genericFilterPredicates.isEmpty() || !(this.genericFilterPredicates
                .peek() instanceof GenericFilterPredicateSpecification)) {
            throw new BuildException(
                    "Parse tree error: binary boolean expression requires boolean predicates.");
        }
        final GenericFilterPredicateSpecification predicate2 =
                (GenericFilterPredicateSpecification) this.genericFilterPredicates.pop();

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
        if (!(value1 instanceof ValueAccessorSpecification)) {
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
                throw new BuildException(
                        String.format("Comparator %s not supported.", ctx.comparators().getText()));
        }

        this.genericFilterPredicates.push(predicate);
    }

    @Override
    public void exitConditionalNotExpression(ConditionalNotExpressionContext ctx) {
        this.handleEthqlElement(ctx, this::handleConditionalNotExpression);
    }

    private void handleConditionalNotExpression(ConditionalNotExpressionContext ctx)
            throws BuildException {
        if (ctx.KEY_NOT() == null) {
            return;
        }

        Object valueExpression = this.genericFilterPredicates.pop();
        if (valueExpression instanceof ValueAccessorSpecification) {
            valueExpression = GenericFilterPredicateSpecification
                    .ofBooleanAccessor((ValueAccessorSpecification) valueExpression);
        }

        if (!(valueExpression instanceof GenericFilterPredicateSpecification)) {
            throw new BuildException(
                    String.format("GenericFilterPredicateSpecification required, but was %s.",
                            valueExpression.getClass()));
        }
        this.genericFilterPredicates.push(GenericFilterPredicateSpecification
                .not((GenericFilterPredicateSpecification) valueExpression));
    }

    @Override
    public void exitConditionalPrimaryExpression(ConditionalPrimaryExpressionContext ctx) {
        this.handleEthqlElement(ctx, this::handleConditionalPrimaryExpression);
    }

    private void handleConditionalPrimaryExpression(ConditionalPrimaryExpressionContext ctx)
            throws BuildException {
        if (ctx.valueExpression() != null) {
            this.genericFilterPredicates.push(this.getValueAccessor(ctx.valueExpression()));
        }
    }

    @Override
    public void enterSmartContractFilter(SmartContractFilterContext ctx) {
        this.handleEthqlElement(ctx, this::prepareSmartContractFilterBuilder);
    }

    private void prepareSmartContractFilterBuilder(SmartContractFilterContext ctx)
            throws BuildException {
        this.composer.prepareSmartContractFilterBuild();
    }

    private void buildSmartContractFilter(SmartContractFilterContext ctx) throws BuildException {
        final ValueAccessorSpecification contractAddress =
                this.getValueAccessor(ctx.valueExpression());

        final List<SmartContractQuerySpecification> queries = new ArrayList<>();
        for (SmartContractQueryContext scQuery : ctx.smartContractQuery()) {
            if (scQuery.publicFunctionQuery() != null) {
                queries.add(this.handlePublicFunctionQuery(scQuery.publicFunctionQuery()));
            } else if (scQuery.publicVariableQuery() != null) {
                queries.add(this.handlePublicVariableQuery(scQuery.publicVariableQuery()));
            } else {
                throw new UnsupportedOperationException();
            }
        }

        this.composer.buildSmartContractFilter(
                SmartContractFilterSpecification.of(contractAddress, queries));
    }

    private SmartContractQuerySpecification handlePublicFunctionQuery(
            PublicFunctionQueryContext ctx) throws BuildException {
        final List<ParameterSpecification> outputParams = ctx.smartContractParameter().stream()
                .map(paramCtx -> this.createParameterSpecification(paramCtx))
                .collect(Collectors.toList());

        final List<TypedValueAccessorSpecification> inputParameters = new ArrayList<>();
        for (SmartContractQueryParameterContext paramCtx : ctx.smartContractQueryParameter()) {
            inputParameters.add(this.createTypedValueAccessor(paramCtx));
        }

        return SmartContractQuerySpecification.ofMemberFunction(ctx.methodName.getText(),
                inputParameters, outputParams);
    }

    private TypedValueAccessorSpecification createTypedValueAccessor(
            SmartContractQueryParameterContext ctx) throws BuildException {
        if (ctx.variableName() != null) {
            final String varName = ctx.variableName().getText();
            return TypedValueAccessorSpecification.of(
                    this.variableAnalyzer.getVariableType(varName),
                    ValueAccessorSpecification.ofVariable(varName));
        } else if (ctx.solType() != null) {
            return TypedValueAccessorSpecification.of(ctx.solType().getText(),
                    this.getLiteral(ctx.literal()));
        } else {
            throw new BuildException("Unsupported way of defining typed value accessors.");
        }

    }

    private SmartContractQuerySpecification handlePublicVariableQuery(
            PublicVariableQueryContext ctx) throws BuildException {
        return SmartContractQuerySpecification
                .ofMemberVariable(createParameterSpecification(ctx.smartContractParameter()));
    }

    private ParameterSpecification createParameterSpecification(SmartContractParameterContext ctx) {
        return ParameterSpecification.of(ctx.variableName().getText(), ctx.solType().getText());
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
            final String name = varCtx.valueExpression().variableName() == null
                    ? varCtx.variableName().getText()
                    : varCtx.valueExpression().variableName().getText();
            final ValueAccessorSpecification accessor =
                    this.getValueAccessor(varCtx.valueExpression());
            columns.add(CsvColumnSpecification.of(name, accessor));
        }
        this.composer.addInstruction(
                CsvExportSpecification.of(this.getValueAccessor(ctx.tableName), columns));
    }

    @Override
    public void exitEmitStatementXesTrace(EmitStatementXesTraceContext ctx) {
        this.handleEthqlElement(ctx, this::handleEmitStatementXesTrace);
    }

    private void handleEmitStatementXesTrace(EmitStatementXesTraceContext ctx)
            throws BuildException {
        final ValueAccessorSpecification pid = this.getXesId(ctx.pid);
        final ValueAccessorSpecification piid = this.getXesId(ctx.piid);
        final List<XesParameterSpecification> parameters =
                this.getXesParameters(ctx.xesEmitVariable());
        this.composer.addInstruction(XesExportSpecification.ofTraceExport(pid, piid, parameters));
    }

    @Override
    public void exitEmitStatementXesEvent(EmitStatementXesEventContext ctx) {
        this.handleEthqlElement(ctx, this::handleEmitStatementXesEvent);
    }

    private void handleEmitStatementXesEvent(EmitStatementXesEventContext ctx)
            throws BuildException {
        final ValueAccessorSpecification pid = this.getXesId(ctx.pid);
        final ValueAccessorSpecification piid = this.getXesId(ctx.piid);
        final ValueAccessorSpecification eid = this.getXesId(ctx.eid);
        final List<XesParameterSpecification> parameters =
                this.getXesParameters(ctx.xesEmitVariable());
        this.composer
                .addInstruction(XesExportSpecification.ofEventExport(pid, piid, eid, parameters));
    }

    private ValueAccessorSpecification getXesId(ValueExpressionContext ctx) throws BuildException {
        return ctx == null ? null : this.getValueAccessor(ctx);
    }

    private List<XesParameterSpecification> getXesParameters(List<XesEmitVariableContext> variables)
            throws BuildException {
        final LinkedList<XesParameterSpecification> parameters = new LinkedList<>();
        for (XesEmitVariableContext varCtx : variables) {
            final String name = varCtx.variableName() == null
                    ? varCtx.valueExpression().variableName().getText()
                    : varCtx.variableName().getText();
            final ValueAccessorSpecification accessor =
                    this.getValueAccessor(varCtx.valueExpression());
            LOGGER.info(varCtx.getText());

            XesParameterSpecification parameter = null;
            switch (varCtx.xesTypes().getText()) {
                case "xs:string":
                    parameter = XesParameterSpecification.ofStringParameter(name, accessor);
                    break;
                case "xs:date":
                    parameter = XesParameterSpecification.ofDateParameter(name, accessor);
                    break;
                case "xs:int":
                    parameter = XesParameterSpecification.ofIntegerParameter(name, accessor);
                    break;
                case "xs:float":
                    parameter = XesParameterSpecification.ofFloatParameter(name, accessor);
                    break;
                case "xs:boolean":
                    parameter = XesParameterSpecification.ofBooleanParameter(name, accessor);
                    break;
                default:
                    throw new BuildException(String.format("Xes type '%s' not supported",
                            varCtx.xesTypes().getText()));
            }
            parameters.add(parameter);
        }

        return parameters;
    }

    @Override
    public void exitMethodStatement(MethodStatementContext ctx) {
        this.handleEthqlElement(ctx, this::handleMethodStatement);
    }

    private void handleMethodStatement(MethodStatementContext ctx) throws BuildException {
        this.addMethodCall(ctx.methodInvocation(), null);
    }

    @Override
    public void exitVariableAssignmentStatement(VariableAssignmentStatementContext ctx) {
        this.handleEthqlElement(ctx, this::handleVariableAssignmentStatement);
    }

    private void handleVariableAssignmentStatement(VariableAssignmentStatementContext ctx)
            throws BuildException {
        this.addVariableAssignment(ctx.variableName(), ctx.statementExpression());
    }

    @Override
    public void exitVariableDeclarationStatement(VariableDeclarationStatementContext ctx) {
        this.handleEthqlElement(ctx, this::handleVariableDeclarationStatement);
    }

    private void handleVariableDeclarationStatement(VariableDeclarationStatementContext ctx)
            throws BuildException {
        this.addVariableAssignment(ctx.variableName(), ctx.statementExpression());
    }

    private void addVariableAssignment(VariableNameContext varCtx,
            StatementExpressionContext stmtCtx) throws BuildException {
        final ValueMutatorSpecification mutator =
                ValueMutatorSpecification.ofVariableName(varCtx.getText());
        if (stmtCtx.valueExpression() != null) {
            this.addValueAssignment(mutator, stmtCtx.valueExpression());
        } else if (stmtCtx.methodInvocation() != null) {
            this.addMethodCall(stmtCtx.methodInvocation(), mutator);
        } else {
            throw new UnsupportedOperationException(
                    "This type of value definition is not supported.");
        }
    }

    private void addValueAssignment(ValueMutatorSpecification mutator, ValueExpressionContext ctx)
            throws BuildException {
        final ValueAccessorSpecification accessor = this.getValueAccessor(ctx);
        final ValueAssignmentSpecification assignment =
                ValueAssignmentSpecification.of(mutator, accessor);
        this.composer.addInstruction(assignment);
    }

    private void addMethodCall(MethodInvocationContext ctx, ValueMutatorSpecification mutator)
            throws BuildException {
        final List<String> parameterTypes = new ArrayList<>();
        final List<ValueAccessorSpecification> accessors = new ArrayList<>();
        for (ValueExpressionContext valCtx : ctx.valueExpression()) {
            parameterTypes.add(InterpreterUtils.determineType(valCtx, this.variableAnalyzer));
            accessors.add(this.getValueAccessor(valCtx));
        }

        final MethodSpecification method =
                MethodSpecification.of(ctx.methodName.getText(), parameterTypes);
        final MethodCallSpecification call = MethodCallSpecification.of(method, mutator, accessors);
        this.composer.addInstruction(call);
    }



    // #region Utils

    private <T extends ParserRuleContext> void handleEthqlElement(T ctx,
            BuilderMethod<T> builderMethod) {
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
    private static interface BuilderMethod<T> {
        public void build(T ctx) throws BuildException;
    }

    private ValueAccessorSpecification getValueAccessor(ValueExpressionContext ctx)
            throws BuildException {
        if (ctx.variableName() != null) {
            return ValueAccessorSpecification.ofVariable(ctx.getText());
        } else if (ctx.literal() != null) {
            return this.getLiteral(ctx.literal());
        } else {
            throw new UnsupportedOperationException(
                    "This value accessor specification is not supported.");
        }
    }

    private ValueAccessorSpecification getLiteral(LiteralContext ctx) throws BuildException {
        String type = this.determineLiteralType(ctx);
        return this.getLiteral(type, ctx.getText());
    }

    private String determineLiteralType(LiteralContext ctx) throws BuildException {
        String type = null;
        if (ctx.BOOLEAN_LITERAL() != null) {
            type = TypeUtils.BOOL_TYPE_KEYWORD;
        } else if (ctx.BYTES_LITERAL() != null) {
            type = TypeUtils.BYTES_TYPE_KEYWORD;
        } else if (ctx.INT_LITERAL() != null) {
            type = TypeUtils.INT_TYPE_KEYWORD;
        } else if (ctx.STRING_LITERAL() != null) {
            type = TypeUtils.STRING_TYPE_KEYWORD;
        } else if (ctx.arrayLiteral() != null) {
            if (ctx.arrayLiteral().booleanArrayLiteral() != null) {
                type = TypeUtils.toArrayType(TypeUtils.BOOL_TYPE_KEYWORD);
            } else if (ctx.arrayLiteral().bytesArrayLiteral() != null) {
                type = TypeUtils.toArrayType(TypeUtils.BYTES_TYPE_KEYWORD);
            } else if (ctx.arrayLiteral().intArrayLiteral() != null) {
                type = TypeUtils.toArrayType(TypeUtils.INT_TYPE_KEYWORD);
            } else if (ctx.arrayLiteral().stringArrayLiteral() != null) {
                type = TypeUtils.toArrayType(TypeUtils.STRING_TYPE_KEYWORD);
            }
        }

        if (type == null) {
            throw new BuildException(
                    String.format("Cannot determine type for literal %s.", ctx.getText()));
        }
        return type;
    }

    private ValueAccessorSpecification getLiteral(String type, String literal)
            throws BuildException {
        if (TypeUtils.isArrayType(type)) {
            if (TypeUtils.isArrayType(type, TypeUtils.ADDRESS_TYPE_KEYWORD)) {
                return ValueAccessorSpecification.addressArrayLiteral(literal);
            } else if (TypeUtils.isArrayType(type, TypeUtils.BOOL_TYPE_KEYWORD)) {
                return ValueAccessorSpecification.booleanArrayLiteral(literal);
            } else if (TypeUtils.isArrayType(type, TypeUtils.BYTES_TYPE_KEYWORD)) {
                return ValueAccessorSpecification.bytesArrayLiteral(literal);
            } else if (TypeUtils.isArrayType(type, TypeUtils.INT_TYPE_KEYWORD)) {
                return ValueAccessorSpecification.integerArrayLiteral(literal);
            } else if (TypeUtils.isArrayType(type, TypeUtils.STRING_TYPE_KEYWORD)) {
                return ValueAccessorSpecification.stringArrayLiteral(literal);
            } else {
                throw new BuildException(String.format("Unsupported type: '%s'.", type));
            }
        } else {
            if (TypeUtils.isAddressType(type)) {
                return ValueAccessorSpecification.addressLiteral(literal);
            } else if (TypeUtils.isBooleanType(type)) {
                return ValueAccessorSpecification.booleanLiteral(literal);
            } else if (TypeUtils.isBytesType(type)) {
                return ValueAccessorSpecification.bytesLiteral(literal);
            } else if (TypeUtils.isIntegerType(type)) {
                return ValueAccessorSpecification.integerLiteral(literal);
            } else if (TypeUtils.isStringType(type)) {
                return ValueAccessorSpecification.stringLiteral(literal);
            } else {
                throw new BuildException(String.format("Unsupported type: '%s'.", type));
            }
        }
    }

    // #endregion Utils

}
