package blf.blockchains.template;

import blf.blockchains.template.instructions.TemplateBlockFilterInstruction;
import blf.blockchains.template.instructions.TemplateConnectInstruction;
import blf.blockchains.template.instructions.TemplateLogEntryFilterInstruction;
import blf.blockchains.template.instructions.TemplateTransactionFilterInstruction;
import blf.configuration.BaseBlockchainListener;
import blf.grammar.BcqlParser;
import blf.parsing.VariableExistenceListener;

import java.util.LinkedList;
import java.util.logging.Logger;

/**
 * The TemplateListener class implements blockchain specific callback functions for Template, which are triggered
 * when a parse tree walker enters or exits corresponding parse tree nodes. These callback functions handle how the
 * program should process the input of the manifest file.
 * <p>
 * It extends the abstract BcqlBaseListener class, which already implements blockchain unspecific callback functions.
 */

public class TemplateListener extends BaseBlockchainListener {

    private final Logger logger;

    public TemplateListener(VariableExistenceListener analyzer) {
        super(analyzer);

        this.state = new TemplateProgramState();
        this.logger = Logger.getLogger(TemplateListener.class.getName());
    }

    /**
     * When entering the connection parse tree node, the listener reads the parameters which are stated after the
     * SET CONNECTION keywords in the manifest file to build a connection to Template. Subsequently it checks if the
     * parameters are specified in a semantically correct way and added to the list of instructions.
     *
     * @param ctx - local connection context
     */

    @Override
    public void enterConnection(BcqlParser.ConnectionContext ctx) {
        this.logger.info("TODO: parse connect parameters from ctx.");
        final TemplateConnectInstruction TemplateConnectInstruction = new TemplateConnectInstruction();

        this.composer.addInstruction(TemplateConnectInstruction);
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
    }

    /**
     * This is the handler method in case a blockFilter was identified in the manifest file. It reads the parameters
     * 'from' and 'to' from the blockFilter context and it checks if they are specified in a semantically correct way.
     * Subsequently it instantiates a TemplateBlockFilterInstruction, which includes the 'from' and 'to' block
     * numbers and the statements included inside the scope as nested instructions, and adds this wrapper instruction to
     * the list of instructions.
     *
     * @param blockCtx - blockFilter context
     */

    private void handleBlockFilterScopeExit(BcqlParser.BlockFilterContext blockCtx) {
        final TemplateBlockFilterInstruction TemplateBlockFilterInstruction = new TemplateBlockFilterInstruction(
            blockCtx,
            this.composer.instructionListsStack.pop()
        );

        this.composer.addInstruction(TemplateBlockFilterInstruction);
    }

    /**
     * This is the handler method in case a transactionFilter was identified in the manifest file. It reads the
     * parameters 'senders' and 'recipients' from the transactionFilter context and it checks if they are
     * specified in a semantically correct way. Subsequently it instantiates a TemplateTransactionFilterInstruction,
     * which includes the extracted parameters, and adds this wrapper instruction to the list of instructions.
     *
     * @param transactionCtx - transactionFilter context
     */

    public void handleTransactionFilterScopeExit(BcqlParser.TransactionFilterContext transactionCtx) {
        final TemplateTransactionFilterInstruction TemplateTransactionFilterInstruction = new TemplateTransactionFilterInstruction(
            transactionCtx,
            this.composer.instructionListsStack.pop()
        );

        this.composer.addInstruction(TemplateTransactionFilterInstruction);
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
        final TemplateLogEntryFilterInstruction logEntryFilterInstruction = new TemplateLogEntryFilterInstruction(
            logEntryCtx,
            this.composer.instructionListsStack.pop()
        );

        this.composer.addInstruction(logEntryFilterInstruction);
    }
}
