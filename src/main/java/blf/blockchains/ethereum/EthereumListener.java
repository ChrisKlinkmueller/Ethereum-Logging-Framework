package blf.blockchains.ethereum;

import java.util.*;
import java.util.function.BiFunction;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import blf.blockchains.ethereum.instructions.EthereumBlockFilterInstruction;
import blf.blockchains.ethereum.instructions.EthereumConnectInstruction;
import blf.blockchains.ethereum.instructions.EthereumConnectIpcInstruction;
import blf.blockchains.ethereum.state.EthereumProgramState;
import blf.configuration.*;

import blf.parsing.InterpreterUtils;
import blf.parsing.VariableExistenceListener;
import blf.util.TypeUtils;
import blf.grammar.BcqlParser;
import org.antlr.v4.runtime.ParserRuleContext;

import org.antlr.v4.runtime.tree.ParseTree;

public class EthereumListener extends BaseBlockchainListener {
    private static final Logger LOGGER = Logger.getLogger(EthereumListener.class.getName());

    public EthereumListener(VariableExistenceListener analyzer) {
        super(analyzer);

        this.state = new EthereumProgramState();
    }

    @Override
    public void exitConnection(BcqlParser.ConnectionContext ctx) {
        this.handleEthqlElement(ctx, this::buildConnection);
    }

    private void buildConnection(BcqlParser.ConnectionContext ctx) {
        final EthereumProgramState ethereumProgramState = (EthereumProgramState) state;
        final BcqlParser.LiteralContext literal = ctx.literal();
        final String literalText = ctx.literal().getText();

        if (literal.STRING_LITERAL() == null) {
            LOGGER.severe("Ethereum SET CONNECTION parameter should be a String");
            System.exit(1);
        }

        final String connectionInputParameter = TypeUtils.parseStringLiteral(literalText);

        if (ctx.KEY_IPC() != null) {
            ethereumProgramState.connectionIpcPath = connectionInputParameter;
            this.composer.instructionListsStack.peek().add(new EthereumConnectIpcInstruction());
        } else {
            ethereumProgramState.connectionUrl = connectionInputParameter;
            this.composer.instructionListsStack.peek().add(new EthereumConnectInstruction());
        }
    }

    @Override
    public void enterBlockFilter(BcqlParser.BlockFilterContext ctx) {
        this.handleEthqlElement(ctx, this::prepareBlockFilterBuild);
    }

    private void prepareBlockFilterBuild(BcqlParser.BlockFilterContext ctx) {
        LOGGER.info("Prepare block filter build");
        try {
            this.composer.prepareBlockRangeBuild();
        } catch (BuildException e) {
            LOGGER.severe(String.format("Preparation of block filter build failed: %s", e.getMessage()));
            System.exit(1);
        }
    }

    private void buildBlockFilter(BcqlParser.BlockFilterContext ctx) {
        LOGGER.info("Build block filter");
        try {
            BlockNumberSpecification from = this.getBlockNumberSpecification(ctx.from);
            BlockNumberSpecification to = this.getBlockNumberSpecification(ctx.to);

            if (this.composer.states.peek() != SpecificationComposer.FactoryState.BLOCK_RANGE_FILTER) {
                throw new BuildException(
                    String.format(
                        "Cannot build a block filter, when construction of %s has not been finished.",
                        this.composer.states.peek()
                    )
                );
            }

            final EthereumBlockFilterInstruction blockRange = new EthereumBlockFilterInstruction(
                from.getValueAccessor(),
                to.getStopCriterion(),
                this.composer.instructionListsStack.peek()
            );

            this.composer.instructionListsStack.pop();
            if (!this.composer.instructionListsStack.isEmpty()) {
                this.composer.instructionListsStack.peek().add(blockRange);
            }
            this.composer.states.pop();

        } catch (BuildException e) {
            LOGGER.severe(String.format("Building block filter failed: %s", e.getMessage()));
            System.exit(1);
        }
    }

