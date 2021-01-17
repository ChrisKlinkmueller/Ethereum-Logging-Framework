package blf.core.values;

import java.util.Map;

/**
 * DataSourceAccessors
 */
public interface BlockchainVariables {

    ValueAccessor currentBlockNumberAccessor();

    Map<String, String> getBlockVariableNamesAndTypes();

    Map<String, String> getTransactionVariableNamesAndTypes();

    Map<String, String> getLogEntryVariableNamesAndTypes();

    ValueAccessor getValueAccessor(String name);

}
