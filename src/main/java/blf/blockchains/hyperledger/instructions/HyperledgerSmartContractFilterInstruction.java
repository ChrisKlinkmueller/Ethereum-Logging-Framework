package blf.blockchains.hyperledger.instructions;

import blf.blockchains.hyperledger.helpers.HyperledgerInstructionHelper;
import blf.blockchains.hyperledger.helpers.HyperledgerQueryParameters;
import blf.blockchains.hyperledger.state.HyperledgerProgramState;
import blf.core.exceptions.ExceptionHandler;
import blf.core.instructions.Instruction;
import blf.core.state.ProgramState;
import blf.grammar.BcqlParser;
import org.antlr.v4.runtime.misc.Pair;
import java.util.List;
import java.util.logging.Logger;

public class HyperledgerSmartContractFilterInstruction extends Instruction {

    private final BcqlParser.SmartContractFilterContext smartContractFilterCtx;

    private ExceptionHandler exceptionHandler;
    private final Logger logger;

    public HyperledgerSmartContractFilterInstruction(
        BcqlParser.SmartContractFilterContext smartContractFilterCtx,
        List<Instruction> nestedInstructions
    ) {
        super(nestedInstructions);
        this.smartContractFilterCtx = smartContractFilterCtx;
        this.logger = Logger.getLogger(HyperledgerSmartContractFilterInstruction.class.getName());
    }

    @Override
    public void execute(final ProgramState state) {
        // init exception handler
        this.exceptionHandler = state.getExceptionHandler();

        HyperledgerProgramState hyperledgerProgramState = (HyperledgerProgramState) state;

        final Pair<String, List<HyperledgerQueryParameters>> smartContractQuery = HyperledgerInstructionHelper.parseSmartContractFilterCtx(
            hyperledgerProgramState,
            smartContractFilterCtx
        );
    }

}
