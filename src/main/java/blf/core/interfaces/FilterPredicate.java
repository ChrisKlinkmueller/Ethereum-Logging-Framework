package blf.core.interfaces;

import blf.core.state.ProgramState;

/**
 * FilterPredicate
 */
@FunctionalInterface
public interface FilterPredicate<T> {
    boolean test(ProgramState state, T value);
}
