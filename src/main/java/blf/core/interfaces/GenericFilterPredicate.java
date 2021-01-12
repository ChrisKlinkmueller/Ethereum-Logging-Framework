package blf.core.interfaces;

import blf.core.state.ProgramState;
import blf.core.exceptions.ProgramException;

/**
 * GenericFilterPredicate
 */
@FunctionalInterface
public interface GenericFilterPredicate {
    public boolean test(ProgramState state) throws ProgramException;
}
