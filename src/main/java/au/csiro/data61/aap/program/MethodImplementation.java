package au.csiro.data61.aap.program;

/**
 * MethodImplementation
 */
@FunctionalInterface
public interface MethodImplementation {
    public Object execute(ProgramState state, Object[] objects) throws Throwable;    
}