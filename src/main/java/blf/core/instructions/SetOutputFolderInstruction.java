package blf.core.instructions;

import blf.core.exceptions.ExceptionHandler;
import blf.core.state.ProgramState;

import java.nio.file.Path;
import java.util.logging.Logger;

public class SetOutputFolderInstruction extends Instruction {

    protected static final Logger LOGGER = Logger.getLogger(SetOutputFolderInstruction.class.getName());

    @Override
    public void execute(ProgramState state) {

        final Path outputFolder = Path.of(state.outputFolderPath);
        if (!outputFolder.toFile().exists()) {
            final String exceptionMsg = String.format("Folder '%s' does not exist.", outputFolder.toString());
            ExceptionHandler.getInstance().handleException(exceptionMsg);
        }

        try {
            state.getWriters().setOutputFolder(outputFolder);
        } catch (Exception cause) {
            ExceptionHandler.getInstance().handleException("Error when setting the output folder.", cause);
        }
    }
}
