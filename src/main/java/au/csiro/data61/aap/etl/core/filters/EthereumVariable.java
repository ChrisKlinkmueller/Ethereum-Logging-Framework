package au.csiro.data61.aap.etl.core.filters;

import java.io.IOException;
import java.util.Set;

import org.web3j.abi.TypeReference;

import au.csiro.data61.aap.etl.core.Instruction;
import au.csiro.data61.aap.etl.core.ProgramState;
import au.csiro.data61.aap.etl.core.exceptions.ProgramException;
import au.csiro.data61.aap.etl.core.values.UserVariables;
import au.csiro.data61.aap.etl.core.values.ValueAccessor;

/**
 * DataSourceVariable
 */
class EthereumVariable {
    private final String name;
    private final TypeReference<?> type;     
    private final ValueExtractor<ProgramState> valueExtractor;

    public EthereumVariable(final String name, final TypeReference<?> type, final ValueExtractor<ProgramState> valueExtractor) {
        assert name != null;
        assert type != null;
        assert valueExtractor != null;
        this.name = name;
        this.type = type;
        this.valueExtractor = valueExtractor;
    }

    boolean hasName(String name) {
        return name != null && this.name.equals(name);
    }

    String getName() {
        return this.name;
    }

    Instruction getValueCreator() {
        return valueCreator(this.name, this.valueExtractor);
    }

    private static Instruction valueCreator(final String name, final ValueExtractor<ProgramState> valueExtractor) {
        return (state) -> {
            try {
                final Object value = valueExtractor.extract(state);
                state.getValueStore().setValue(name, value);
            }
            catch (IOException ex) {
                throw new ProgramException(String.format("Error exctracting a value '%s'.", name), ex);
            }
        };
    }

    Instruction getValueRemover() {
        return valueRemover(this.name);
    }

    private static Instruction valueRemover(final String name) {
        return state -> state.getValueStore().removeValue(name);
    }

    TypeReference<?> getType() {
        return this.type;
    }

    ValueAccessor getAccessor() {
        return UserVariables.createValueAccessor(this.name);
    }

    static <T> void addVariable(Set<EthereumVariable> variables, String name, String type, ValueExtractor<ProgramState> valueExtractor) {
        try {
            variables.add(new EthereumVariable(name, TypeReference.makeTypeReference(type), valueExtractor));
        }
        catch (Throwable error) {
            error.printStackTrace();
        }
    }

    @FunctionalInterface
    static interface ValueExtractor<T> {
        public Object extract(T object) throws IOException;
    }
}