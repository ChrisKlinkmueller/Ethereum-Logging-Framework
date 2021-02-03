package blf.core.instructions;

import blf.core.interfaces.GenericFilterPredicate;
import blf.core.state.ProgramState;

import java.util.List;

/**
 * GenericFilter
 */
public class GenericFilterInstruction extends Instruction {
    private final GenericFilterPredicate predicate;

    public GenericFilterInstruction(GenericFilterPredicate predicate, List<Instruction> instructions) {
        super(instructions);
        this.predicate = predicate;
    }

    @Override
    public void execute(ProgramState state) {
        if (this.predicate.test(state)) {
            this.executeNestedInstructions(state);
        }
    }

}
