package blf.core.filters;

import java.util.List;

import blf.core.Instruction;
import blf.core.ProgramState;
import blf.core.exceptions.ProgramException;
import io.reactivex.annotations.NonNull;

/**
 * GenericFilter
 */
public class GenericFilterInstruction extends FilterInstruction {
    private final GenericFilterPredicate predicate;

    public GenericFilterInstruction(@NonNull GenericFilterPredicate predicate, List<Instruction> instructions) {
        super(instructions);
        this.predicate = predicate;
    }

    @Override
    public void execute(ProgramState state) throws ProgramException {
        if (this.predicate.test(state)) {
            this.executeInstructions(state);
        }
    }

}