    private BlockNumberSpecification getBlockNumberSpecification(BcqlParser.BlockNumberContext ctx) {
        try {
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
        } catch (BuildException e) {
            LOGGER.severe(String.format("Block number specification failed: %s", e.getMessage()));
            System.exit(1);
            return null;
        }
    }

    @Override
    public void enterTransactionFilter(BcqlParser.TransactionFilterContext ctx) {
        this.handleEthqlElement(ctx, this::prepareTransactionFilterBuild);
    }

    private void prepareTransactionFilterBuild(BcqlParser.TransactionFilterContext ctx) {
        LOGGER.info("Prepare transaction filter build");
        try {
            this.composer.prepareTransactionFilterBuild();
        } catch (BuildException e) {
            LOGGER.severe(String.format("Preparation of transaction filter build failed: %s", e.getMessage()));
            System.exit(1);
        }
    }

    private void buildTransactionFilter(BcqlParser.TransactionFilterContext ctx) {
        LOGGER.info("Build Transaction Filter");
        try {
            final AddressListSpecification senders = this.getAddressListSpecification(ctx.senders);
            final AddressListSpecification recipients = this.getAddressListSpecification(ctx.recipients);
            this.composer.buildTransactionFilter(senders, recipients);
        } catch (BuildException e) {
            LOGGER.severe(String.format("Building transaction filter failed: %s", e.getMessage()));
            System.exit(1);
        }
    }

    @Override
    public void enterLogEntryFilter(BcqlParser.LogEntryFilterContext ctx) {
        this.handleEthqlElement(ctx, this::prepareLogEntryFilterBuild);
    }

    private void prepareLogEntryFilterBuild(BcqlParser.LogEntryFilterContext ctx) {
        LOGGER.info("Prepare log entry filter build");
        try {
            this.composer.prepareLogEntryFilterBuild();
        } catch (BuildException e) {
            LOGGER.severe(String.format("Preparation of log entry filter build failed: %s", e.getMessage()));
            System.exit(1);
        }
    }

    private void buildLogEntryFilter(BcqlParser.LogEntryFilterContext ctx) {
        LOGGER.info("Build log entry filter");
        try {
            final AddressListSpecification contracts = this.getAddressListSpecification(ctx.addressList());
            final LogEntrySignatureSpecification signature = this.getLogEntrySignature(ctx.logEntrySignature());
            this.composer.buildLogEntryFilter(contracts, signature);
        } catch (BuildException e) {
            LOGGER.severe(String.format("Building log entry filter failed: %s", e.getMessage()));
            System.exit(1);
        }
    }

    private AddressListSpecification getAddressListSpecification(BcqlParser.AddressListContext ctx) {
        if (ctx.BYTES_LITERAL() != null) {
            return AddressListSpecification.ofAddresses(ctx.BYTES_LITERAL().stream().map(ParseTree::getText).collect(Collectors.toList()));
        } else if (ctx.KEY_ANY() != null) {
            return AddressListSpecification.ofAny();
        } else if (ctx.variableName() != null) {
            return AddressListSpecification.ofVariableName(ctx.variableName().getText());
        } else {
            return AddressListSpecification.ofEmpty();
        }
    }

    private LogEntrySignatureSpecification getLogEntrySignature(BcqlParser.LogEntrySignatureContext ctx) {
        final LinkedList<ParameterSpecification> parameters = new LinkedList<>();
        for (BcqlParser.LogEntryParameterContext paramCtx : ctx.logEntryParameter()) {
            parameters.add(
                ParameterSpecification.of(paramCtx.variableName().getText(), paramCtx.solType().getText(), paramCtx.KEY_INDEXED() != null)
            );
        }
        return LogEntrySignatureSpecification.of(ctx.methodName.getText(), parameters);
    }

    @Override
    public void enterGenericFilter(BcqlParser.GenericFilterContext ctx) {
        this.handleEthqlElement(ctx, this::prepareGenericFilterBuild);
    }

    private void prepareGenericFilterBuild(BcqlParser.GenericFilterContext ctx) {
        LOGGER.info("Prepare generic filter build");
        try {
            this.composer.prepareGenericFilterBuild();
        } catch (BuildException e) {
            LOGGER.severe(String.format("Preparation of generic filter build failed: %s", e.getMessage()));
            System.exit(1);
        }
    }

