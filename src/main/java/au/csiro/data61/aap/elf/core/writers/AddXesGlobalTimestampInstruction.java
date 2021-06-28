package au.csiro.data61.aap.elf.core.writers;

import au.csiro.data61.aap.elf.core.Instruction;
import au.csiro.data61.aap.elf.core.ProgramState;
import au.csiro.data61.aap.elf.core.exceptions.ProgramException;
import au.csiro.data61.aap.elf.core.values.ValueAccessor;

public class AddXesGlobalTimestampInstruction implements Instruction {
    private final ValueAccessor pid;

    public AddXesGlobalTimestampInstruction(ValueAccessor pid) {
        this.pid = pid;
    }

    @Override
    public void execute(ProgramState state) throws ProgramException {
        if (this.pid == null) {
            state.getWriters().getXesWriter().addGlobalTimestampForDefaultPid();
        } else {
            final Object pidValue = pid.getValue(state);
            assert pidValue != null;
            state.getWriters().getXesWriter().addGlobalTimestamp(pidValue.toString());
        }
    }
}
