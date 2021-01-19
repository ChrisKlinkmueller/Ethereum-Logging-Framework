package blf.blockchains.hyperledger;

import blf.blockchains.hyperledger.helpers.HyperledgerListenerHelper;
import blf.blockchains.hyperledger.instructions.HyperledgerBlockFilterInstruction;
import blf.blockchains.hyperledger.instructions.HyperledgerConnectInstruction;
import blf.blockchains.hyperledger.instructions.HyperledgerLogEntryFilterInstruction;
import blf.blockchains.hyperledger.instructions.HyperledgerTransactionFilterInstruction;
import blf.blockchains.hyperledger.state.HyperledgerProgramState;
import blf.configuration.*;
import blf.core.exceptions.ExceptionHandler;
import blf.grammar.BcqlParser;
import blf.parsing.VariableExistenceListener;
import blf.util.TypeUtils;
import org.antlr.v4.runtime.misc.Pair;

import java.util.*;
import java.util.logging.Logger;

/**
 * The HyperledgerListener class implements blockchain specific callback functions for Hyperledger, which are triggered
 * when a parse tree walker enters or exits corresponding parse tree nodes. These callback functions handle how the
 * program should process the input of the manifest file.
 * <p>
 * It extends the abstract BcqlBaseListener class, which already implements blockchain unspecific callback functions.
 */

public class HyperledgerListener extends BaseBlockchainListener {

    private final Logger logger;
    @SuppressWarnings("FieldCanBeLocal")
    private final HyperledgerProgramState hyperledgerProgramState;
    private final ExceptionHandler exceptionHandler;

    public HyperledgerListener(VariableExistenceListener analyzer) {
        super(analyzer);

        this.state = new HyperledgerProgramState();
        this.hyperledgerProgramState = (HyperledgerProgramState) this.state;
        this.logger = Logger.getLogger(HyperledgerListener.class.getName());
        this.exceptionHandler = hyperledgerProgramState.getExceptionHandler();
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

    @Override
    public void exitLogEntryFilter(BcqlParser.LogEntryFilterContext ctx) {
        this.composer.instructionListsStack.add(new LinkedList<>());
    }

    @Override
    public void exitTransactionFilter(BcqlParser.TransactionFilterContext ctx) {
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
        super.exitScope(ctx);

        final BcqlParser.BlockFilterContext blockFilterCtx = ctx.filter().blockFilter();
        final BcqlParser.LogEntryFilterContext logEntryFilterCtx = ctx.filter().logEntryFilter();
        final BcqlParser.TransactionFilterContext transactionFilterCtx = ctx.filter().transactionFilter();

        if (blockFilterCtx != null) {
            handleBlockFilterScopeExit(blockFilterCtx);
        }
        if (logEntryFilterCtx != null) {
            handleLogEntryFilterScopeExit(logEntryFilterCtx);
        }
        if (transactionFilterCtx != null) {
            handleTransactionFilterScopeExit(transactionFilterCtx);
        }
    }

    /**
     * This is the handler method in case a transactionFilter was identified in the manifest file. It reads the
     * parameters 'senders' and 'recipients' from the transactionFilter context and it checks if they are
     * specified in a semantically correct way. Subsequently it instantiates a hyperledgerTransactionFilterInstruction,
     * which includes the extracted parameters, and adds this wrapper instruction to the list of instructions.
     *
     * @param transactionCtx - transactionFilter context
     */

    public void handleTransactionFilterScopeExit(BcqlParser.TransactionFilterContext transactionCtx) {

        final HyperledgerTransactionFilterInstruction hyperledgerTransactionFilterInstruction = new HyperledgerTransactionFilterInstruction(
            transactionCtx,
            this.composer.instructionListsStack.pop()
        );

        this.composer.addInstruction(hyperledgerTransactionFilterInstruction);
    }

    /**
     * This is the handler method in case a logEntryFilter was identified in the manifest file. It reads the
     * parameters 'addressList' and 'logEntrySignature' from the logEntryFilter context and it checks if they are
     * specified in a semantically correct way. Subsequently it instantiates a logEntryFilterInstruction, which includes
     * the extracted parameters, and adds this wrapper instruction to the list of instructions.
     *
     * @param logEntryCtx - logEntryFilter context
     */

    public void handleLogEntryFilterScopeExit(BcqlParser.LogEntryFilterContext logEntryCtx) {

        final HyperledgerLogEntryFilterInstruction logEntryFilterInstruction = new HyperledgerLogEntryFilterInstruction(
            logEntryCtx,
            this.composer.instructionListsStack.pop()
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
     * @param blockCtx - blockFilter context
     */

    private void handleBlockFilterScopeExit(BcqlParser.BlockFilterContext blockCtx) {
        final HyperledgerBlockFilterInstruction hyperledgerBlockFilterInstruction = new HyperledgerBlockFilterInstruction(
            blockCtx,
            this.composer.instructionListsStack.pop()
        );

        this.composer.addInstruction(hyperledgerBlockFilterInstruction);
    }

}
