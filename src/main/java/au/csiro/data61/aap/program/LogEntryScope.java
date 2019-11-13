package au.csiro.data61.aap.program;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import au.csiro.data61.aap.program.suppliers.Variable;
import au.csiro.data61.aap.program.types.SolidityAddress;
import au.csiro.data61.aap.program.types.SolidityBool;
import au.csiro.data61.aap.program.types.SolidityInteger;
import au.csiro.data61.aap.util.MethodResult;

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

    public LogEntryScope(Set<Variable> variables) {
        super(DEFAULT_VARIABLES);
    }

    @Override
    public MethodResult<Void> execute(ProgramState state) {        
        return MethodResult.ofError("Method not implemented");
    }

    @Override
    public Stream<Variable> variableStream() {
        return DEFAULT_VARIABLES.stream();
    }

}