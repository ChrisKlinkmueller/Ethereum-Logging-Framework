package blf.blockchains.hyperledger;

import blf.blockchains.hyperledger.instructions.HyperledgerBlockFilterInstruction;
import blf.blockchains.hyperledger.instructions.HyperledgerConnectInstruction;
import blf.blockchains.hyperledger.instructions.HyperledgerLogEntryFilterInstruction;
import blf.blockchains.hyperledger.state.HyperledgerProgramState;
import blf.configuration.*;
import blf.core.exceptions.ExceptionHandler;
import blf.core.exceptions.ProgramException;
import blf.core.values.ValueAccessor;
import blf.grammar.BcqlParser;
import blf.parsing.VariableExistenceListener;
import blf.util.TypeUtils;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.math.BigInteger;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class HyperledgerListener extends BaseBlockchainListener {

    private final Logger logger;
    private final ExceptionHandler exceptionHandler;
    private final HyperledgerProgramState hyperledgerProgramState;

    public HyperledgerListener(VariableExistenceListener analyzer) {
        super(analyzer);

        this.state = new HyperledgerProgramState();

        hyperledgerProgramState = (HyperledgerProgramState) this.state;
        logger = Logger.getLogger(HyperledgerListener.class.getName());
        exceptionHandler = new ExceptionHandler();
    }

    @Override
    public void enterConnection(BcqlParser.ConnectionContext ctx) {
        final BcqlParser.LiteralContext literal = ctx.literal();
        final String literalText = ctx.literal().getText();

        if (literal.arrayLiteral() == null || literal.arrayLiteral().stringArrayLiteral() == null) {
            logger.severe("Hyperledger SET CONNECTION parameter should be a String array");
            System.exit(1);
        }

        final List<String> hyperledgerConnectionParams = TypeUtils.parseStringArrayLiteral(literalText);

        if (hyperledgerConnectionParams.size() != 5) {
            logger.severe("Hyperledger SET CONNECTION parameter should be a String array of length 5");
        }

        final String networkConfigFilePath = hyperledgerConnectionParams.get(0);
        final String serverKeyFilePath = hyperledgerConnectionParams.get(1);
        final String serverCrtFilePath = hyperledgerConnectionParams.get(2);
        final String mspName = hyperledgerConnectionParams.get(3);
        final String channelName = hyperledgerConnectionParams.get(4);

        final HyperledgerConnectInstruction hyperledgerConnectInstruction = new HyperledgerConnectInstruction(
                networkConfigFilePath,
                serverKeyFilePath,
                serverCrtFilePath,
                mspName,
                channelName
        );

        this.composer.addInstruction(hyperledgerConnectInstruction);
    }

    @Override
    public void exitBlockFilter(BcqlParser.BlockFilterContext ctx) {
        this.composer.instructionListsStack.add(new LinkedList<>());
    }

    @Override
    public void exitScope(BcqlParser.ScopeContext ctx) {
        final BcqlParser.BlockFilterContext blockFilterCtx = ctx.filter().blockFilter();
        final BcqlParser.LogEntryFilterContext logEntryCtx = ctx.filter().logEntryFilter();

        if (blockFilterCtx != null) {
            handleBlockFilterScopeExit(blockFilterCtx);
        }

        if (logEntryCtx != null) {
            handleLogEntryScopeExit(logEntryCtx);
        }
    }

    private void handleLogEntryScopeExit(BcqlParser.LogEntryFilterContext logEntryCtx) {
        final BcqlParser.AddressListContext addressListCtx = logEntryCtx.addressList();
        final BcqlParser.LogEntrySignatureContext logEntrySignatureCtx = logEntryCtx.logEntrySignature();

        final List<TerminalNode> stringLiteral = addressListCtx.STRING_LITERAL();
        final BcqlParser.VariableNameContext variableNameCtx = addressListCtx.variableName();

        List<BcqlParser.LogEntryParameterContext> logEntryParameterContextList =
                logEntrySignatureCtx.logEntryParameter();

        if (logEntryParameterContextList == null) {
            this.exceptionHandler.handleExceptionAndDecideOnAbort(
                    "Variable 'logEntryParameterContextList' is null or empty",
                    new NullPointerException()
            );

            logEntryParameterContextList = new LinkedList<>();
        }

        final String eventName = logEntrySignatureCtx.methodName.getText();

        final List<Pair<String, String>> entryParameters = new LinkedList<>();

        for (BcqlParser.LogEntryParameterContext logEntryParameterCtx : logEntryParameterContextList) {
            entryParameters.add(
                    new Pair<>(
                            logEntryParameterCtx.solType().getText(),
                            logEntryParameterCtx.variableName().getText()
                    )
            );
        }

        List<String> addressNames = null;

        if (variableNameCtx != null) {
            final ValueAccessor accessor = ValueAccessor.createVariableAccessor(variableNameCtx.getText());
            String value = "";
            try {
                value = (String) accessor.getValue(this.hyperledgerProgramState);
            } catch (ClassCastException e) {
                String errorMsg = String.format(
                        "Variable '%s' in manifest file is not an instance of String.",
                        variableNameCtx.getText()
                );

                this.exceptionHandler.handleExceptionAndDecideOnAbort(errorMsg, e);
            } catch (ProgramException e) {
                this.exceptionHandler.handleExceptionAndDecideOnAbort("Unexpected exception occurred!", e);
            }

            addressNames = new LinkedList<>(Collections.singletonList(value));
        }

        if (stringLiteral != null && !stringLiteral.isEmpty()) {
            addressNames = stringLiteral.stream()
                    .map(ParseTree::getText)
                    .collect(Collectors.toList());
        }

        final HyperledgerLogEntryFilterInstruction logEntryFilterInstruction =
                new HyperledgerLogEntryFilterInstruction(addressNames, eventName, entryParameters);

        this.composer.addInstruction(logEntryFilterInstruction);
    }

    private void handleBlockFilterScopeExit(BcqlParser.BlockFilterContext ctx) {
        final BcqlParser.LiteralContext fromLiteral = ctx.from.valueExpression().literal();
        final BcqlParser.LiteralContext toLiteral = ctx.to.valueExpression().literal();

        if (fromLiteral.INT_LITERAL() == null) {
            this.exceptionHandler.handleExceptionAndDecideOnAbort(
                    "Hyperledger BLOCKS (`from`)() parameter should be an Integer",
                    new NullPointerException()
            );
        }

        if (toLiteral.INT_LITERAL() == null) {
            this.exceptionHandler.handleExceptionAndDecideOnAbort(
                    "Hyperledger BLOCKS ()(`to`) parameter should be an Integer",
                    new NullPointerException()
            );
        }

        final String fromBlockNumberString = ctx.from.valueExpression().literal().getText();
        final String toBlockNumberString = ctx.to.valueExpression().literal().getText();

        final BigInteger fromBlockNumber = new BigInteger(fromBlockNumberString);
        final BigInteger toBlockNumber = new BigInteger(toBlockNumberString);

        final HyperledgerBlockFilterInstruction hyperledgerBlockFilterInstruction =
                new HyperledgerBlockFilterInstruction(
                        fromBlockNumber,
                        toBlockNumber,
                        this.composer.instructionListsStack.pop()
                );

        this.composer.addInstruction(hyperledgerBlockFilterInstruction);
    }

}