    private void buildGenericFilter() {
        LOGGER.info("Build generic filter");
        try {
            if (this.genericFilterPredicates.size() != 1) {
                throw new BuildException("Error in boolean expression tree.");
            }

            final Object predicate = this.genericFilterPredicates.pop();
            if (predicate instanceof GenericFilterPredicateSpecification) {
                this.composer.buildGenericFilter((GenericFilterPredicateSpecification) predicate);
            } else if (predicate instanceof ValueAccessorSpecification) {
                final GenericFilterPredicateSpecification filterSpec = GenericFilterPredicateSpecification.ofBooleanAccessor(
                    (ValueAccessorSpecification) predicate
                );
                this.composer.buildGenericFilter(filterSpec);
            } else {
                final String message = String.format(
                    "Unsupported type for specification of generic filter predicates: %s",
                    predicate.getClass()
                );
                throw new BuildException(message);
            }
        } catch (BuildException e) {
            LOGGER.severe(String.format("Building generic filter failed: %s", e.getMessage()));
            System.exit(1);
        }
    }

    @Override
    public void exitScope(BcqlParser.ScopeContext ctx) {
        this.handleEthqlElement(ctx.filter(), this::handleScopeBuild);
    }

    private void handleScopeBuild(BcqlParser.FilterContext ctx) {
        try {
            if (ctx.logEntryFilter() != null) {
                this.buildLogEntryFilter(ctx.logEntryFilter());
            } else if (ctx.blockFilter() != null) {
                this.buildBlockFilter(ctx.blockFilter());
            } else if (ctx.transactionFilter() != null) {
                this.buildTransactionFilter(ctx.transactionFilter());
            } else if (ctx.genericFilter() != null) {
                this.buildGenericFilter();
            } else if (ctx.smartContractFilter() != null) {
                this.buildSmartContractFilter(ctx.smartContractFilter());
            } else {
                throw new BuildException(String.format("Filter type '%s' not supported.", ctx.getText()));
            }
        } catch (BuildException e) {
            LOGGER.severe(String.format("Handling of scope build failed: %s", e.getMessage()));
            System.exit(1);
        }
    }

    @Override
    public void exitConditionalOrExpression(BcqlParser.ConditionalOrExpressionContext ctx) {
        this.handleEthqlElement(ctx, this::handleConditionalOrExpression);
    }

    private void handleConditionalOrExpression(BcqlParser.ConditionalOrExpressionContext ctx) {
        try {
            if (ctx.conditionalOrExpression() != null) {
                this.createBinaryConditionalExpression(GenericFilterPredicateSpecification::or);
            }
        } catch (BuildException e) {
            LOGGER.severe(String.format("Handling of conditional OR expression failed: %s", e.getMessage()));
            System.exit(1);
        }
    }

    @Override
    public void exitConditionalAndExpression(BcqlParser.ConditionalAndExpressionContext ctx) {
        this.handleEthqlElement(ctx, this::handleConditionalAndExpression);
    }

    private void handleConditionalAndExpression(BcqlParser.ConditionalAndExpressionContext ctx) {
        try {
            if (ctx.conditionalAndExpression() != null) {
                this.createBinaryConditionalExpression(GenericFilterPredicateSpecification::and);
            }
        } catch (BuildException e) {
            LOGGER.severe(String.format("Handling of conditional AND expression failed: %s", e.getMessage()));
            System.exit(1);
        }
    }

