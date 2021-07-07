/*
package au.csiro.data61.aap.elf.core.writers;

import au.csiro.data61.aap.elf.core.Instruction;
import au.csiro.data61.aap.elf.core.ProgramState;
import au.csiro.data61.aap.elf.core.exceptions.ProgramException;
import au.csiro.data61.aap.elf.core.values.ValueAccessor;

public class AddXesGlobalValueInstruction implements Instruction {
    private final ValueAccessor pid;
    private final String attribute;

    public AddXesGlobalValueInstruction(ValueAccessor pid, String attribute) {
        assert pid != null;
        assert attribute != null;
        this.pid = pid;
        this.attribute = attribute;
    }

    @Override
    public void execute(ProgramState state) throws ProgramException {
        if (this.pid == null) {
            state.getWriters().getXesWriter().addGlobalValueForDefaultPid(attribute);
        } else {
            final Object pidValue = pid.getValue(state);
            assert pidValue != null;
            state.getWriters().getXesWriter().addGlobalValue(pidValue.toString(), attribute);
        }
    }
}
*/
