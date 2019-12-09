package au.csiro.data61.aap.elf.core.filters;

import java.util.ArrayList;
import java.util.List;

import au.csiro.data61.aap.elf.core.Instruction;
import au.csiro.data61.aap.elf.core.ProgramState;
import au.csiro.data61.aap.elf.core.exceptions.ProgramException;

/**
 * SmartContractFilter
 */
public class SmartContractFilter extends Filter {
    private final List<SmartContractQuery> queries;
    private final String contract;

    public SmartContractFilter(String contract, List<SmartContractQuery> queries, List<Instruction> instructions) {
        super(instructions);
        assert contract != null;
        assert queries != null;
        this.queries = new ArrayList<>(queries);
        this.contract = contract;
    }

    @Override
    public void execute(ProgramState state) throws ProgramException {
        for (SmartContractQuery query : this.queries) {
            query.query(this.contract, state);
        }
        this.executeInstructions(state);
    }

    
}