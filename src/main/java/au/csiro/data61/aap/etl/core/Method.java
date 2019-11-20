package au.csiro.data61.aap.etl.core;

import au.csiro.data61.aap.etl.core.exceptions.ProgramException;

/**
 * Method
 */
@FunctionalInterface
public interface Method {
    public Object call(Object[] parameters, ProgramState state) throws ProgramException;    
}