    private void createBinaryConditionalExpression(
        BiFunction<
            GenericFilterPredicateSpecification,
            GenericFilterPredicateSpecification,
            GenericFilterPredicateSpecification> constructor
    ) throws BuildException {
        if (this.genericFilterPredicates.isEmpty()
            || !(this.genericFilterPredicates.peek() instanceof GenericFilterPredicateSpecification)) {
            throw new BuildException("Parse tree error: binary boolean expression requires boolean predicates.");
        }
        final GenericFilterPredicateSpecification predicate1 = (GenericFilterPredicateSpecification) this.genericFilterPredicates.pop();

        if (this.genericFilterPredicates.isEmpty()
            || !(this.genericFilterPredicates.peek() instanceof GenericFilterPredicateSpecification)) {
            throw new BuildException("Parse tree error: binary boolean expression requires boolean predicates.");
        }
        final GenericFilterPredicateSpecification predicate2 = (GenericFilterPredicateSpecification) this.genericFilterPredicates.pop();

        this.genericFilterPredicates.push(constructor.apply(predicate1, predicate2));
    }

    @Override
    public void exitConditionalComparisonExpression(BcqlParser.ConditionalComparisonExpressionContext ctx) {
        this.handleEthqlElement(ctx, this::handleConditionalComparisonExpression);
    }

    private void handleConditionalComparisonExpression(BcqlParser.ConditionalComparisonExpressionContext ctx) {
        try {
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

            GenericFilterPredicateSpecification predicate;
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
        } catch (BuildException e) {
            LOGGER.severe(String.format("Handling of conditional comparison expression failed: %s", e.getMessage()));
            System.exit(1);
        }
    }

    @Override
    public void exitConditionalNotExpression(BcqlParser.ConditionalNotExpressionContext ctx) {
        this.handleEthqlElement(ctx, this::handleConditionalNotExpression);
    }

    private void handleConditionalNotExpression(BcqlParser.ConditionalNotExpressionContext ctx) {
        try {
            if (ctx.KEY_NOT() == null) {
                return;
            }

            Object valueExpression = this.genericFilterPredicates.pop();
            if (valueExpression instanceof ValueAccessorSpecification) {
                valueExpression = GenericFilterPredicateSpecification.ofBooleanAccessor((ValueAccessorSpecification) valueExpression);
            }

            if (!(valueExpression instanceof GenericFilterPredicateSpecification)) {
                throw new BuildException(
                    String.format("GenericFilterPredicateSpecification required, but was %s.", valueExpression.getClass())
                );
            }
            this.genericFilterPredicates.push(
                GenericFilterPredicateSpecification.not((GenericFilterPredicateSpecification) valueExpression)
            );
        } catch (BuildException e) {
            LOGGER.severe(String.format("Handling of conditional NOT expression failed: %s", e.getMessage()));
            System.exit(1);
        }
    }

    @Override
    public void exitConditionalPrimaryExpression(BcqlParser.ConditionalPrimaryExpressionContext ctx) {
        this.handleEthqlElement(ctx, this::handleConditionalPrimaryExpression);
    }

    private void handleConditionalPrimaryExpression(BcqlParser.ConditionalPrimaryExpressionContext ctx) {
        if (ctx.valueExpression() != null) {
            this.genericFilterPredicates.push(this.getValueAccessor(ctx.valueExpression()));
        }
    }

    @Override
    public void enterSmartContractFilter(BcqlParser.SmartContractFilterContext ctx) {
        this.handleEthqlElement(ctx, this::prepareSmartContractFilterBuilder);
    }

    private void prepareSmartContractFilterBuilder(BcqlParser.SmartContractFilterContext ctx) {
        try {
            this.composer.prepareSmartContractFilterBuild();
        } catch (BuildException e) {
            LOGGER.severe(String.format("Preparation of smart contract filter build failed: %s", e.getMessage()));
            System.exit(1);
        }
    }

    private void buildSmartContractFilter(BcqlParser.SmartContractFilterContext ctx) {
        try {
            final ValueAccessorSpecification contractAddress = this.getValueAccessor(ctx.valueExpression());

            final List<SmartContractQuerySpecification> queries = new ArrayList<>();
            for (BcqlParser.SmartContractQueryContext scQuery : ctx.smartContractQuery()) {
                if (scQuery.publicFunctionQuery() != null) {
                    queries.add(this.handlePublicFunctionQuery(scQuery.publicFunctionQuery()));
                } else if (scQuery.publicVariableQuery() != null) {
                    queries.add(this.handlePublicVariableQuery(scQuery.publicVariableQuery()));
                } else {
                    throw new UnsupportedOperationException();
                }
            }

            this.composer.buildSmartContractFilter(SmartContractFilterSpecification.of(contractAddress, queries));
        } catch (BuildException e) {
            LOGGER.severe(String.format("Building smart contract filter failed: %s", e.getMessage()));
            System.exit(1);
        }
    }

