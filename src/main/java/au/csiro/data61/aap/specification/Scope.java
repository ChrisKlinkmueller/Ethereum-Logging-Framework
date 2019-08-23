package au.csiro.data61.aap.specification;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * TransformScope
 */
public class Scope extends AbstractInstruction {
    private final AbstractInstruction[] instructions;
    private final ScopeDefinition definition;
    
    public Scope(ScopeDefinition definition) {
        this(definition, new AbstractInstruction[0]);
    }

    public Scope(ScopeDefinition definition, AbstractInstruction[] instructions) {
        assert instructions != null;
        this.instructions = instructions;
        this.definition = definition;
    }

    public int instructionCount() {
        return this.instructions.length;
    }

    public AbstractInstruction getInstruction(int index) { 
        assert 0 <= index && index < this.instructions.length;
        return this.instructions[index];
    }

    public Stream<AbstractInstruction> instructionStream() {
        return Arrays.stream(this.instructions);
    }

    public ScopeDefinition getDefinition() {
        return this.definition;
    }
}