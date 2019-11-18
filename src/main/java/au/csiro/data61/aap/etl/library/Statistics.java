package au.csiro.data61.aap.etl.library;

import au.csiro.data61.aap.etl.EtlState;

/**
 * Statistics
 */
public class Statistics {

    public static Object countTransactions(Object[] parameters, EtlState state) {
        assert state != null && state.getEthereumSources().getCurrentBlock() != null;
        return state.getEthereumSources().getCurrentBlock().transactionCount();
    }
    
}