    private SmartContractQuerySpecification handlePublicFunctionQuery(BcqlParser.PublicFunctionQueryContext ctx) {
        final List<ParameterSpecification> outputParams = ctx.smartContractParameter()
            .stream()
            .map(this::createParameterSpecification)
            .collect(Collectors.toList());

        final List<TypedValueAccessorSpecification> inputParameters = new ArrayList<>();
        for (BcqlParser.SmartContractQueryParameterContext paramCtx : ctx.smartContractQueryParameter()) {
            inputParameters.add(this.createTypedValueAccessor(paramCtx));
        }

        return SmartContractQuerySpecification.ofMemberFunction(ctx.methodName.getText(), inputParameters, outputParams);
    }

    private TypedValueAccessorSpecification createTypedValueAccessor(BcqlParser.SmartContractQueryParameterContext ctx) {
        try {
            if (ctx.variableName() != null) {
                final String varName = ctx.variableName().getText();
                return TypedValueAccessorSpecification.of(
                    this.variableAnalyzer.getVariableType(varName),
                    ValueAccessorSpecification.ofVariable(varName)
                );
            } else if (ctx.solType() != null) {
                return TypedValueAccessorSpecification.of(ctx.solType().getText(), this.getLiteral(ctx.literal()));
            } else {
                throw new BuildException("Unsupported way of defining typed value accessors.");
            }
        } catch (BuildException e) {
            LOGGER.severe(String.format("Creation of typed value accessor failed: %s", e.getMessage()));
            System.exit(1);
            return null;
        }
    }

    private SmartContractQuerySpecification handlePublicVariableQuery(BcqlParser.PublicVariableQueryContext ctx) {
        return SmartContractQuerySpecification.ofMemberVariable(createParameterSpecification(ctx.smartContractParameter()));
    }

    private ParameterSpecification createParameterSpecification(BcqlParser.SmartContractParameterContext ctx) {
        return ParameterSpecification.of(ctx.variableName().getText(), ctx.solType().getText());
    }

    @Override
    public void exitEmitStatementLog(BcqlParser.EmitStatementLogContext ctx) {
        this.handleEthqlElement(ctx, this::handleEmitStatementLog);
    }

    private void handleEmitStatementLog(BcqlParser.EmitStatementLogContext ctx) {
        final List<ValueAccessorSpecification> accessors = new LinkedList<>();
        try {
            for (BcqlParser.ValueExpressionContext valEx : ctx.valueExpression()) {
                accessors.add(this.getValueAccessor(valEx));
            }
            final LogLineExportSpecification spec = LogLineExportSpecification.ofValues(accessors);
            this.composer.addInstruction(spec);
        } catch (BuildException e) {
            LOGGER.severe(String.format("Handling of emit statement for Log files failed: %s", e.getMessage()));
            System.exit(1);
        }
    }

    @Override
    public void exitEmitStatementCsv(BcqlParser.EmitStatementCsvContext ctx) {
        this.handleEthqlElement(ctx, this::handleEmitStatementCsv);
    }

    private void handleEmitStatementCsv(BcqlParser.EmitStatementCsvContext ctx) {
        LinkedList<CsvColumnSpecification> columns = new LinkedList<>();
        try {
            for (BcqlParser.NamedEmitVariableContext varCtx : ctx.namedEmitVariable()) {
                final String name = varCtx.valueExpression().variableName() == null
                    ? varCtx.variableName().getText()
                    : varCtx.valueExpression().variableName().getText();
                final ValueAccessorSpecification accessor = this.getValueAccessor(varCtx.valueExpression());
                columns.add(CsvColumnSpecification.of(name, accessor));
            }
            this.composer.addInstruction(CsvExportSpecification.of(this.getValueAccessor(ctx.tableName), columns));
        } catch (BuildException e) {
            LOGGER.severe(String.format("Handling of emit statement for CSV files failed: %s", e.getMessage()));
            System.exit(1);
        }
    }

