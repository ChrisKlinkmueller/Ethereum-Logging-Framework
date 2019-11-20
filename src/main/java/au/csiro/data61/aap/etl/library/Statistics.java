package au.csiro.data61.aap.etl.library;

import au.csiro.data61.aap.etl.core.ProgramState;

/**
 * Statistics
 */
public class Statistics {

    public static Object countTransactions(Object[] parameters, ProgramState state) {
        assert state != null && state.getReader().getCurrentBlock() != null;
        return state.getReader().getCurrentBlock().transactionCount();
    }
    
}