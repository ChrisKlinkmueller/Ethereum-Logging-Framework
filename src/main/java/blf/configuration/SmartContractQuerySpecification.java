package blf.configuration;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import blf.core.filters.Parameter;
import blf.core.filters.PublicMemberQuery;
import blf.core.filters.SmartContractParameter;
import blf.core.filters.SmartContractQuery;
import io.reactivex.annotations.NonNull;

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

    public static SmartContractQuerySpecification ofMemberVariable(@NonNull ParameterSpecification variable) {
        final SmartContractQuery query = new PublicMemberQuery(
            variable.getParameter().getName(),
            Collections.emptyList(),
            Collections.singletonList(variable.getParameter())
        );
        return new SmartContractQuerySpecification(query);
    }

    public static SmartContractQuerySpecification ofMemberFunction(
        @NonNull String functionName,
        @NonNull List<TypedValueAccessorSpecification> inputParameters,
        @NonNull List<ParameterSpecification> outputParameters
    ) {

        final List<SmartContractParameter> inputs = inputParameters.stream()
            .map(SmartContractQuerySpecification::createSmartContractParameter)
            .collect(Collectors.toList());

        final List<Parameter> outputs = outputParameters.stream().map(ParameterSpecification::getParameter).collect(Collectors.toList());

        return new SmartContractQuerySpecification(new PublicMemberQuery(functionName, inputs, outputs));
    }

    private static SmartContractParameter createSmartContractParameter(TypedValueAccessorSpecification param) {
        final String name = createParameterName();
        return new SmartContractParameter(param.getType(), name, param.getAccessor());
    }

    private static long counter = 0;

    private static String createParameterName() {
        return String.format("param%s", counter++);
    }
}
