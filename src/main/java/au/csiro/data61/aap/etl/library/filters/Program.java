package au.csiro.data61.aap.etl.library.filters;

import java.util.Arrays;
import java.util.List;

import au.csiro.data61.aap.etl.core.Instruction;
import au.csiro.data61.aap.etl.core.ProgramState;

/**
 * Program
 */
public class Program extends Filter {

    public Program(Instruction... instructions) {
        this(Arrays.asList(instructions));
    }

    public Program(List<Instruction> instructions) {
        super(instructions);
    }

    public void execute(ProgramState state) {
        try {
            this.executeInstructions(state);
            state.endProgram();
        } catch (final Throwable ex) {
            final String message = "Error when executing the program.";
            state.getExceptionHandler().handleExceptionAndDecideOnAbort(message, ex);
        }
        finally {
            state.close();
        }
    }
    
}