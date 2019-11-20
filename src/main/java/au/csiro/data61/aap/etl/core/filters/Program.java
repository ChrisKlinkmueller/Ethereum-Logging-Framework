package au.csiro.data61.aap.etl.core.filters;

import java.util.Arrays;
import java.util.Collections;
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
        super(instructions, Collections.emptyList(), Collections.emptyList());
    }

    public void execute(ProgramState state) {
        try {
            this.executeInstructions(state);
            state.getWriters().writeAllData();
        } catch (final Throwable ex) {
            final String message = "Error when executing the program.";
            state.getExceptionHandler().handleExceptionAndDecideOnAbort(message, ex);
        }
        finally {
            state.close();
        }
    }
    
}