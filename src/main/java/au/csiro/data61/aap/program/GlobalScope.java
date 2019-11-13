package au.csiro.data61.aap.program;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import au.csiro.data61.aap.util.MethodResult;

/**
 * GlobalScope
 */
public class GlobalScope extends Scope {
    public static final Set<Variable> DEFAULT_VARIABLES = new HashSet<>();

    @Override
    public MethodResult<Void> execute(ProgramState state) {
        this.instructionStream().forEach(instr -> instr.execute(state));
        return MethodResult.ofResult();
    }

    @Override
    public Stream<Variable> defaultVariableStream() {
        return DEFAULT_VARIABLES.stream();
    }

    
}