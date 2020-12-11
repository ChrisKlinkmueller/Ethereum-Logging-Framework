package blf;

import blf.configuration.BaseBlockchainListener;
import blf.configuration.EthereumListener;
import blf.parsing.VariableExistenceListener;

import java.util.HashMap;
import java.util.Map;

public class Constants {

    static final String ETHEREUM_BLOCKCHAIN_KEY = "ethereum";

    private static final Map<String, BaseBlockchainListener> blockchainMap = new HashMap<>();

    public static Map<String, BaseBlockchainListener> getBlockchainMap(VariableExistenceListener variableExistenceListener) {
        blockchainMap.put(ETHEREUM_BLOCKCHAIN_KEY, new EthereumListener(variableExistenceListener));

        return blockchainMap;
    }

}
