package au.csiro.data61.aap.program;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * GlobalScope
 */
public class GlobalScope extends Scope {
    public static final Set<Variable> DEFAULT_VARIABLES = new HashSet<>();

    @Override
    public void execute(ProgramState state) {
        this.instructionStream().forEach(instr -> instr.execute(state));
    }

    @Override
    public Stream<Variable> defaultVariableStream() {
        return DEFAULT_VARIABLES.stream();
    }

    
}