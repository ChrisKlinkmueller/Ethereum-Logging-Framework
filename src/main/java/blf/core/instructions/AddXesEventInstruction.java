package blf.core.instructions;

import blf.core.parameters.XesParameter;
import blf.core.state.ProgramState;
import blf.core.values.ValueAccessor;
import blf.core.writers.XesWriter;

import java.util.List;

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
    protected void startElement(XesWriter writer, ProgramState state, String pid, String piid) {
        final String eId = this.getId(state, this.eid);
        XesWriter.startEvent(writer, pid, piid, eId);
    }

}
