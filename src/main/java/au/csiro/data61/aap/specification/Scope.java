package au.csiro.data61.aap.specification;

import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * TransformScope
 */
public class Scope extends Instruction {
    private final Instruction[] instructions;
    private final ScopeDefinition definition;
    
    public Scope(ScopeDefinition definition) {
        this(definition, new Instruction[0]);
    }

    public Scope(ScopeDefinition definition, Instruction[] instructions) {
        this(null, definition, instructions);   
    }

    public Scope(Scope parent, ScopeDefinition definition) {
        this(parent, definition, new Instruction[0]);
    }

    public Scope(Scope parent, ScopeDefinition definition, Instruction[] instructions) {
        super(parent);
        assert definition != null;
        assert instructions != null;
        assert this.getParentScope() == null ? definition.getType() == ScopeType.GLOBAL_SCOPE : definition.getType() != ScopeType.GLOBAL_SCOPE;
        
        this.definition = definition;
        this.instructions = instructions;
    }

    public int instructionCount() {
        return this.instructions.length;
    }

    public Instruction getInstruction(int index) { 
        assert 0 <= index && index < this.instructions.length;
        return this.instructions[index];
    }

    public Stream<Instruction> instructionStream() {
        return Arrays.stream(this.instructions);
    }

    public ScopeDefinition getDefinition() {
        return this.definition;
    }

    protected int getChildPosition(Instruction child) {
        final int index = IntStream.range(0, this.instructions.length)
            .filter(i -> this.instructions[i].equals(child))
            .findFirst()
            .orElse(-1);

        assert 0 <= index && index < this.instructions.length;
        return index;
    }

}