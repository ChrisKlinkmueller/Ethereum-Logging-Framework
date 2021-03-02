package blf.blockchains.template.instructions;

import blf.blockchains.template.TemplateProgramState;
import blf.core.instructions.Instruction;
import blf.core.state.ProgramState;
import blf.grammar.BcqlParser;

import java.math.BigInteger;
import java.util.List;
import java.util.logging.Logger;

/**
 * TemplateLogEntryFilterInstruction is an Instruction for the Template log extraction mode of the Blockchain
 * Logging Framework. It extracts the requested event from the current Block and stores the extracted parameter values
 * in the ProgramState.
 */
public class TemplateLogEntryFilterInstruction extends Instruction {

    private final BcqlParser.LogEntryFilterContext logEntryFilterCtx;

    @SuppressWarnings({ "FieldCanBeLocal", "unused" })
    private final Logger logger;

    /**
     * Constructs a TemplateLogEntryFilterInstruction.
     *
     * @param logEntryFilterCtx  The context of logEntryFilter.
     * @param nestedInstructions The list of nested instruction.
     */
    public TemplateLogEntryFilterInstruction(BcqlParser.LogEntryFilterContext logEntryFilterCtx, List<Instruction> nestedInstructions) {
        super(nestedInstructions);

        this.logEntryFilterCtx = logEntryFilterCtx;
        this.logger = Logger.getLogger(TemplateBlockFilterInstruction.class.getName());
    }

    /**
     * execute is called once the program is constructed from the manifest. It contains the logic for extracting an
     * event from the Template block that the BLF is currently analyzing. It is called by the Program class.
     *
     * @param state The current ProgramState of the BLF, provided by the Program when called.
     */
    @Override
    public void execute(ProgramState state) {

        TemplateProgramState TemplateProgramState = (TemplateProgramState) state;

        // parse the logEntryFilterCtx to extract the event name, parameters and addresses
        this.logger.info("TODO: parse the logEntryFilterCtx.");
        String eventName = "youEventName";

        // you can access variables from the state
        BigInteger currentBlock = ((TemplateProgramState) state).getCurrentBlockNumber();

        // extract the events of the block that match the events specified by the user
        this.logger.info(String.format("TODO: extract events of block %s", currentBlock.toString()));

        // execute nested instructions
        this.logger.info("Execute nested log entry instructions.");
        this.executeNestedInstructions(TemplateProgramState);
    }

}
