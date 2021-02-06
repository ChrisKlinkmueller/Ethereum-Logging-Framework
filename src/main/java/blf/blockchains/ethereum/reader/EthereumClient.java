package blf.blockchains.ethereum.reader;

import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Type;

import java.math.BigInteger;
import java.util.List;

/**
 * EthereumClient
 */
public interface EthereumClient {
    void close();

    BigInteger queryBlockNumber();

    EthereumBlock queryBlockData(BigInteger blockNumber);

    @SuppressWarnings("all")
    public List<Type> queryPublicMember(
        String contract,
        BigInteger block,
        String memberName,
        List<Type> inputParameters,
        List<TypeReference<?>> returnTypes
    ) throws Throwable;
}
