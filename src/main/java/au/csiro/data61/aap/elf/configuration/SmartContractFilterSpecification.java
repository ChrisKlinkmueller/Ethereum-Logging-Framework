package au.csiro.data61.aap.elf.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import au.csiro.data61.aap.elf.core.filters.SmartContractQuery;
import au.csiro.data61.aap.elf.core.values.ValueAccessor;

/**
 * SmartContractFilterSpecification
 */
public class SmartContractFilterSpecification {
    private final List<SmartContractQuery> queries;
    private final ValueAccessor contractAddress;

    private SmartContractFilterSpecification(ValueAccessor contractAddress, List<SmartContractQuery> queries) {
        this.contractAddress = contractAddress;
        this.queries = queries;
    }

    ValueAccessor getContractAddress() {
        return this.contractAddress;
    }

    List<SmartContractQuery> getQueries() {
        return this.queries;
    }

    public static SmartContractFilterSpecification of(ValueAccessorSpecification contractAddress, SmartContractQuerySpecification queries)
        throws BuildException {
        return of(contractAddress, Arrays.asList(queries));
    }

    public static SmartContractFilterSpecification of(
        ValueAccessorSpecification contractAddress,
        List<SmartContractQuerySpecification> querySpecs
    ) throws BuildException {
        final ArrayList<SmartContractQuery> queries = new ArrayList<>();
        for (SmartContractQuerySpecification querySpec : querySpecs) {
            queries.add(querySpec.getQuery());
        }

        return new SmartContractFilterSpecification(contractAddress.getValueAccessor(), queries);
    }

}
