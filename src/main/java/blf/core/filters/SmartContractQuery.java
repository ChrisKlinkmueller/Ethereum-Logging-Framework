package blf.core.filters;

import blf.core.ProgramState;
import blf.core.exceptions.ProgramException;

/**
 * SmartContractQuery
 */
public interface SmartContractQuery {
    public void query(String contract, ProgramState state) throws ProgramException;
}
