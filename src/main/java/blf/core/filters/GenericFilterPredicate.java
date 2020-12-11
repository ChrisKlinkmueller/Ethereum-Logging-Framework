package blf.core.filters;

import blf.core.ProgramState;
import blf.core.exceptions.ProgramException;

/**
 * GenericFilterPredicate
 */
@FunctionalInterface
public interface GenericFilterPredicate {
    public boolean test(ProgramState state) throws ProgramException;
}
