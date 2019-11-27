package au.csiro.data61.aap.elf.core.writers;

import java.util.List;

import au.csiro.data61.aap.elf.core.ProgramState;
import au.csiro.data61.aap.elf.core.exceptions.ProgramException;
import au.csiro.data61.aap.elf.core.values.ValueAccessor;

/**
 * AddXesTraceInstruction
 */
public class AddXesTraceInstruction extends AddXesElementInstruction {

    public AddXesTraceInstruction(ValueAccessor pid, ValueAccessor piid, List<XesParameter> parameters) {
        super(pid, piid, parameters);
    }

    @Override
    protected void startElement(XesWriter writer, ProgramState state, String pid, String piid) throws ProgramException {
        writer.startTrace(pid, piid);
    }

    
}