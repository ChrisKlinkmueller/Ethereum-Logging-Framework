package blf.core.filters;

import java.util.List;

import blf.core.Instruction;
import blf.core.ProgramState;
import blf.core.exceptions.ProgramException;

/**
 * GenericFilter
 */
public class GenericFilter extends Filter {
    private final GenericFilterPredicate predicate;

    public GenericFilter(GenericFilterPredicate predicate, List<Instruction> instructions) {
        super(instructions);
        assert predicate != null;
        this.predicate = predicate;
    }

    @Override
    public void execute(ProgramState state) throws ProgramException {
        if (this.predicate.test(state)) {
            this.executeInstructions(state);
        }
    }

}
