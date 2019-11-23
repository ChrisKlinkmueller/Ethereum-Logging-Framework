package au.csiro.data61.aap.etl.core.writers;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import au.csiro.data61.aap.etl.core.Instruction;
import au.csiro.data61.aap.etl.core.ProgramState;
import au.csiro.data61.aap.etl.core.exceptions.ProgramException;
import au.csiro.data61.aap.etl.core.values.ValueAccessor;

/**
 * AddLogLineInstruction
 */
public class AddLogLineInstruction implements Instruction {
    private final List<ValueAccessor> valueAccessors;

    public AddLogLineInstruction(ValueAccessor... valueAccessors) {
        this(Arrays.asList(valueAccessors));
    }

    public AddLogLineInstruction(List<ValueAccessor> valueAccessors) {
        assert valueAccessors != null && valueAccessors.stream().allMatch(Objects::nonNull);
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