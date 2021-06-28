package au.csiro.data61.aap.elf.core.writers;

import au.csiro.data61.aap.elf.core.Instruction;
import au.csiro.data61.aap.elf.core.ProgramState;
import au.csiro.data61.aap.elf.core.exceptions.ProgramException;
import au.csiro.data61.aap.elf.core.values.ValueAccessor;

public class AddXesExtensionInstruction implements Instruction {
    private final String prefix;
    private final ValueAccessor pid;

    public AddXesExtensionInstruction(String prefix, ValueAccessor pid) {
        this.prefix = prefix;
        this.pid = pid;
    }

    @Override
    public void execute(ProgramState state) throws ProgramException {
        if (this.pid == null) {
            state.getWriters().getXesWriter().addExtensionForDefaultPid(this.prefix);
        } else {
            final Object pidValue = pid.getValue(state);
            assert pidValue != null;
            state.getWriters().getXesWriter().addExtension(pidValue.toString(), this.prefix);
        }

    }
}
