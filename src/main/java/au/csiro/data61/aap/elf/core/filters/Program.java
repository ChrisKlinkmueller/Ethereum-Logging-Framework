package au.csiro.data61.aap.elf.core.filters;

import java.util.Arrays;
import java.util.List;

import au.csiro.data61.aap.elf.core.Instruction;
import au.csiro.data61.aap.elf.core.ProgramState;

/**
 * Program
 */
public class Program extends Filter {

    public Program(Program... instructions) {
        this(Arrays.asList(instructions));
    }

    public Program(List<Instruction> instructions) {
        super(instructions);
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