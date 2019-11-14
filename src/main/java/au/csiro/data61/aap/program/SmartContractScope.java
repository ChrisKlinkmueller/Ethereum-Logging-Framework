package au.csiro.data61.aap.program;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;

import au.csiro.data61.aap.program.suppliers.Variable;
import au.csiro.data61.aap.util.MethodResult;

/**
 * SmartContractScope
 */
public class SmartContractScope extends Scope {
    public static final Set<Variable> DEFAULT_VARIABLES = Collections.emptySet();

    @Override
    public MethodResult<Void> execute(ProgramState state) {
        return MethodResult.ofError("Method not implemented.");
    }

    @Override
    public Stream<? extends Variable> variableStream() {
        return Stream.empty();
    }
}