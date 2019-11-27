package au.csiro.data61.aap.elf.core.writers;

import java.util.List;

import au.csiro.data61.aap.elf.core.ProgramState;
import au.csiro.data61.aap.elf.core.exceptions.ProgramException;
import au.csiro.data61.aap.elf.core.values.ValueAccessor;

/**
 * AddXesEventInstruction
 */
public class AddXesEventInstruction extends AddXesElementInstruction {
    private final ValueAccessor eid;

    public AddXesEventInstruction(ValueAccessor pid, ValueAccessor piid, ValueAccessor eid, List<XesParameter> parameters) {
        super(pid, piid, parameters);
        this.eid = eid;
        
    }

    @Override
    protected void startElement(XesWriter writer, ProgramState state, String pid, String piid) throws ProgramException {
        final String eid = this.getId(state, this.eid);
        writer.startEvent(pid, piid, eid);
    }
    
}