package blf.core.instructions;

import blf.core.parameters.XesParameter;
import blf.core.state.ProgramState;
import blf.core.values.ValueAccessor;
import blf.core.writers.XesWriter;

import java.util.List;

/**
 * AddXesTraceInstruction
 */
public class AddXesTraceInstruction extends AddXesElementInstruction {

    public AddXesTraceInstruction(ValueAccessor pid, ValueAccessor piid, List<XesParameter> parameters) {
        super(pid, piid, parameters);
    }

    @Override
    protected void startElement(XesWriter writer, ProgramState state, String pid, String piid) {
        writer.startTrace(pid, piid);
    }

}
