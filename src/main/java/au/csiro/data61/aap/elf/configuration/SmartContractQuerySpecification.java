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
    private final SmartContractQuery query;

    private SmartContractQuerySpecification(SmartContractQuery query) {
        assert query != null;
        this.query = query;
    }

    public SmartContractQuery getQuery() {
        return this.query;
    }

    public static SmartContractQuerySpecification ofMemberVariable(ParameterSpecification variable) {
        assert variable != null;
        final SmartContractQuery query = new PublicMemberQuery(
            variable.getParameter().getName(),
            Collections.emptyList(),
            Arrays.asList(variable.getParameter())
        );
        return new SmartContractQuerySpecification(query);
    }

    public static SmartContractQuerySpecification ofMemberFunction(
        String functionName,
        List<TypedValueAccessorSpecification> inputParameters,
        List<ParameterSpecification> outpuParameters
    ) {
        assert inputParameters != null;
        assert outpuParameters != null;
        assert functionName != null;

        final List<SmartContractParameter> inputs = inputParameters.stream()
            .map(param -> createSmartContractParameter(param))
            .collect(Collectors.toList());

        final List<Parameter> outputs = outpuParameters.stream().map(p -> p.getParameter()).collect(Collectors.toList());

        return new SmartContractQuerySpecification(new PublicMemberQuery(functionName, inputs, outputs));
    }

    private static SmartContractParameter createSmartContractParameter(TypedValueAccessorSpecification param) {
        final String name = createParameterName();
        return new SmartContractParameter(param.getType(), name, param.getAccessor());
    }

    private static long COUNTER = 0;

    private static String createParameterName() {
        return String.format("param%s", COUNTER++);
    }
}
