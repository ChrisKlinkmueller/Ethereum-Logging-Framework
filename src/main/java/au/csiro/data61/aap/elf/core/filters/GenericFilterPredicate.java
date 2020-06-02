package au.csiro.data61.aap.elf.core.filters;

import au.csiro.data61.aap.elf.core.ProgramState;
import au.csiro.data61.aap.elf.core.exceptions.ProgramException;

/**
 * GenericFilterPredicate
 */
@FunctionalInterface
public interface GenericFilterPredicate {
    public boolean test(ProgramState state) throws ProgramException;
}
