package blf.blockchains.hyperledger.instructions;

import blf.blockchains.hyperledger.helpers.HyperledgerInstructionHelper;
import blf.blockchains.hyperledger.state.HyperledgerProgramState;
import blf.core.instructions.Instruction;
import blf.core.state.ProgramState;
import blf.grammar.BcqlParser;

import java.util.List;
import java.util.logging.Logger;

public class HyperledgerSmartContractFilterInstruction extends Instruction {

    private final BcqlParser.SmartContractFilterContext smartContractFilterCtx;

    @SuppressWarnings({ "FieldCanBeLocal", "unused" })
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

        HyperledgerProgramState hyperledgerProgramState = (HyperledgerProgramState) state;

        HyperledgerInstructionHelper.parseSmartContractFilterCtx(hyperledgerProgramState, smartContractFilterCtx);
    }

}
