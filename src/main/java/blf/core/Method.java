package blf.core;

import blf.core.exceptions.ProgramException;

/**
 * Method
 */
@FunctionalInterface
public interface Method {
    public Object call(Object[] parameters, ProgramState state) throws ProgramException;
}
