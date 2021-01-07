package blf.core.writers;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import blf.core.Instruction;
import blf.core.ProgramState;
import blf.core.exceptions.ProgramException;
import blf.core.values.ValueAccessor;
import io.reactivex.annotations.NonNull;

/**
 * AddLogLineInstruction
 */
public class AddLogLineInstruction implements Instruction {
    private final List<ValueAccessor> valueAccessors;

    public AddLogLineInstruction(ValueAccessor... valueAccessors) {
        this(Arrays.asList(valueAccessors));
    }

    public AddLogLineInstruction(@NonNull List<ValueAccessor> valueAccessors) {
        this.valueAccessors = new LinkedList<>(valueAccessors);
    }

    @Override
    public void execute(ProgramState state) throws ProgramException {
        final List<Object> values = new LinkedList<>();
        for (ValueAccessor va : this.valueAccessors) {
            values.add(va.getValue(state));
        }
        state.getWriters().getLogWriter().addLogLine(values);
    }

}
