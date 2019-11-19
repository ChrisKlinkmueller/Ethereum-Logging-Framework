package au.csiro.data61.aap.etl.core;

/**
 * Method
 */
@FunctionalInterface
public interface Method {
    public Object call(Object[] parameters, ProgramState state) throws EtlException;    
}