    @Override
    public void exitEmitStatementXesTrace(BcqlParser.EmitStatementXesTraceContext ctx) {
        this.handleEthqlElement(ctx, this::handleEmitStatementXesTrace);
    }

    private void handleEmitStatementXesTrace(BcqlParser.EmitStatementXesTraceContext ctx) {
        try {
            final ValueAccessorSpecification pid = this.getXesId(ctx.pid);
            final ValueAccessorSpecification piid = this.getXesId(ctx.piid);
            final List<XesParameterSpecification> parameters = this.getXesParameters(ctx.xesEmitVariable());
            this.composer.addInstruction(XesExportSpecification.ofTraceExport(pid, piid, parameters));
        } catch (BuildException e) {
            LOGGER.severe(String.format("Handling of emit statement for XES trace files failed: %s", e.getMessage()));
            System.exit(1);
        }
    }

    @Override
    public void exitEmitStatementXesEvent(BcqlParser.EmitStatementXesEventContext ctx) {
        this.handleEthqlElement(ctx, this::handleEmitStatementXesEvent);
    }

    private void handleEmitStatementXesEvent(BcqlParser.EmitStatementXesEventContext ctx) {
        try {
            final ValueAccessorSpecification pid = this.getXesId(ctx.pid);
            final ValueAccessorSpecification piid = this.getXesId(ctx.piid);
            final ValueAccessorSpecification eid = this.getXesId(ctx.eid);
            final List<XesParameterSpecification> parameters = this.getXesParameters(ctx.xesEmitVariable());
            this.composer.addInstruction(XesExportSpecification.ofEventExport(pid, piid, eid, parameters));
        } catch (BuildException e) {
            LOGGER.severe(String.format("Handling of emit statement for XES event files failed: %s", e.getMessage()));
            System.exit(1);
        }
    }

    private ValueAccessorSpecification getXesId(BcqlParser.ValueExpressionContext ctx) {
        return ctx == null ? null : this.getValueAccessor(ctx);
    }

