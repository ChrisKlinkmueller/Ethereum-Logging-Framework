package au.csiro.data61.aap.elf.core.filters;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.web3j.abi.TypeDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Type;

import au.csiro.data61.aap.elf.core.ProgramState;
import au.csiro.data61.aap.elf.core.exceptions.ProgramException;
import au.csiro.data61.aap.elf.core.readers.EthereumClient;

/**
 * PublicMemberQuery
 */
public class PublicMemberQuery implements SmartContractQuery {
    private final String memberName;
    private final List<SmartContractParameter> inputParameters;
    private final List<Parameter> outputParameters;

    public PublicMemberQuery(String memberName, List<SmartContractParameter> inputParameters,
            List<Parameter> outputParameters) {
        assert memberName != null;
        assert inputParameters != null && inputParameters.stream().allMatch(Objects::nonNull);
        assert outputParameters != null && outputParameters.stream().allMatch(Objects::nonNull);
        this.inputParameters = new ArrayList<>(inputParameters);
        this.outputParameters = new ArrayList<>(outputParameters);
        this.memberName = memberName;
    }

    @Override
    @SuppressWarnings("all")
    public void query(String contract, ProgramState state) throws ProgramException {
        assert contract != null;
        assert state != null;

        try {
            final EthereumClient client = state.getReader().getClient();
            final BigInteger block = state.getReader().getCurrentBlock().getNumber();
            final List<Type> inputs = this.createInputTypes(state);
            final List<TypeReference<?>> outputs = this.createReturnTypes();
            final List<Type> values =
                    client.queryPublicMember(contract, block, this.memberName, inputs, outputs);
            this.setValues(values, state);
        } catch (Throwable cause) {
            throw new ProgramException(
                    String.format("Error querying members of smart contract %s", contract), cause);
        }
    }

    @SuppressWarnings("all")
    private List<Type> createInputTypes(ProgramState state) throws Exception {
        final ArrayList<Type> types = new ArrayList<>();
        for (SmartContractParameter param : this.inputParameters) {
            final Object value = param.getAccessor().getValue(state);
            types.add(TypeDecoder.instantiateType(param.getType(), value));
        }
        return types;
    }

    private List<TypeReference<?>> createReturnTypes() {
        return this.outputParameters.stream().map(param -> param.getType())
                .collect(Collectors.toList());
    }

    @SuppressWarnings("all")
    private void setValues(List<Type> values, ProgramState state) {
        if (!this.matchOutputParameters(values)) {
            throw new IllegalArgumentException(
                    "Output parameters not compatible with return values.");
        }

        IntStream.range(0, values.size()).forEach(i -> {
            final Object value = values.get(i).getValue();
            final String name = this.outputParameters.get(i).getName();
            state.getValueStore().setValue(name, value);
        });
    }

    @SuppressWarnings("all")
    private boolean matchOutputParameters(List<Type> values) {
        if (values.size() != this.outputParameters.size()) {
            return false;
        }

        return IntStream.range(0, values.size())
                .allMatch(i -> typesMatch(values.get(i), this.outputParameters.get(i)));
    }

    @SuppressWarnings("all")
    private boolean typesMatch(Type type, Parameter parameter) {
        if (type == null) {
            System.out.println("type == null");
            return false;
        }
        try {
            return parameter.getType().getClassType().equals(type.getClass());
        } catch (Throwable cause) {
            return false;
        }
    }

}
