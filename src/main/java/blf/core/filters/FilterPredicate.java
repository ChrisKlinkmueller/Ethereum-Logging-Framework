package blf.core.filters;

import blf.core.ProgramState;
import blf.core.exceptions.ProgramException;

/**
 * FilterPredicate
 */
@FunctionalInterface
public interface FilterPredicate<T> {
    public boolean test(ProgramState state, T value) throws ProgramException;
}
