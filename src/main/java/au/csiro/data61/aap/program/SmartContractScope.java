package au.csiro.data61.aap.program;

import java.util.HashSet;
import java.util.Set;

import au.csiro.data61.aap.program.suppliers.Variable;
import au.csiro.data61.aap.util.MethodResult;

/**
 * SmartContractScope
 */
public class SmartContractScope extends Scope {
    public static final Set<Variable> DEFAULT_VARIABLES = new HashSet<>();
    
    public SmartContractScope() {
        super(DEFAULT_VARIABLES);        
    }

    @Override
    public MethodResult<Void> execute(ProgramState state) {
        return MethodResult.ofError("Method not implemented.");
    }
}