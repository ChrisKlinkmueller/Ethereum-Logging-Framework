package blf.blockchains.template.instructions;

import blf.blockchains.template.TemplateProgramState;
import blf.core.instructions.Instruction;
import blf.core.state.ProgramState;
import blf.grammar.BcqlParser;

import java.math.BigInteger;
import java.util.List;
import java.util.logging.Logger;

/**
 * TemplateTransactionFilterInstruction is an Instruction for the Template log extraction mode.
 */
public class TemplateTransactionFilterInstruction extends Instruction {

    private final BcqlParser.TransactionFilterContext transactionCtx;
    @SuppressWarnings({ "FieldCanBeLocal", "unused" })
    private final Logger logger;

    /**
     * Constructs a TemplateTransactionFilterInstruction.
     *
     * @param transactionCtx The context of transaction filter.
     * @param nestedInstructions The list of nested instructions.
     */
    public TemplateTransactionFilterInstruction(BcqlParser.TransactionFilterContext transactionCtx, List<Instruction> nestedInstructions) {
        super(nestedInstructions);

        this.transactionCtx = transactionCtx;
        this.logger = Logger.getLogger(TemplateTransactionFilterInstruction.class.getName());
    }

    /**
     * execute is called once the program is constructed from the manifest. It contains the logic for extracting an
     * event from the Template block that the BLF is currently analyzing. It is called by the Program class.
     *
     * @param state The current ProgramState of the BLF, provided by the Program when called.
     */
    @Override
    public void execute(ProgramState state) {

        // access the current state
        TemplateProgramState templateProgramState = (TemplateProgramState) state;
        BigInteger currentBlock = templateProgramState.getCurrentBlockNumber();
        this.logger.info(String.format("TODO: extract the parameters of the transactionCtx"));

        // extract transactions of the current block related to the parameters specified by the user
        this.logger.info(String.format("TODO: extract the transactions of the current block %s ", currentBlock.toString()));
        // execute nested instructions
        this.logger.info("Execute nested template instructions.");
        this.executeNestedInstructions(templateProgramState);
    }
}
