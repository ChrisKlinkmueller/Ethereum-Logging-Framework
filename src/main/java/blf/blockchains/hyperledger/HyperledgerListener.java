package blf.blockchains.hyperledger;

import blf.blockchains.hyperledger.connection.HyperledgerConnection;
import blf.blockchains.hyperledger.state.HyperledgerState;
import blf.configuration.*;
import blf.grammar.BcqlParser;
import blf.parsing.VariableExistenceListener;
import blf.util.TypeUtils;

import java.util.List;
import java.util.logging.Logger;

public class HyperledgerListener extends BaseBlockchainListener {

    private static final Logger LOGGER = Logger.getLogger(HyperledgerListener.class.getName());

    public final HyperledgerState state = new HyperledgerState();

    public HyperledgerListener(VariableExistenceListener analyzer) {
        super(analyzer);
    }

    @Override
    public void enterConnection(BcqlParser.ConnectionContext ctx) {
        final BcqlParser.LiteralContext literal = ctx.literal();
        final String literalText = ctx.literal().getText();

        if (literal.arrayLiteral() == null || literal.arrayLiteral().stringArrayLiteral() == null) {
            LOGGER.severe("Hyperledger SET CONNECTION parameter should be a String array");
            System.exit(1);
        }

        final List<String> hyperledgerConnectionParams = TypeUtils.parseStringArrayLiteral(literalText);

        if (hyperledgerConnectionParams.size() != 4) {
            LOGGER.severe("Hyperledger SET CONNECTION parameter should be a String array of length 4");
        }

        state.gateway = HyperledgerConnection.getGateway(
            hyperledgerConnectionParams.get(0),
            hyperledgerConnectionParams.get(1),
            hyperledgerConnectionParams.get(2)
        );

        state.network = HyperledgerConnection.getNetwork(state.gateway, hyperledgerConnectionParams.get(3));

    }
}
