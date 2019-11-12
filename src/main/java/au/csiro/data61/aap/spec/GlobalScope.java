package au.csiro.data61.aap.spec;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import au.csiro.data61.aap.state.ProgramState;

/**
 * GlobalScope
 */
public class GlobalScope extends Scope {
    public static final Set<Variable> DEFAULT_VARIABLES = new HashSet<>();

    @Override
    public void execute(ProgramState state) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Stream<Variable> defaultVariableStream() {
        return DEFAULT_VARIABLES.stream();
    }

    
}