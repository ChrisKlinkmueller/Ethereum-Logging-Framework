package au.csiro.data61.aap.elf.core.filters;

import au.csiro.data61.aap.elf.core.ProgramState;
import au.csiro.data61.aap.elf.core.exceptions.ProgramException;

/**
 * FilterPredicate
 */
@FunctionalInterface
public interface FilterPredicate<T> {
    public boolean test(ProgramState state, T value) throws ProgramException;    
}