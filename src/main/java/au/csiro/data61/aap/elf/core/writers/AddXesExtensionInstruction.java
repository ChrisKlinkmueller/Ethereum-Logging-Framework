package au.csiro.data61.aap.elf.core.writers;

import au.csiro.data61.aap.elf.core.Instruction;
import au.csiro.data61.aap.elf.core.ProgramState;
import au.csiro.data61.aap.elf.core.exceptions.ProgramException;

public class AddXesExtensionInstruction implements Instruction {
    private final String prefix;

    public AddXesExtensionInstruction(String prefix) {
        assert prefix != null;
        this.prefix = prefix;
    }

    @Override
    public void execute(ProgramState state) throws ProgramException {
        state.getWriters().getXesWriter().addExtension(this.prefix);
    }
}
