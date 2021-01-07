package blf.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import blf.core.filters.EthereumSmartContractQuery;
import blf.core.values.ValueAccessor;

/**
 * SmartContractFilterSpecification
 */
public class SmartContractFilterSpecification {
    private final List<EthereumSmartContractQuery> queries;
    private final ValueAccessor contractAddress;

    private SmartContractFilterSpecification(ValueAccessor contractAddress, List<EthereumSmartContractQuery> queries) {
        this.contractAddress = contractAddress;
        this.queries = queries;
    }

    ValueAccessor getContractAddress() {
        return this.contractAddress;
    }

    List<EthereumSmartContractQuery> getQueries() {
        return this.queries;
    }

    public static SmartContractFilterSpecification of(ValueAccessorSpecification contractAddress, SmartContractQuerySpecification queries)
        throws BuildException {
        return of(contractAddress, Arrays.asList(queries));
    }

    public static SmartContractFilterSpecification of(
        ValueAccessorSpecification contractAddress,
        List<SmartContractQuerySpecification> querySpecs
    ) {
        final ArrayList<EthereumSmartContractQuery> queries = new ArrayList<>();
        for (SmartContractQuerySpecification querySpec : querySpecs) {
            queries.add(querySpec.getQuery());
        }

        return new SmartContractFilterSpecification(contractAddress.getValueAccessor(), queries);
    }

}
