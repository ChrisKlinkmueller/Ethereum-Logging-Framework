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

/**
 * The HyperledgerListener class implements blockchain specific callback functions for Hyperledger, which are triggered
 * when a parse tree walker enters or exits corresponding parse tree nodes. These callback functions handle how the
 * program should process the input of the manifest file.
 *
 * It extends the abstract BcqlBaseListener class, which already implements blockchain unspecific callback functions.
 */

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

    /**
     * When entering the connection parse tree node, the listener reads the parameters which are stated after the
     * SET CONNECTION keywords in the manifest file to build a connection to hyperledger. Subsequently it checks if the
     * parameters are specified in a semantically correct way and added to the list of instructions.
     *
     * @param ctx - local connection context
     */

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

    /**
     * When exiting a scope parse tree node, the listener identifies which filter was specified in the local scope
     * context and calls the corresponding handler method accordingly.
     *
     * @param ctx - local scope context
     */

    @Override
    public void exitScope(BcqlParser.ScopeContext ctx) {
        final BcqlParser.BlockFilterContext blockFilterCtx = ctx.filter().blockFilter();
        final BcqlParser.LogEntryFilterContext logEntryCtx = ctx.filter().logEntryFilter();

        if (blockFilterCtx != null) {
            handleBlockFilterScopeExit(blockFilterCtx);
        }
    }

    /**
     * This is the handler method in case a logEntryFilter was identified in the manifest file. It reads the
     * parameters 'addressList' and 'logEntrySignature' from the logEntryFilter context and it checks if they are
     * specified in a semantically correct way. Subsequently it instantiates a logEntryFilterInstruction, which includes
     * the extracted parameters, and adds this wrapper instruction to the list of instructions.
     *
     * @param logEntryCtx - logEntryFilter context
     */

    @Override
    public void enterLogEntryFilter(BcqlParser.LogEntryFilterContext logEntryCtx) {
        final BcqlParser.AddressListContext addressListCtx = logEntryCtx.addressList();
        final BcqlParser.LogEntrySignatureContext logEntrySignatureCtx = logEntryCtx.logEntrySignature();

        final List<TerminalNode> stringLiteral = addressListCtx.STRING_LITERAL();
        final BcqlParser.VariableNameContext variableNameCtx = addressListCtx.variableName();

        List<BcqlParser.LogEntryParameterContext> logEntryParameterContextList = logEntrySignatureCtx.logEntryParameter();

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
            entryParameters.add(new Pair<>(logEntryParameterCtx.solType().getText(), logEntryParameterCtx.variableName().getText()));
        }

        List<String> addressNames = null;

        if (variableNameCtx != null) {
            final ValueAccessor accessor = ValueAccessor.createVariableAccessor(variableNameCtx.getText());
            String value = "";
            try {
                value = (String) accessor.getValue(this.hyperledgerProgramState);
            } catch (ClassCastException e) {
                String errorMsg = String.format("Variable '%s' in manifest file is not an instance of String.", variableNameCtx.getText());

                this.exceptionHandler.handleExceptionAndDecideOnAbort(errorMsg, e);
            } catch (ProgramException e) {
                this.exceptionHandler.handleExceptionAndDecideOnAbort("Unexpected exception occurred!", e);
            }

            addressNames = new LinkedList<>(Collections.singletonList(value));
        }

        if (stringLiteral != null && !stringLiteral.isEmpty()) {
            addressNames = stringLiteral.stream().map(ParseTree::getText).collect(Collectors.toList());
        }

        // remove leading and closing " from all addresses
        for (int i = 0; i < addressNames.size(); i++) {
            if (addressNames.get(i).charAt(0) == '\"') {
                addressNames.set(i, addressNames.get(i).substring(1, addressNames.get(i).length() - 1));
            }
        }

        final HyperledgerLogEntryFilterInstruction logEntryFilterInstruction = new HyperledgerLogEntryFilterInstruction(
            addressNames,
            eventName,
            entryParameters
        );

        this.composer.addInstruction(logEntryFilterInstruction);
    }

    /**
     * This is the handler method in case a blockFilter was identified in the manifest file. It reads the parameters
     * 'from' and 'to' from the blockFilter context and it checks if they are specified in a semantically correct way.
     * Subsequently it instantiates a hyperledgerBlockFilterInstruction, which includes the 'from' and 'to' block
     * numbers and the statements included inside the scope as nested instructions, and adds this wrapper instruction to
     * the list of instructions.
     *
     *
     * @param blockCtx - blockFilter context
     */

    private void handleBlockFilterScopeExit(BcqlParser.BlockFilterContext blockCtx) {
        final BcqlParser.LiteralContext fromLiteral = blockCtx.from.valueExpression().literal();
        final BcqlParser.LiteralContext toLiteral = blockCtx.to.valueExpression().literal();

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

        final String fromBlockNumberString = blockCtx.from.valueExpression().literal().getText();
        final String toBlockNumberString = blockCtx.to.valueExpression().literal().getText();

        final BigInteger fromBlockNumber = new BigInteger(fromBlockNumberString);
        final BigInteger toBlockNumber = new BigInteger(toBlockNumberString);

        final HyperledgerBlockFilterInstruction hyperledgerBlockFilterInstruction = new HyperledgerBlockFilterInstruction(
            fromBlockNumber,
            toBlockNumber,
            this.composer.instructionListsStack.pop()
        );

        this.composer.addInstruction(hyperledgerBlockFilterInstruction);
    }

}
