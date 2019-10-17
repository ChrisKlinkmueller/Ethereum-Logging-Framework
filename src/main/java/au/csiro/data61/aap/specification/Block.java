package au.csiro.data61.aap.specification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Block
 */
public abstract class Block implements Instruction {
    private final Instruction[] instructions;
    private final List<ValueContainer> valueContainers;

    public Block(Instruction... instructions) {
        assert Arrays.stream(instructions).allMatch(i -> i != null);
        this.instructions = Arrays.copyOf(instructions, instructions.length);
        this.valueContainers = new ArrayList<>();
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

    protected void registerValueContainterInState(ProgramState state, ValueContainer container) {
        state.addValueContainer(container);
        this.valueContainers.add(container);
    }

    protected void deregisterValueContainerFromState(ProgramState state, ValueContainer container) {
        state.removeValueContainer(container);
        this.valueContainers.remove(container);
    }

    protected void clearRegisteredValueContainers(ProgramState state) {
        this.valueContainers.forEach(c -> state.removeValueContainer(c));
        this.valueContainers.clear();;
    }
}