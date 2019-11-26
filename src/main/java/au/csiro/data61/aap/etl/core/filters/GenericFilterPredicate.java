package au.csiro.data61.aap.etl.core.filters;

import au.csiro.data61.aap.etl.core.ProgramState;
import au.csiro.data61.aap.etl.core.exceptions.ProgramException;

/**
 * GenericFilterPredicate
 */
@FunctionalInterface
public interface GenericFilterPredicate {
    public boolean test(ProgramState state) throws ProgramException;
}