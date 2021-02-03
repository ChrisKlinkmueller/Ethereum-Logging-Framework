package blf.core.interfaces;

import blf.core.state.ProgramState;

/**
 * Method
 */
@FunctionalInterface
public interface Method {
    Object call(Object[] parameters, ProgramState state);
}
