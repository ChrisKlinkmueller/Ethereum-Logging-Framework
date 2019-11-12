package au.csiro.data61.aap.spec;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import au.csiro.data61.aap.spec.types.SolidityType;

/**
 * Block
 */
public abstract class Scope extends Instruction {
    private final List<Instruction> instructions;

    protected Scope() {
        this.instructions = new ArrayList<>();
    }

    protected void initDefaultVariables() {}

    protected static final void addVariable(Set<Variable> variables, SolidityType type, String name) {
        assert variables != null && type != null && name != null;
        variables.add(new Variable(type, name, VariableCategory.SCOPE_VARIABLE, null));
    }

    public void addInstruction(Instruction instruction) {
        this.instructions.add(instruction);
    }

    public int instructionCount() {
        return this.instructions.size();
    }

    public Instruction getInstruction(int index) {
        assert 0 <= index && index < this.instructionCount();
        return this.instructions.get(index);
    }

    public Stream<Instruction> instructionStream() {
        return this.instructions.stream();
    }    

    public abstract Stream<Variable> defaultVariableStream();
}