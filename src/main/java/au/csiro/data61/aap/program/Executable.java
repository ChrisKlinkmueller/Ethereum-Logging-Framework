package au.csiro.data61.aap.program;

import au.csiro.data61.aap.util.MethodResult;

/**
 * Executable
 */
public interface Executable {   
    public abstract MethodResult<Void> execute(ProgramState state);
}