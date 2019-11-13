package au.csiro.data61.aap.program;

import java.util.HashSet;
import java.util.Set;

import au.csiro.data61.aap.util.MethodResult;

/**
 * GlobalScope
 */
public class GlobalScope extends Scope {
    public static final Set<Variable> DEFAULT_VARIABLES = new HashSet<>();

    public GlobalScope() {
        super(DEFAULT_VARIABLES);
    }

    @Override
    public MethodResult<Void> execute(ProgramState state) {
        this.instructionStream().forEach(instr -> instr.execute(state));
        return MethodResult.ofResult();
    }

    
}