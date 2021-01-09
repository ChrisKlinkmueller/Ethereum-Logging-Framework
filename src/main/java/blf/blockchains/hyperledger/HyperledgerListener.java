package blf.blockchains.hyperledger;

import blf.blockchains.hyperledger.instructions.HyperledgerBlockFilterInstruction;
import blf.blockchains.hyperledger.instructions.HyperledgerConnectInstruction;
import blf.blockchains.hyperledger.state.HyperledgerProgramState;
import blf.configuration.*;
import blf.grammar.BcqlParser;
import blf.parsing.VariableExistenceListener;
import blf.util.TypeUtils;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public class HyperledgerListener extends BaseBlockchainListener {

    private final Logger logger;
    private final HyperledgerProgramState hyperledgerProgramState;

    public HyperledgerListener(VariableExistenceListener analyzer) {
        super(analyzer);

        this.state = new HyperledgerProgramState();

        hyperledgerProgramState = (HyperledgerProgramState) this.state;
        logger = Logger.getLogger(HyperledgerListener.class.getName());
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
        // TODO: add new HyperledgerLogEntryInstruction via this.composer
    }

    private void handleBlockFilterScopeExit(BcqlParser.BlockFilterContext ctx) {
        final BcqlParser.LiteralContext fromLiteral = ctx.from.valueExpression().literal();
        final BcqlParser.LiteralContext toLiteral = ctx.to.valueExpression().literal();

        // TODO: handle exceptions via exceptionHandler
        if (fromLiteral.INT_LITERAL() == null) {
            logger.severe("Hyperledger BLOCKS (`from`)() parameter should be an Integer");
            System.exit(1);
        }

        // TODO: handle exceptions via exceptionHandler
        if (toLiteral.INT_LITERAL() == null) {
            logger.severe("Hyperledger BLOCKS ()(`to`) parameter should be an Integer");
            System.exit(1);
        }

        final String fromBlockNumberString = ctx.from.valueExpression().literal().getText();
        final String toBlockNumberString = ctx.to.valueExpression().literal().getText();

        final BigInteger fromBlockNumber = new BigInteger(fromBlockNumberString);
        final BigInteger toBlockNumber = new BigInteger(toBlockNumberString);

        this.composer.addInstruction(
                new HyperledgerBlockFilterInstruction(
                        fromBlockNumber,
                        toBlockNumber,
                        this.composer.instructionListsStack.pop()
                )
        );
    }

}
