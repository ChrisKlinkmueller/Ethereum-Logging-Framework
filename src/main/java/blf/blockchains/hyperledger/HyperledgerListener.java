package blf.blockchains.hyperledger;

import blf.blockchains.hyperledger.helpers.UserContext;
import blf.blockchains.hyperledger.instructions.*;
import blf.blockchains.hyperledger.state.HyperledgerProgramState;
import blf.configuration.BaseBlockchainListener;
import blf.grammar.BcqlParser;
import blf.parsing.VariableExistenceListener;
import blf.util.TypeUtils;

import java.util.LinkedList;
import java.util.List;
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

    public HyperledgerListener(VariableExistenceListener analyzer) {
        super(analyzer);

        this.state = new HyperledgerProgramState();
        this.logger = Logger.getLogger(HyperledgerListener.class.getName());
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
        HyperledgerProgramState hyperledgerProgramState = (HyperledgerProgramState) state;
        final BcqlParser.LiteralContext literal = ctx.literal();
        final String literalText = ctx.literal().getText();

        if (literal.arrayLiteral() == null || literal.arrayLiteral().stringArrayLiteral() == null) {
            logger.severe("Hyperledger SET CONNECTION parameter should be a String array");
            System.exit(1);
        }

        final List<String> hyperledgerConnectionParams = TypeUtils.parseStringArrayLiteral(literalText);

        if (!(hyperledgerConnectionParams.size() == 5 || hyperledgerConnectionParams.size() == 8)) {
            logger.severe("Hyperledger SET CONNECTION parameter should be a String array of either length 5 or 8");
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

        if (hyperledgerConnectionParams.size() == 8) {
            final String userName = hyperledgerConnectionParams.get(5);
            final String userKeyFilePath = hyperledgerConnectionParams.get(6);
            final String userCrtFilePath = hyperledgerConnectionParams.get(7);

            UserContext userContext = new UserContext(userName, mspName, userKeyFilePath, userCrtFilePath);
            hyperledgerProgramState.setUserContext(userContext);

            if (hyperledgerProgramState.getUserContext() == null) {
                logger.severe("Setting the Hyperledger User has failed");
            } else {
                logger.info("The Hyperledger User has been set successfully");
            }
        }

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

    @Override
    public void exitSmartContractFilter(BcqlParser.SmartContractFilterContext ctx) {
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
        final BcqlParser.TransactionFilterContext transactionFilterCtx = ctx.filter().transactionFilter();
        final BcqlParser.LogEntryFilterContext logEntryFilterCtx = ctx.filter().logEntryFilter();
        final BcqlParser.SmartContractFilterContext smartContractFilterCtx = ctx.filter().smartContractFilter();

        if (blockFilterCtx != null) {
            handleBlockFilterScopeExit(blockFilterCtx);
        }
        if (transactionFilterCtx != null) {
            handleTransactionFilterScopeExit(transactionFilterCtx);
        }
        if (logEntryFilterCtx != null) {
            handleLogEntryFilterScopeExit(logEntryFilterCtx);
        }
        if (smartContractFilterCtx != null) {
            handleSmartContractFilterScopeExit(smartContractFilterCtx);
        }
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
     * This is the handler method in case a smartContractFilter was identified in the manifest file. It reads the
     * parameters according to the selected query from the smartContractFilter context and it checks if they are
     * specified in a semantically correct way. Subsequently it instantiates a smartContractFilterInstruction, which includes
     * the extracted parameters, and adds this wrapper instruction to the list of instructions.
     *
     * @param smartContractFilterCtx - smartContractFilter context
     */

    public void handleSmartContractFilterScopeExit(BcqlParser.SmartContractFilterContext smartContractFilterCtx) {

        final HyperledgerSmartContractFilterInstruction smartContractFilterInstruction = new HyperledgerSmartContractFilterInstruction(
            smartContractFilterCtx,
            this.composer.instructionListsStack.pop()
        );

        this.composer.addInstruction(smartContractFilterInstruction);
    }

}
