package blf.core.instructions;

import blf.core.parameters.XesParameter;
import blf.core.state.ProgramState;
import blf.core.values.ValueAccessor;
import blf.core.writers.XesWriter;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * AddXesElementInstruction
 */
public abstract class AddXesElementInstruction extends Instruction {
    private final List<XesParameter> parameters;
    private final ValueAccessor pid;
    private final ValueAccessor piid;

    protected AddXesElementInstruction(ValueAccessor pid, ValueAccessor piid, List<XesParameter> parameters) {
        assert parameters != null && parameters.stream().allMatch(Objects::nonNull);
        this.pid = pid;
        this.piid = piid;
        this.parameters = new LinkedList<>(parameters);
    }

    @Override
    public void execute(ProgramState state) {
        final XesWriter writer = state.getWriters().getXesWriter();

        this.startElement(writer, state, this.getId(state, this.pid), this.getId(state, this.piid));

        for (XesParameter parameter : this.parameters) {
            parameter.exportAttribute(state, writer);
        }

    }

    protected String getId(ProgramState state, ValueAccessor accessor) {
        return accessor == null ? null : accessor.getValue(state).toString();
    }

    protected abstract void startElement(XesWriter writer, ProgramState state, String pid, String piid);
}
