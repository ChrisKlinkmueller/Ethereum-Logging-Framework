package blf.core.instructions;

import blf.core.exceptions.ProgramException;
import blf.core.interfaces.GenericFilterPredicate;
import blf.core.state.ProgramState;
import io.reactivex.annotations.NonNull;

import java.util.List;

/**
 * GenericFilter
 */
public class GenericFilterInstruction extends Instruction {
    private final GenericFilterPredicate predicate;

    public GenericFilterInstruction(@NonNull GenericFilterPredicate predicate, List<Instruction> instructions) {
        super(instructions);
        this.predicate = predicate;
    }

    @Override
    public void execute(ProgramState state) {
        try {
            if (this.predicate.test(state)) {
                this.executeNestedInstructions(state);
            }
        } catch (ProgramException e) {
            final String exceptionMsg = String.format(
                "Exception occurred while executing nested instructions of %s class.",
                getClass().getName()
            );
            state.getExceptionHandler().handleException(exceptionMsg, new NullPointerException());
        }
    }

}
