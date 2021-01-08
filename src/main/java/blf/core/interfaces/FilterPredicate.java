package blf.core.interfaces;

import blf.core.state.ProgramState;
import blf.core.exceptions.ProgramException;

/**
 * FilterPredicate
 */
@FunctionalInterface
public interface FilterPredicate<T> {
    public boolean test(ProgramState state, T value) throws ProgramException;
}