    private List<XesParameterSpecification> getXesParameters(List<BcqlParser.XesEmitVariableContext> variables) {
        final LinkedList<XesParameterSpecification> parameters = new LinkedList<>();
        try {
            for (BcqlParser.XesEmitVariableContext varCtx : variables) {
                final String name = varCtx.variableName() == null
                    ? varCtx.valueExpression().variableName().getText()
                    : varCtx.variableName().getText();
                final ValueAccessorSpecification accessor = this.getValueAccessor(varCtx.valueExpression());
                LOGGER.info(varCtx.getText());

                XesParameterSpecification parameter;
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
                        throw new BuildException(String.format("Xes type '%s' not supported", varCtx.xesTypes().getText()));
                }
                parameters.add(parameter);
            }

            return parameters;
        } catch (BuildException e) {
            LOGGER.severe(String.format("Getter of XES parameters failed: %s", e.getMessage()));
            System.exit(1);
            return null;
        }
    }

    @Override
    public void exitMethodStatement(BcqlParser.MethodStatementContext ctx) {
        this.handleEthqlElement(ctx, this::handleMethodStatement);
    }

    private void handleMethodStatement(BcqlParser.MethodStatementContext ctx) {
        this.addMethodCall(ctx.methodInvocation(), null);
    }

    @Override
    public void exitVariableAssignmentStatement(BcqlParser.VariableAssignmentStatementContext ctx) {
        this.handleEthqlElement(ctx, this::handleVariableAssignmentStatement);
    }

    private void handleVariableAssignmentStatement(BcqlParser.VariableAssignmentStatementContext ctx) {
        this.addVariableAssignment(ctx.variableName(), ctx.statementExpression());
    }

    @Override
    public void exitVariableDeclarationStatement(BcqlParser.VariableDeclarationStatementContext ctx) {
        this.handleEthqlElement(ctx, this::handleVariableDeclarationStatement);
    }

    private void handleVariableDeclarationStatement(BcqlParser.VariableDeclarationStatementContext ctx) {
        this.addVariableAssignment(ctx.variableName(), ctx.statementExpression());
    }

    private void addVariableAssignment(BcqlParser.VariableNameContext varCtx, BcqlParser.StatementExpressionContext stmtCtx) {
        try {
            final ValueMutatorSpecification mutator = ValueMutatorSpecification.ofVariableName(varCtx.getText());
            if (stmtCtx.valueExpression() != null) {
                this.addValueAssignment(mutator, stmtCtx.valueExpression());
            } else if (stmtCtx.methodInvocation() != null) {
                this.addMethodCall(stmtCtx.methodInvocation(), mutator);
            } else {
                throw new UnsupportedOperationException("This type of value definition is not supported.");
            }
        } catch (UnsupportedOperationException e) {
            LOGGER.severe(String.format("Adding variable assignment failed: %s", e.getMessage()));
            System.exit(1);
        }
    }

    private void addValueAssignment(ValueMutatorSpecification mutator, BcqlParser.ValueExpressionContext ctx) {
        try {
            final ValueAccessorSpecification accessor = this.getValueAccessor(ctx);
            final ValueAssignmentSpecification assignment = ValueAssignmentSpecification.of(mutator, accessor);
            this.composer.addInstruction(assignment);
        } catch (BuildException e) {
            LOGGER.severe(String.format("Adding value assignment failed: %s", e.getMessage()));
            System.exit(1);
        }
    }

    private void addMethodCall(BcqlParser.MethodInvocationContext ctx, ValueMutatorSpecification mutator) {
        final List<String> parameterTypes = new ArrayList<>();
        final List<ValueAccessorSpecification> accessors = new ArrayList<>();
        try {
            for (BcqlParser.ValueExpressionContext valCtx : ctx.valueExpression()) {
                parameterTypes.add(InterpreterUtils.determineType(valCtx, this.variableAnalyzer));
                accessors.add(this.getValueAccessor(valCtx));
            }

            final MethodSpecification method = MethodSpecification.of(ctx.methodName.getText(), parameterTypes);
            final MethodCallSpecification call = MethodCallSpecification.of(method, mutator, accessors);
            this.composer.addInstruction(call);
        } catch (BuildException e) {
            LOGGER.severe(String.format("Adding method call failed: %s", e.getMessage()));
            System.exit(1);
        }
    }

    // #region Utils

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
    private interface BuilderMethod<T> {
        void build(T ctx) throws BuildException;
    }

    private ValueAccessorSpecification getValueAccessor(BcqlParser.ValueExpressionContext ctx) {
        try {
            if (ctx.variableName() != null) {
                return ValueAccessorSpecification.ofVariable(ctx.getText());
            } else if (ctx.literal() != null) {
                return this.getLiteral(ctx.literal());
            } else {
                throw new UnsupportedOperationException("This value accessor specification is not supported.");
            }
        } catch (UnsupportedOperationException e) {
            LOGGER.severe(String.format("Getter of value accessor failed: %s", e.getMessage()));
            System.exit(1);
            return null;
        }
    }

    private ValueAccessorSpecification getLiteral(BcqlParser.LiteralContext ctx) {
        String type = this.determineLiteralType(ctx);
        return this.getLiteral(type, ctx.getText());
    }

    private String determineLiteralType(BcqlParser.LiteralContext ctx) {
        try {
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
                throw new BuildException(String.format("Cannot determine type for literal %s.", ctx.getText()));
            }
            return type;
        } catch (BuildException e) {
            LOGGER.severe(String.format("Determination of literal type failed: %s", e.getMessage()));
            System.exit(1);
            return null;
        }

    }

    private ValueAccessorSpecification getLiteral(String type, String literal) {
        try {
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
        } catch (BuildException e) {
            LOGGER.severe(String.format("Getter of literal failed: %s", e.getMessage()));
            System.exit(1);
            return null;
        }
    }

    // #endregion Utils

}
