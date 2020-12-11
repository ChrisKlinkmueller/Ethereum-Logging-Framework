package blf.core.writers;

import java.util.List;

import blf.core.ProgramState;
import blf.core.exceptions.ProgramException;
import blf.core.values.ValueAccessor;

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
