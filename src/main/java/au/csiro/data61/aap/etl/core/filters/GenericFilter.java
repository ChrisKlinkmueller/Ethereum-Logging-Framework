package au.csiro.data61.aap.etl.core.filters;

import java.util.List;

import au.csiro.data61.aap.etl.core.Instruction;
import au.csiro.data61.aap.etl.core.ProgramState;
import au.csiro.data61.aap.etl.core.exceptions.ProgramException;

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