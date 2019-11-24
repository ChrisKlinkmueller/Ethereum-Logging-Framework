package au.csiro.data61.aap.etl.core.writers;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import au.csiro.data61.aap.etl.core.Instruction;
import au.csiro.data61.aap.etl.core.ProgramState;
import au.csiro.data61.aap.etl.core.exceptions.ProgramException;
import au.csiro.data61.aap.etl.core.values.ValueAccessor;

/**
 * AddXesElementInstruction
 */
abstract class AddXesElementInstruction implements Instruction {
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
        return accessor == null ? null : (String)accessor.getValue(state);
    }

    protected abstract void startElement(XesWriter writer, ProgramState state, String pid, String piid) throws ProgramException;    
}