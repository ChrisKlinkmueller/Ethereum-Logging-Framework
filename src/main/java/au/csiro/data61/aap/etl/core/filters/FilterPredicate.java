package au.csiro.data61.aap.etl.core.filters;

import au.csiro.data61.aap.etl.core.ProgramState;
import au.csiro.data61.aap.etl.core.exceptions.ProgramException;

/**
 * FilterPredicate
 */
@FunctionalInterface
public interface FilterPredicate<T> {
    public boolean test(ProgramState state, T value) throws ProgramException;    
}