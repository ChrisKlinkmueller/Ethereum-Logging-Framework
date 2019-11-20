package au.csiro.data61.aap.etl.library.values;

import java.util.function.Function;

import org.web3j.abi.TypeReference;

import au.csiro.data61.aap.etl.core.Instruction;
import au.csiro.data61.aap.etl.core.ProgramState;

/**
 * DataSourceVariable
 */
class DataSourceVariable {
    private final String name;
    private final TypeReference<?> type;     
    private final Function<ProgramState, Object> valueExtractor;

    public DataSourceVariable(final String name, final TypeReference<?> type, final Function<ProgramState, Object> valueExtractor) {
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

    private static Instruction valueCreator(final String name, final Function<ProgramState, Object> valueExtractor) {
        return (state) -> {
            final Object value = valueExtractor.apply(state);
            state.getValueStore().setValue(name, value);
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
}