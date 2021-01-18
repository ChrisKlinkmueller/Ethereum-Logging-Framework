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

        analyzer.setBlockchainVariables(this.state.getBlockchainVariables());
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
                return BlockNumberSpecification.ofCurrent(this.state.getBlockchainVariables());
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
    public void exitScope(BcqlParser.ScopeContext ctx) {
        super.exitScope(ctx);

        this.handleEthqlElement(ctx.filter(), this::handleScopeBuild);
    }

    private void handleScopeBuild(BcqlParser.FilterContext ctx) {
        if (ctx.genericFilter() != null) {
            // already handled by super.exitScope(ctx);
            return;
        }

        try {
            if (ctx.logEntryFilter() != null) {
                this.buildLogEntryFilter(ctx.logEntryFilter());
            } else if (ctx.blockFilter() != null) {
                this.buildBlockFilter(ctx.blockFilter());
            } else if (ctx.transactionFilter() != null) {
                this.buildTransactionFilter(ctx.transactionFilter());
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
                    ValueAccessorSpecification.ofVariable(varName, this.state.getBlockchainVariables())
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

}
