package blf.core.writers;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import blf.core.Instruction;
import blf.core.ProgramState;
import blf.core.exceptions.ProgramException;
import blf.core.values.ValueAccessor;

/**
 * AddXesElementInstruction
 */
public abstract class AddXesElementInstruction implements Instruction {
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
    public void execute(ProgramState state) throws ProgramException {
        final XesWriter writer = state.getWriters().getXesWriter();
        this.startElement(writer, state, this.getId(state, this.pid), this.getId(state, this.piid));

        for (XesParameter parameter : this.parameters) {
            parameter.exportAttribute(state, writer);
        }
    }

    protected String getId(ProgramState state, ValueAccessor accessor) throws ProgramException {
        return accessor == null ? null : accessor.getValue(state).toString();
    }

    protected abstract void startElement(XesWriter writer, ProgramState state, String pid, String piid) throws ProgramException;
}
