package blf.blockchains.template.instructions;

import blf.blockchains.template.TemplateProgramState;
import blf.core.instructions.Instruction;
import blf.core.state.ProgramState;
import java.util.logging.Logger;

/**
 * This class provides functionality to connect to a Template blockchain node.
 */
public class TemplateConnectInstruction extends Instruction {

    private final Logger logger;

    public TemplateConnectInstruction() {

        this.logger = Logger.getLogger(TemplateConnectInstruction.class.getName());
    }

    @Override
    public void execute(ProgramState state) {
        final TemplateProgramState templateProgramState = (TemplateProgramState) state;

        // connect to your blockchain node
        this.logger.info("Connect to your blockchains node.");
        // store the connection in the state so other components can access it
        this.logger.info("Store the connection in the template program state");
    }
}
