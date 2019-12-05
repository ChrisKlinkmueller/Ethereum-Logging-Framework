package au.csiro.data61.aap.elf.core.readers;

import java.math.BigInteger;
import java.util.List;

import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Type;

/**
 * EthereumClient
 */
public interface EthereumClient {    
    public void close();
    public BigInteger queryBlockNumber() throws Throwable;
    public EthereumBlock queryBlockData(BigInteger blockNumber) throws Throwable;    
    @SuppressWarnings("all")
    public List<Type> queryPublicMember(
        String contract, 
        BigInteger block, 
        String memberName, 
        List<Type> inputParameters, 
        List<TypeReference<?>> returnTypes
    ) throws Throwable;
}