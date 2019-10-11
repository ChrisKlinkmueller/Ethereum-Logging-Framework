package au.csiro.data61.aap.specification;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Block
 */
public abstract class Block implements Instruction {
    private final Instruction[] instructions;
    
    public Block(Instruction... instructions) {
        assert Arrays.stream(instructions).allMatch(i -> i != null);
        this.instructions = Arrays.copyOf(instructions, instructions.length);
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
}