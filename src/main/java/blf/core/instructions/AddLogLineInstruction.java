package blf.core.instructions;

import blf.core.state.ProgramState;
import blf.core.values.ValueAccessor;
import io.reactivex.annotations.NonNull;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * AddLogLineInstruction
 */
public class AddLogLineInstruction extends Instruction {
    private final List<ValueAccessor> valueAccessors;

    public AddLogLineInstruction(ValueAccessor... valueAccessors) {
        this(Arrays.asList(valueAccessors));
    }

    public AddLogLineInstruction(@NonNull List<ValueAccessor> valueAccessors) {
        this.valueAccessors = new LinkedList<>(valueAccessors);
    }

    @Override
    public void execute(ProgramState state) {
        final List<Object> values = new LinkedList<>();
        for (ValueAccessor va : this.valueAccessors) {
            values.add(va.getValue(state));
        }
        state.getWriters().getLogWriter().addLogLine(values);
    }

}
