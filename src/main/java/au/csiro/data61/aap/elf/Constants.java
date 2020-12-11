package au.csiro.data61.aap.elf;

import au.csiro.data61.aap.elf.configuration.BaseBlockchainListener;
import au.csiro.data61.aap.elf.configuration.EthereumListener;
import au.csiro.data61.aap.elf.parsing.VariableExistenceListener;

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
