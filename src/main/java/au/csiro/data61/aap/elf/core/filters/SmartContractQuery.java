package au.csiro.data61.aap.elf.core.filters;

import au.csiro.data61.aap.elf.core.ProgramState;
import au.csiro.data61.aap.elf.core.exceptions.ProgramException;

/**
 * SmartContractQuery
 */
public interface SmartContractQuery {
    public void query(String contract, ProgramState state) throws ProgramException;
}
