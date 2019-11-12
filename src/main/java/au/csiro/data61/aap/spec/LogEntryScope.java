package au.csiro.data61.aap.spec;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import au.csiro.data61.aap.spec.types.SolidityAddress;
import au.csiro.data61.aap.spec.types.SolidityBool;
import au.csiro.data61.aap.spec.types.SolidityInteger;
import au.csiro.data61.aap.state.ProgramState;

/**
 * LogEntryScope
 */
public class LogEntryScope extends Scope {
    public static final Set<Variable> DEFAULT_VARIABLES;

    static {
        DEFAULT_VARIABLES = new HashSet<>();
        addVariable(DEFAULT_VARIABLES, SolidityBool.DEFAULT_INSTANCE, "log.removed");
        addVariable(DEFAULT_VARIABLES, SolidityInteger.DEFAULT_INSTANCE, "log.logIndex");
        addVariable(DEFAULT_VARIABLES, SolidityAddress.DEFAULT_INSTANCE, "log.address");
    }

    @Override
    public void execute(ProgramState state) {
        throw new UnsupportedOperationException("Method not implemented.");
    }

    @Override
    public Stream<Variable> defaultVariableStream() {
        return DEFAULT_VARIABLES.stream();
    }

}