package blf.core.interfaces;

import blf.core.state.ProgramState;

/**
 * GenericFilterPredicate
 */
@FunctionalInterface
public interface GenericFilterPredicate {
    boolean test(ProgramState state);
}
