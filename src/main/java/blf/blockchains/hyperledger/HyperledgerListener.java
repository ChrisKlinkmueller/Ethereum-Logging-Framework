package blf.blockchains.hyperledger;

import blf.blockchains.hyperledger.instructions.HyperledgerConnectInstruction;
import blf.blockchains.hyperledger.state.HyperledgerProgramState;
import blf.configuration.*;
import blf.grammar.BcqlParser;
import blf.parsing.VariableExistenceListener;
import blf.util.TypeUtils;

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

        hyperledgerProgramState.setNetworkConfigFilePath(hyperledgerConnectionParams.get(0));
        hyperledgerProgramState.setServerKeyFilePath(hyperledgerConnectionParams.get(1));
        hyperledgerProgramState.setServerCrtFilePath(hyperledgerConnectionParams.get(2));
        hyperledgerProgramState.setMspName(hyperledgerConnectionParams.get(3));
        hyperledgerProgramState.setChannel(hyperledgerConnectionParams.get(4));

        this.composer.addInstruction(new HyperledgerConnectInstruction());
    }
}
