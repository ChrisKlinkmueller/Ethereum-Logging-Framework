package au.csiro.data61.aap.elf.configuration;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import au.csiro.data61.aap.elf.core.filters.Parameter;
import au.csiro.data61.aap.elf.core.filters.PublicMemberQuery;
import au.csiro.data61.aap.elf.core.filters.SmartContractParameter;
import au.csiro.data61.aap.elf.core.filters.SmartContractQuery;

/**
 * SmartContractFilter
 */
public class SmartContractQuerySpecification {
    private final String contract;
    private final SmartContractQuery query;

    private SmartContractQuerySpecification(String contract, SmartContractQuery query) {
        assert contract != null;
        assert query != null;
        this.query = query;
        this.contract = contract;
    }

    public String getContract() {
        return contract;
    }

    public SmartContractQuery getQuery() {
        return query;
    }

    public static SmartContractQuerySpecification ofMemberVariable(String contract, ParameterSpecification variable) {
        assert contract != null;
        assert variable != null;
        final SmartContractQuery query =
            new PublicMemberQuery(variable.getParameter().getName(), Collections.emptyList(), Arrays.asList(variable.getParameter()));
        return new SmartContractQuerySpecification(contract, query);
    }

    public static SmartContractQuerySpecification ofMemberFunction(String contract, String functionName, List<TypedValueAccessorSpecification> inputParameters, List<ParameterSpecification> outpuParameters) {
        assert contract != null;
        assert inputParameters != null;
        assert outpuParameters != null;
        assert functionName != null;

        final List<SmartContractParameter> inputs = inputParameters.stream()
            .map(param -> createSmartContractParameter(param))
            .collect(Collectors.toList());

        final List<Parameter> outputs = outpuParameters.stream()
            .map(p -> p.getParameter())
            .collect(Collectors.toList());
        
        return new SmartContractQuerySpecification(contract, new PublicMemberQuery(functionName, inputs, outputs));
    }

    private static SmartContractParameter createSmartContractParameter(TypedValueAccessorSpecification param) {
        final String name = createParameterName();
        return new SmartContractParameter(
            param.getType(), 
            name, 
            param.getAccessor()
        );
    }

    private static long COUNTER = 0;
    private static String createParameterName() {
        return String.format("param%s", COUNTER++);
    }
}