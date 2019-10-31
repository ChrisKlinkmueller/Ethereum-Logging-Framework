package au.csiro.data61.aap.spec;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Block
 */
public abstract class Scope extends Instruction {
    private final List<Instruction> instructions;

    protected Scope() {
        super(null);
        this.instructions = new ArrayList<>();
    }

    protected Scope(Scope enclosingScope) {
        super(enclosingScope);
        assert enclosingScope != null;
        this.instructions = new ArrayList<>();
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
}