package au.csiro.data61.aap.spec;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import au.csiro.data61.aap.state.ProgramState;

/**
 * SmartContractScope
 */
public class SmartContractScope extends Scope {
    public static final Set<Variable> DEFAULT_VARIABLES = new HashSet<>();
    
    @Override
    public void execute(ProgramState state) {
        throw new UnsupportedOperationException("Method not implemented.");
    }

    @Override
    public Stream<Variable> defaultVariableStream() {
        return DEFAULT_VARIABLES.stream();
    }
}