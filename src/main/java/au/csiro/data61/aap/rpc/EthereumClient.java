package au.csiro.data61.aap.rpc;

import java.math.BigInteger;

/**
 * EthereumClient
 */
public interface EthereumClient {    
    public void close();
    public BigInteger queryBlockNumber() throws Throwable;
    public EthereumBlock queryBlockData(BigInteger blockNumber) throws Throwable;    
}