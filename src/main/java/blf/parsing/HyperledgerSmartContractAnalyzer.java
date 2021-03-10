package blf.parsing;

import blf.configuration.BaseBlockchainListener;
import blf.core.values.BlockchainVariables;
import blf.grammar.BcqlParser;
import blf.grammar.BcqlParser.*;
import org.antlr.v4.runtime.Token;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Stream;

/**
 * HyperledgerSmartContractAnalyzer this class can check whether the SMART CONTRACT statement was used correctly in
 * the manifest. It will check if the selected blockchain is hyperledger and the manifest specifies to start with the
 * current block.
 */
public class HyperledgerSmartContractAnalyzer extends SemanticAnalyzer {
    private static final String HYPERLEDGER_BLOCKCHAIN_KEY = "hyperledger";
    private String blockchainKey;
    private boolean currentIsStartingBlock = false;

    public HyperledgerSmartContractAnalyzer() {
        this(new ErrorCollector());
    }

    public HyperledgerSmartContractAnalyzer(ErrorCollector errorCollector) {
        super(errorCollector);

    }

    @Override
    public void clear() {
        this.blockchainKey = null;
    }

    @Override
    public void enterBlockchain(BcqlParser.BlockchainContext ctx) {
        this.blockchainKey = ctx.literal().STRING_LITERAL().getText().replace("\"", "").toLowerCase();
    }

    @Override
    public void enterBlockFilter(BlockFilterContext ctx) {
        this.currentIsStartingBlock = ctx.from.KEY_CURRENT() != null;
    }

    @Override
    public void enterSmartContractFilter(BcqlParser.SmartContractFilterContext ctx) {
        if (this.blockchainKey.equals(this.HYPERLEDGER_BLOCKCHAIN_KEY) && !this.currentIsStartingBlock) {
            this.addError(
                ctx.start,
                "SMART CONTRACT statement used in Hyperledger manifest without the use of CURRENT keyword as start block."
            );
        }
        super.enterSmartContractFilter(ctx);
    }
}
