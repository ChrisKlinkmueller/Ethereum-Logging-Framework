package au.csiro.data61.aap.etl.core.writers;

import java.util.List;

import au.csiro.data61.aap.etl.core.ProgramState;
import au.csiro.data61.aap.etl.core.exceptions.ProgramException;
import au.csiro.data61.aap.etl.core.values.ValueAccessor;

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