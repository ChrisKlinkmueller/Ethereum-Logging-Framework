package blf.core.instructions;

import blf.core.Instruction;
import blf.core.ProgramState;
import blf.core.exceptions.ProgramException;

import java.nio.file.Path;
import java.util.logging.Logger;

public class SetOutputFolderInstruction implements Instruction {

    protected static final Logger LOGGER = Logger.getLogger(SetOutputFolderInstruction.class.getName());

    @Override
    public void execute(ProgramState state) throws ProgramException {

        final Path outputFolder = Path.of(state.outputFolderPath);
        if (!outputFolder.toFile().exists()) {
            throw new ProgramException(String.format("Folder '%s' does not exist.", outputFolder.toString()));
        }

        try {
            state.getExceptionHandler().setOutputFolder(outputFolder);
            state.getWriters().setOutputFolder(outputFolder);
        } catch (Exception cause) {
            throw new ProgramException("Error when setting the output folder.", cause);
        }
    }
}
